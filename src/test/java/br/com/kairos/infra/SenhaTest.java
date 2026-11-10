package br.com.kairos.infra;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * RN-AC-03: a senha é guardada só como resultado do PBKDF2, com sal.
 */
public class SenhaTest {

	@Test
	public void rnAc03_senhaCertaConfere() {
		String guardada = Senha.gerar("segredo");

		assertTrue(Senha.confere("segredo", guardada));
	}

	@Test
	public void rnAc03_senhaErradaNaoConfere() {
		String guardada = Senha.gerar("segredo");

		assertFalse(Senha.confere("Segredo", guardada));
		assertFalse(Senha.confere("segredo ", guardada));
		assertFalse(Senha.confere("", guardada));
	}

	@Test
	public void rnAc03_oTextoGuardadoNaoContemASenha() {
		String guardada = Senha.gerar("segredo");

		assertFalse(guardada.contains("segredo"));
		assertTrue(guardada.startsWith("pbkdf2$" + Senha.ITERACOES + "$"));
	}

	@Test
	public void rnAc03_mesmaSenhaGeraTextosDiferentesPorCausaDoSal() {
		String primeira = Senha.gerar("segredo");
		String segunda = Senha.gerar("segredo");

		assertNotEquals(primeira, segunda);
		assertTrue(Senha.confere("segredo", primeira));
		assertTrue(Senha.confere("segredo", segunda));
	}

	@Test
	public void textoGuardadoForaDoFormatoNaoConfereENaoDaErro() {
		assertFalse(Senha.confere("segredo", null));
		assertFalse(Senha.confere(null, Senha.gerar("segredo")));
		assertFalse(Senha.confere("segredo", ""));
		assertFalse(Senha.confere("segredo", "segredo"));
		assertFalse(Senha.confere("segredo", "pbkdf2$abc$AAAA$AAAA"));
		assertFalse(Senha.confere("segredo", "pbkdf2$1000$***$AAAA"));
		assertFalse(Senha.confere("segredo", "md5$1000$AAAA$AAAA"));
	}
}
