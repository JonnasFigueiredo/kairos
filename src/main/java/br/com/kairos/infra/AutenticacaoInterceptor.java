package br.com.kairos.infra;

import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * Porteiro das telas (RN-AC-01): roda antes de cada Action protegida e só
 * deixa passar quem tem usuário na sessão.
 *
 * É um interceptor do Struts: o struts.xml o coloca na frente da pilha padrão.
 * Assim nenhuma Action precisa lembrar de conferir o login, e uma Action nova
 * já nasce protegida.
 */
public class AutenticacaoInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 1L;

	/** Nome com que o usuário fica guardado na sessão. O menu lê por ele. */
	public static final String USUARIO = "usuario";

	/** Resultado global do struts.xml que leva à tela de login. */
	public static final String LOGIN = "login";

	@Override
	public String intercept(ActionInvocation chamada) throws Exception {
		Map<String, Object> sessao = chamada.getInvocationContext().getSession();

		if (sessao == null || sessao.get(USUARIO) == null)
			return LOGIN;

		// invoke() segue para o próximo interceptor e, no fim, para a Action
		return chamada.invoke();
	}
}
