package br.com.kairos.business;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.kairos.dao.ResultadoDao;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Indicador;
import br.com.kairos.infra.Transacao;
import br.com.kairos.vo.CompromissoVo;
import br.com.kairos.vo.ResultadoVo;
import br.com.kairos.vo.UsuarioVo;
import br.com.kairos.vo.VersaoResultadoVo;

/**
 * Regras do atendimento do médico e do resultado do exame (RN-AT).
 * O compromisso continua compromisso: o resultado é à parte, um por compromisso.
 */
public class AtendimentoBusiness {

	static final int TAMANHO_MAXIMO_DO_TEXTO = 10000;
	static final int TAMANHO_MAXIMO_DO_MOTIVO = 500;
	static final int MAXIMO_DE_PENDENTES = 50;

	static final String SO_MEDICO = "Só médico usa o atendimento.";
	static final String JA_LIBERADO = "Resultado já liberado. Para mudar, retifique.";
	static final String NAO_LIBERADO = "Só resultado liberado pode ser retificado.";
	static final String NAO_ENCONTRADO = "Resultado não encontrado.";

	private final ResultadoDao dao = new ResultadoDao();
	private final CompromissoBusiness compromissoBusiness = new CompromissoBusiness();

	/** RN-AT-07: compromissos de hoje, com a situação do resultado. */
	public List<CompromissoVo> filaDoDia(LocalDate hoje) {
		return dao.listarDoDia(hoje);
	}

	/** RN-AT-07: até 50 compromissos anteriores sem resultado liberado, do mais novo para o mais antigo. */
	public List<CompromissoVo> pendentes(LocalDate hoje) {
		return dao.listarPendentesAntesDe(hoje, MAXIMO_DE_PENDENTES);
	}

	/** O compromisso que será atendido. Lança erro se não existir. */
	public CompromissoVo compromisso(Long codigoCompromisso) {
		return compromissoBusiness.buscar(codigoCompromisso);
	}

	/** O resultado do compromisso, com as versões; um rascunho vazio se ainda não houver. */
	public ResultadoVo resultadoDe(CompromissoVo compromisso) {
		ResultadoVo resultado = dao.buscarPorCompromisso(compromisso.getCodigo());

		if (resultado == null) {
			resultado = new ResultadoVo();
			resultado.setCodigoCompromisso(compromisso.getCodigo());
			return resultado;
		}

		resultado.setVersoes(dao.listarVersoes(resultado.getCodigo()));
		return resultado;
	}

	/** Os outros compromissos do funcionário, com o resultado quando houver. */
	public List<ResultadoVo> historico(CompromissoVo compromisso) {
		return dao.listarHistorico(compromisso.getCodigoFuncionario(), compromisso.getCodigo());
	}

	/** RN-AT-02: o rascunho pode ficar incompleto. Inclui ou altera. */
	public void salvarRascunho(ResultadoVo informado, UsuarioVo medico) {
		exigirMedico(medico);
		validarTextos(informado);
		CompromissoVo compromisso = compromisso(informado.getCodigoCompromisso());

		ResultadoVo atual = dao.buscarPorCompromisso(compromisso.getCodigo());
		if (atual != null && atual.isLiberado())
			throw new BusinessException(JA_LIBERADO);                                        // RN-AT-03

		ResultadoVo gravar = atual == null ? novo(compromisso) : atual;
		gravar.setCodigoMedico(medico.getCodigo());
		gravar.setIndicador(nomeDoIndicador(informado.getIndicador(), false));
		gravar.setAchados(informado.getAchados());
		gravar.setParecer(informado.getParecer());
		gravar.setSituacao(ResultadoVo.RASCUNHO);

		if (gravar.getCodigo() == null)
			dao.inserir(gravar);
		else
			dao.alterar(gravar);
	}

	/** RN-AT-02: liberar exige indicador e parecer. RN-ME-05: a assinatura é gravada no momento. */
	public void liberar(ResultadoVo informado, UsuarioVo medico, LocalDateTime agora) {
		exigirMedico(medico);
		validarTextos(informado);
		CompromissoVo compromisso = compromisso(informado.getCodigoCompromisso());

		ResultadoVo atual = dao.buscarPorCompromisso(compromisso.getCodigo());
		if (atual != null && atual.isLiberado())
			throw new BusinessException(JA_LIBERADO);                                        // RN-AT-03

		ResultadoVo gravar = atual == null ? novo(compromisso) : atual;
		preencherLiberacao(gravar, informado, medico, agora);

		if (gravar.getCodigo() == null)
			dao.inserir(gravar);
		else
			dao.alterar(gravar);
	}

	/**
	 * RN-AT-03: resultado liberado não se edita. A retificação exige motivo; a versão
	 * anterior fica guardada com o motivo, quem retificou e quando, e o resultado vira a próxima versão.
	 */
	public void retificar(ResultadoVo informado, String motivo, UsuarioVo medico, LocalDateTime agora) {
		exigirMedico(medico);
		validarTextos(informado);
		String motivoLimpo = validarMotivo(motivo);

		ResultadoVo atual = dao.buscarPorCompromisso(informado.getCodigoCompromisso());
		if (atual == null)
			throw new BusinessException(NAO_ENCONTRADO);
		if (!atual.isLiberado())
			throw new BusinessException(NAO_LIBERADO);

		VersaoResultadoVo anterior = new VersaoResultadoVo();
		anterior.setCodigoResultado(atual.getCodigo());
		anterior.setVersao(atual.getVersao());
		anterior.setIndicador(atual.getIndicador());
		anterior.setAchados(atual.getAchados());
		anterior.setParecer(atual.getParecer());
		anterior.setAssinatura(atual.getAssinatura());
		anterior.setLiberadoEm(atual.getLiberadoEm());
		anterior.setMotivo(motivoLimpo);
		anterior.setRetificadoPor(assinaturaDe(medico));
		anterior.setRetificadoEm(agora);

		preencherLiberacao(atual, informado, medico, agora);
		atual.setVersao(anterior.getVersao() + 1);

		Transacao.executar(conexao -> {
			dao.inserirVersao(conexao, anterior);
			if (dao.alterar(conexao, atual) == 0)
				throw new BusinessException(NAO_ENCONTRADO);
		});
	}

	/** RN-AT-06: leitura de um resultado liberado, com as versões. */
	public ResultadoVo buscarLiberado(Long codigo) {
		ResultadoVo resultado = codigo == null ? null : dao.buscarPorCodigo(codigo);

		if (resultado == null || !resultado.isLiberado())
			throw new BusinessException(NAO_ENCONTRADO);

		resultado.setVersoes(dao.listarVersoes(resultado.getCodigo()));
		return resultado;
	}

	/** RN-AT-06: resultados liberados, com pesquisa por funcionário ou agenda. */
	public List<ResultadoVo> listarLiberados(String busca) {
		return dao.listarLiberados(busca);
	}

	/** Usado pelo cadastro de compromissos (RN-AT-04). */
	public boolean temResultado(Long codigoCompromisso) {
		return codigoCompromisso != null && dao.existePorCompromisso(codigoCompromisso);
	}

	private ResultadoVo novo(CompromissoVo compromisso) {
		ResultadoVo resultado = new ResultadoVo();
		resultado.setCodigoCompromisso(compromisso.getCodigo());
		return resultado;
	}

	private void preencherLiberacao(ResultadoVo gravar, ResultadoVo informado, UsuarioVo medico, LocalDateTime agora) {
		String parecer = informado.getParecer() == null ? "" : informado.getParecer().trim();
		if (parecer.isEmpty())
			throw new BusinessException("Informe o parecer para liberar o resultado.");

		gravar.setCodigoMedico(medico.getCodigo());
		gravar.setIndicador(nomeDoIndicador(informado.getIndicador(), true));
		gravar.setAchados(informado.getAchados());
		gravar.setParecer(parecer);
		gravar.setSituacao(ResultadoVo.LIBERADO);
		gravar.setAssinatura(assinaturaDe(medico));
		gravar.setLiberadoEm(agora);
	}

	/** RN-ME-05: nome, CRM e UF do médico como estão agora; não mudam se o cadastro mudar depois. */
	static String assinaturaDe(UsuarioVo medico) {
		return medico.getNome() + " - " + medico.getMedico().getCrmFormatado();
	}

	/** RN-AT-01: só médico lança resultado, conferido a cada pedido. */
	void exigirMedico(UsuarioVo usuario) {
		if (usuario == null || !usuario.ehMedico() || usuario.getCodigo() == null)
			throw new BusinessException(SO_MEDICO);
	}

	/** RN-AT-02: achados e parecer até 10.000 caracteres. */
	void validarTextos(ResultadoVo resultado) {
		if (resultado.getCodigoCompromisso() == null)
			throw new BusinessException("Compromisso não encontrado.");

		if (tamanho(resultado.getAchados()) > TAMANHO_MAXIMO_DO_TEXTO)
			throw new BusinessException("Os achados podem ter no máximo " + TAMANHO_MAXIMO_DO_TEXTO + " caracteres.");

		if (tamanho(resultado.getParecer()) > TAMANHO_MAXIMO_DO_TEXTO)
			throw new BusinessException("O parecer pode ter no máximo " + TAMANHO_MAXIMO_DO_TEXTO + " caracteres.");

		resultado.setAchados(limpar(resultado.getAchados()));
		resultado.setParecer(limpar(resultado.getParecer()));
	}

	/** RN-AT-03: o motivo da retificação é obrigatório, até 500 caracteres. */
	String validarMotivo(String motivo) {
		String limpo = motivo == null ? "" : motivo.trim();

		if (limpo.isEmpty())
			throw new BusinessException("Informe o motivo da retificação.");

		if (limpo.length() > TAMANHO_MAXIMO_DO_MOTIVO)
			throw new BusinessException("O motivo pode ter no máximo " + TAMANHO_MAXIMO_DO_MOTIVO + " caracteres.");

		return limpo;
	}

	/** RN-AT-02: indicador é um dos quatro do eSocial; obrigatório só para liberar. */
	String nomeDoIndicador(String informado, boolean obrigatorio) {
		Indicador indicador = Indicador.porNome(informado);

		if (indicador == null && (obrigatorio || (informado != null && !informado.trim().isEmpty())))
			throw new BusinessException("Selecione o indicador do resultado.");

		return indicador == null ? null : indicador.name();
	}

	private static int tamanho(String texto) {
		return texto == null ? 0 : texto.trim().length();
	}

	private static String limpar(String texto) {
		if (texto == null)
			return null;
		String limpo = texto.trim();
		return limpo.isEmpty() ? null : limpo;
	}
}
