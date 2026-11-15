package br.com.kairos.infra;

import java.util.Map;
import java.util.Set;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.UsuarioVo;

/**
 * Segundo porteiro (RN-PE-02): roda depois do AutenticacaoInterceptor e confere
 * se o usuário logado tem permissão no módulo da Action.
 *
 * O módulo vem do struts.xml, como parâmetro da pilha:
 *   <param name="autorizacao.modulo">FUNCIONARIOS</param>
 * A ação exigida sai do nome do método (Acao.exigidasPor). Action sem módulo
 * configurado (ex.: página inicial) só exige estar logado.
 */
public class AutorizacaoInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 1L;

	/** Resultado global do struts.xml que leva à página de acesso negado. */
	public static final String SEM_ACESSO = "semAcesso";

	private String modulo;

	@Override
	public String intercept(ActionInvocation chamada) throws Exception {
		if (modulo == null || modulo.trim().isEmpty())
			return chamada.invoke();

		Map<String, Object> sessao = chamada.getInvocationContext().getSession();
		Object usuario = sessao == null ? null : sessao.get(AutenticacaoInterceptor.USUARIO);

		if (!(usuario instanceof UsuarioVo))
			return AutenticacaoInterceptor.LOGIN;

		if (!autorizado((UsuarioVo) usuario, chamada.getProxy().getMethod()))
			return SEM_ACESSO;

		return chamada.invoke();
	}

	boolean autorizado(UsuarioVo usuario, String metodo) {
		Modulo moduloDaAction = Modulo.porNome(modulo.trim());
		if (moduloDaAction == null)
			throw new TechnicalException("Módulo desconhecido no struts.xml: " + modulo);

		Set<Acao> exigidas = Acao.exigidasPor(metodo == null ? "execute" : metodo);

		for (Acao acao : exigidas)
			if (usuario.pode(moduloDaAction, acao))
				return true;

		return false;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}
}
