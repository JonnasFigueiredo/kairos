package br.com.kairos.action;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.kairos.business.AnaliseIaBusiness;
import br.com.kairos.business.AtendimentoBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Acao;
import br.com.kairos.infra.Action;
import br.com.kairos.infra.Indicador;
import br.com.kairos.infra.Modulo;
import br.com.kairos.vo.CompromissoVo;
import br.com.kairos.vo.ResultadoVo;

/**
 * Telas do atendimento do médico (RN-AT): fila do dia, tela do atendimento com
 * rascunho, liberação e retificação do resultado.
 *
 * Só chega aqui conta de médico com o módulo Atendimento (AutorizacaoInterceptor);
 * a Business confere de novo a cada gravação (RN-AT-01).
 */
public class AtendimentoAction extends Action {

	private static final long serialVersionUID = 1L;

	/** Resultado do struts.xml que abre a tela do atendimento. */
	static final String ATENDIMENTO = "atendimento";
	/** Volta para a mesma tela do atendimento depois de gravar. */
	static final String REABRIR = "reabrir";

	private final AtendimentoBusiness business = new AtendimentoBusiness();

	/** Fila do dia e pendentes (RN-AT-07). */
	private List<CompromissoVo> hoje = new ArrayList<>();
	private List<CompromissoVo> pendentes = new ArrayList<>();

	/** Compromisso em atendimento: vem da URL e do campo escondido do formulário. */
	private Long codigoCompromisso;
	private CompromissoVo compromisso;
	private ResultadoVo resultadoVo = new ResultadoVo();
	private List<ResultadoVo> historico = new ArrayList<>();
	/** Só na retificação (RN-AT-03). */
	private String motivo;

	public String fila() {
		LocalDate dia = LocalDate.now();
		hoje = business.filaDoDia(dia);
		pendentes = business.pendentes(dia);
		return SUCCESS;
	}

	public String abrir() {
		try {
			carregarTela();
			return ATENDIMENTO;
		} catch (BusinessException e) {
			clearFieldErrors();
			addActionError(e.getMessage());
			return REDIRECT;
		}
	}

	/** Grava o rascunho (RN-AT-02). O interceptor exige INCLUIR ou ALTERAR; rascunho é inclusão. */
	public String salvar() {
		if (!requisicaoEhPost())
			return REDIRECT;
		if (!usuarioLogado().pode(Modulo.ATENDIMENTO, Acao.INCLUIR))
			return SEM_ACESSO;

		return gravar(new Gravacao() {
			@Override
			public void executar() {
				business.salvarRascunho(resultadoVo, usuarioLogado());
			}
		}, "atendimento.rascunho.salvo");
	}

	public String liberar() {
		if (!requisicaoEhPost())
			return REDIRECT;

		return gravar(new Gravacao() {
			@Override
			public void executar() {
				business.liberar(resultadoVo, usuarioLogado(), agora());
			}
		}, "atendimento.liberado.msg");
	}

	public String retificar() {
		if (!requisicaoEhPost())
			return REDIRECT;

		return gravar(new Gravacao() {
			@Override
			public void executar() {
				business.retificar(resultadoVo, motivo, usuarioLogado(), agora());
			}
		}, "atendimento.retificado");
	}

	/** Regra violada: volta à tela do atendimento com a mensagem e o que foi digitado. */
	private String gravar(Gravacao gravacao, String chaveDaMensagem) {
		resultadoVo.setCodigoCompromisso(codigoCompromisso);
		try {
			gravacao.executar();
			addActionMessage(getText(chaveDaMensagem));
			return REABRIR;
		} catch (BusinessException e) {
			addActionError(e.getMessage());
			ResultadoVo digitado = resultadoVo;
			try {
				carregarTela();
			} catch (BusinessException semCompromisso) {
				return REDIRECT;
			}
			// Mantém o que o médico digitou, em vez do que está gravado
			if (!resultadoVo.isLiberado()) {
				resultadoVo.setIndicador(digitado.getIndicador());
				resultadoVo.setAchados(digitado.getAchados());
				resultadoVo.setParecer(digitado.getParecer());
			}
			return ATENDIMENTO;
		}
	}

	private void carregarTela() {
		compromisso = business.compromisso(codigoCompromisso);
		resultadoVo = business.resultadoDe(compromisso);
		historico = business.historico(compromisso);
	}

	private static LocalDateTime agora() {
		return LocalDateTime.now().withNano(0);
	}

	/** Uma gravação da Business, para o tratamento de erro ficar em um lugar só. */
	private interface Gravacao {
		void executar();
	}

	/** RN-IA-06: o painel da IA só aparece quando há provedor configurado. */
	public boolean isIaDisponivel() {
		return new AnaliseIaBusiness().disponivel();
	}

	/** Opções do select de indicador (RN-AT-02). */
	public List<Indicador> getIndicadores() {
		return Arrays.asList(Indicador.values());
	}

	public List<CompromissoVo> getHoje() {
		return hoje;
	}

	public List<CompromissoVo> getPendentes() {
		return pendentes;
	}

	public Long getCodigoCompromisso() {
		return codigoCompromisso;
	}

	public void setCodigoCompromisso(Long codigoCompromisso) {
		this.codigoCompromisso = codigoCompromisso;
	}

	public CompromissoVo getCompromisso() {
		return compromisso;
	}

	public ResultadoVo getResultadoVo() {
		return resultadoVo;
	}

	public void setResultadoVo(ResultadoVo resultadoVo) {
		this.resultadoVo = resultadoVo;
	}

	public List<ResultadoVo> getHistorico() {
		return historico;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
}
