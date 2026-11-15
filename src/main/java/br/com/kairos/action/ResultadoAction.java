package br.com.kairos.action;

import java.util.ArrayList;
import java.util.List;

import br.com.kairos.business.AtendimentoBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Action;
import br.com.kairos.vo.ResultadoVo;

/**
 * Leitura dos resultados liberados (RN-AT-06), para quem tem o módulo Resultados.
 * Rascunho não aparece aqui: só no atendimento do médico.
 */
public class ResultadoAction extends Action {

	private static final long serialVersionUID = 1L;

	/** Resultado do struts.xml que abre a leitura de um resultado. */
	static final String VER = "ver";

	private final AtendimentoBusiness business = new AtendimentoBusiness();

	private String busca;
	private List<ResultadoVo> resultados = new ArrayList<>();
	private ResultadoVo resultadoVo = new ResultadoVo();

	public String listar() {
		resultados = business.listarLiberados(busca);
		return SUCCESS;
	}

	public String ver() {
		try {
			resultadoVo = business.buscarLiberado(resultadoVo.getCodigo());
			return VER;
		} catch (BusinessException e) {
			clearFieldErrors();
			addActionError(e.getMessage());
			return REDIRECT;
		}
	}

	public String getBusca() {
		return busca;
	}

	public void setBusca(String busca) {
		this.busca = busca;
	}

	public List<ResultadoVo> getResultados() {
		return resultados;
	}

	public ResultadoVo getResultadoVo() {
		return resultadoVo;
	}

	public void setResultadoVo(ResultadoVo resultadoVo) {
		this.resultadoVo = resultadoVo;
	}
}
