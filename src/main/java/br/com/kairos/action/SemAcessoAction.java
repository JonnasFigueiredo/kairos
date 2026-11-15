package br.com.kairos.action;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import br.com.kairos.infra.Action;

/**
 * Página de acesso negado (RN-PE-02). Responde com o código HTTP 403:
 * o usuário está logado, mas o perfil dele não alcança esta função.
 */
public class SemAcessoAction extends Action {

	private static final long serialVersionUID = 1L;

	@Override
	public String execute() {
		ServletActionContext.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
		return SUCCESS;
	}
}
