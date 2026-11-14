package br.com.kairos.action;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import br.com.kairos.infra.Action;

/**
 * Página de erro técnico. Responde com o código HTTP 500, e não 200:
 * navegador, cache e monitoramento precisam saber que a operação falhou.
 */
public class ErroAction extends Action {

	private static final long serialVersionUID = 1L;

	@Override
	public String execute() {
		ServletActionContext.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return SUCCESS;
	}
}
