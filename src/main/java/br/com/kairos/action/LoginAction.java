package br.com.kairos.action;

import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.SessionMap;

import com.opensymphony.xwork2.ActionContext;

import br.com.kairos.business.AutenticacaoBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Action;
import br.com.kairos.infra.AutenticacaoInterceptor;
import br.com.kairos.vo.UsuarioVo;

/**
 * Tela de login, entrada e saída do sistema (RN-AC-01 a RN-AC-04).
 *
 * É a única Action com classe que fica fora do AutenticacaoInterceptor:
 * quem ainda não entrou precisa conseguir abrir esta tela.
 */
public class LoginAction extends Action {

	private static final long serialVersionUID = 1L;

	private final AutenticacaoBusiness business = new AutenticacaoBusiness();

	private String usuario;
	/** Só tem setter: a senha digitada nunca volta para a página. */
	private String senha;

	public String abrir() {
		// Quem já entrou não precisa ver o login de novo
		return sessao().get(AutenticacaoInterceptor.USUARIO) != null ? REDIRECT : SUCCESS;
	}

	public String entrar() {
		if (!requisicaoEhPost())
			return abrir();

		try {
			UsuarioVo autenticado = business.autenticar(usuario, senha);

			// Troca o identificador da sessão ao entrar. Se alguém plantou um
			// identificador no navegador da vítima antes do login, ele deixa de valer.
			ServletActionContext.getRequest().getSession(true);
			ServletActionContext.getRequest().changeSessionId();

			sessao().put(AutenticacaoInterceptor.USUARIO, autenticado);
			return REDIRECT;

		} catch (BusinessException e) {
			addActionError(e.getMessage());
			return INPUT;
		}
	}

	/** RN-AC-04: encerra a sessão inteira, não só apaga o usuário dela. */
	public String sair() {
		if (!requisicaoEhPost())
			return REDIRECT;

		Map<String, Object> sessao = sessao();
		if (sessao instanceof SessionMap)
			((SessionMap<String, Object>) sessao).invalidate();

		addActionMessage(getText("login.saiu"));
		return AutenticacaoInterceptor.LOGIN;
	}

	private Map<String, Object> sessao() {
		return ActionContext.getContext().getSession();
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
}
