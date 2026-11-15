package br.com.kairos.business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import br.com.kairos.dao.AnaliseIaDao;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.exception.IaIndisponivelException;
import br.com.kairos.infra.Configuracao;
import br.com.kairos.infra.ia.Instrucoes;
import br.com.kairos.infra.ia.ModeloDeLinguagem.FalhaDoModelo;
import br.com.kairos.infra.ia.ModeloDeLinguagem.PedidoModelo;
import br.com.kairos.infra.ia.ModeloDeLinguagem.RespostaModelo;
import br.com.kairos.infra.ia.MontadorDeContexto;
import br.com.kairos.infra.ia.ProvedoresIa;
import br.com.kairos.vo.AnaliseVo;
import br.com.kairos.vo.CompromissoVo;
import br.com.kairos.vo.ResultadoVo;
import br.com.kairos.vo.UsuarioVo;

/**
 * Assistente de IA do médico (RN-IA). Monta o contexto pseudonimizado a partir do
 * atendimento, chama o provedor configurado e devolve a análise em seções.
 * Nada é salvo no resultado: o médico decide o que usar.
 */
public class AnaliseIaBusiness {

	private static final Logger LOG = LogManager.getLogger(AnaliseIaBusiness.class);

	static final String SO_MEDICO = "Só médico usa o assistente de IA.";
	static final String DESLIGADA = "O assistente de IA não está configurado neste servidor.";
	static final String LIMITE = "Limite diário de análises atingido. Siga sem a IA ou tente amanhã.";
	static final String FALHOU = "A IA não respondeu agora. Tente de novo ou siga sem ela.";
	static final int LIMITE_DIARIO_PADRAO = 30;
	static final int MAX_ERRO = 500;

	private final AnaliseIaDao dao = new AnaliseIaDao();
	private final AtendimentoBusiness atendimento = new AtendimentoBusiness();
	private final ProvedoresIa provedores;
	private final int limiteDiario;

	public AnaliseIaBusiness() {
		this(ProvedoresIa.instancia(), lerLimiteDiario());
	}

	AnaliseIaBusiness(ProvedoresIa provedores, int limiteDiario) {
		this.provedores = provedores;
		this.limiteDiario = limiteDiario;
	}

	/** RN-IA-06: sem provedor configurado, o painel não aparece. */
	public boolean disponivel() {
		return provedores.disponivel();
	}

	public String provedor() {
		return provedores.descricao();
	}

	/**
	 * Sem transação de propósito: a chamada à IA pode levar um minuto, e não se segura
	 * conexão do banco esperando serviço externo. Leitura e auditoria usam conexões curtas próprias.
	 *
	 * @param atual o que está no formulário agora, mesmo sem salvar (RN-IA-02)
	 */
	public AnaliseVo analisar(Long codigoCompromisso, ResultadoVo atual, UsuarioVo medico, LocalDateTime agora) {
		if (medico == null || !medico.ehMedico() || medico.getCodigo() == null)
			throw new BusinessException(SO_MEDICO);                                              // RN-IA-01
		if (!provedores.disponivel())
			throw new BusinessException(DESLIGADA);                                              // RN-IA-06
		if (dao.contarDesde(medico.getCodigo(), agora.toLocalDate().atStartOfDay()) >= limiteDiario)
			throw new BusinessException(LIMITE);                                                 // RN-IA-04

		CompromissoVo compromisso = atendimento.compromisso(codigoCompromisso);
		ResultadoVo salvo = atendimento.resultadoDe(compromisso);
		List<ResultadoVo> historico = atendimento.historico(compromisso);
		String contexto = MontadorDeContexto.montar(compromisso, salvo, atual == null ? new ResultadoVo() : atual, historico);

		long inicio = System.nanoTime();
		try {
			RespostaModelo resposta = provedores.responder(new PedidoModelo(Instrucoes.TEXTO, contexto, Instrucoes.ESQUEMA));
			dao.registrar(compromisso.getCodigo(), medico.getCodigo(), resposta.getDescricao(), contexto, resposta.getTexto(),
					true, null, resposta.getTokensEntrada(), resposta.getTokensSaida(), duracao(inicio), agora);     // RN-IA-05
			return interpretar(resposta.getTexto(), resposta.getDescricao());

		} catch (FalhaDoModelo e) {
			LOG.error("A IA falhou no compromisso {}: {}", compromisso.getCodigo(), e.getMessage(), e);
			String erro = e.getMessage() == null ? "falha" : e.getMessage();
			dao.registrar(compromisso.getCodigo(), medico.getCodigo(), provedores.descricao(), contexto, null, false,
					erro.substring(0, Math.min(MAX_ERRO, erro.length())), null, null, duracao(inicio), agora);
			throw new IaIndisponivelException(FALHOU);                                        // RN-IA-05
		}
	}

	/** RN-IA-03: JSON das seções; se o provedor não seguiu o formato, o texto inteiro vai no resumo. */
	@SuppressWarnings("unchecked")
	AnaliseVo interpretar(String texto, String provedor) {
		AnaliseVo analise = new AnaliseVo();
		analise.setProvedor(provedor);

		String limpo = texto.trim().replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
		Object valor;
		try {
			valor = JSONUtil.deserialize(limpo);
		} catch (JSONException e) {
			valor = null;
		}

		if (!(valor instanceof Map)) {
			analise.setResumo(texto);
			analise.setEstruturada(false);
			return analise;
		}

		Map<String, Object> campos = (Map<String, Object>) valor;
		analise.setResumo(textoDe(campos.get("resumo")));
		analise.setComparacaoComAnteriores(textoDe(campos.get("comparacaoComAnteriores")));
		analise.setPontosDeAtencao(listaDe(campos.get("pontosDeAtencao")));
		analise.setSugestaoDeParecer(textoDe(campos.get("sugestaoDeParecer")));
		analise.setDadosFaltantes(listaDe(campos.get("dadosFaltantes")));
		analise.setLimitacoes(textoDe(campos.get("limitacoes")));
		return analise;
	}

	private static String textoDe(Object valor) {
		return valor == null ? "" : valor.toString();
	}

	private static List<String> listaDe(Object valor) {
		List<String> lista = new ArrayList<>();
		if (valor instanceof List)
			for (Object item : (List<?>) valor)
				if (item != null)
					lista.add(item.toString());
		return lista;
	}

	private static int lerLimiteDiario() {
		try {
			return Integer.parseInt(Configuracao.obter("KAIROS_IA_LIMITE_DIARIO", String.valueOf(LIMITE_DIARIO_PADRAO)));
		} catch (NumberFormatException e) {
			return LIMITE_DIARIO_PADRAO;
		}
	}

	private static int duracao(long inicioNanos) {
		return (int) ((System.nanoTime() - inicioNanos) / 1_000_000);
	}
}
