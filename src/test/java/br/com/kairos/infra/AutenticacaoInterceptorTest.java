package br.com.kairos.infra;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.mock.MockActionInvocation;

import br.com.kairos.vo.UsuarioVo;

/**
 * RN-AC-01: sem usuário na sessão, a Action não chega a ser chamada.
 */
public class AutenticacaoInterceptorTest {

	private static final String RESULTADO_DA_ACTION = "success";

	private final AutenticacaoInterceptor interceptor = new AutenticacaoInterceptor();

	@Test
	public void rnAc01_semUsuarioNaSessaoVaiParaOLogin() throws Exception {
		Map<String, Object> sessaoVazia = new HashMap<>();

		assertEquals(AutenticacaoInterceptor.LOGIN, interceptor.intercept(chamadaCom(sessaoVazia)));
	}

	@Test
	public void rnAc01_semSessaoVaiParaOLogin() throws Exception {
		assertEquals(AutenticacaoInterceptor.LOGIN, interceptor.intercept(chamadaCom(null)));
	}

	@Test
	public void rnAc01_comUsuarioNaSessaoSegueParaAAction() throws Exception {
		Map<String, Object> sessao = new HashMap<>();
		sessao.put(AutenticacaoInterceptor.USUARIO, new UsuarioVo());

		assertEquals(RESULTADO_DA_ACTION, interceptor.intercept(chamadaCom(sessao)));
	}

	/** Chamada de mentira: se o interceptor deixar passar, devolve o resultado da Action. */
	private MockActionInvocation chamadaCom(Map<String, Object> sessao) {
		ActionContext contexto = new ActionContext(new HashMap<String, Object>());
		contexto.setSession(sessao);

		MockActionInvocation chamada = new MockActionInvocation();
		chamada.setInvocationContext(contexto);
		chamada.setResultCode(RESULTADO_DA_ACTION);
		return chamada;
	}
}
