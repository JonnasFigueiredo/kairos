package br.com.kairos.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import br.com.kairos.exception.BusinessException;

/**
 * Regras do login que não dependem do banco.
 */
public class AutenticacaoBusinessTest {

	private final AutenticacaoBusiness business = new AutenticacaoBusiness();

	@Test
	public void rnAc02_usuarioVazioERecusado() {
		assertRecusado(null, "senha", "Informe o usuário.");
		assertRecusado("   ", "senha", "Informe o usuário.");
	}

	@Test
	public void rnAc02_senhaVaziaERecusada() {
		assertRecusado("admin", null, "Informe a senha.");
		assertRecusado("admin", "", "Informe a senha.");
	}

	@Test
	public void senhaSoComEspacosPassaDaValidacao() {
		// Espaço pode fazer parte da senha: quem decide se confere é o cálculo, não o trim
		business.validar("admin", "   ");
	}

	private void assertRecusado(String login, String senha, String mensagemEsperada) {
		try {
			business.validar(login, senha);
			fail("Deveria recusar: [" + login + "]");
		} catch (BusinessException e) {
			assertEquals(mensagemEsperada, e.getMessage());
		}
	}
}
