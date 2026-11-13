package br.com.kairos.infra;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ActionTest {

	private static final String HOST = "localhost:8082";

	@Test
	public void aceitaPedidoDoProprioServidor() {
		assertTrue(Action.mesmaOrigem("http://localhost:8082", null, HOST));
		assertTrue(Action.mesmaOrigem(null, "http://localhost:8082/kairos/novoFuncionarios.action", HOST));
		assertTrue(Action.mesmaOrigem("http://LOCALHOST:8082", null, HOST));
	}

	@Test
	public void recusaPedidoDeOutroSite() {
		assertFalse(Action.mesmaOrigem("http://malicioso.example", null, HOST));
		assertFalse(Action.mesmaOrigem(null, "http://malicioso.example/kairos", HOST));
		assertFalse(Action.mesmaOrigem("http://localhost:9999", null, HOST));
		// Origin manda mais que o Referer
		assertFalse(Action.mesmaOrigem("http://malicioso.example", "http://localhost:8082/kairos", HOST));
	}

	@Test
	public void recusaOrigemQueNaoEhEndereco() {
		assertFalse(Action.mesmaOrigem("null", null, HOST));
		assertFalse(Action.mesmaOrigem("http://exemplo com espaço", null, HOST));
		assertFalse(Action.mesmaOrigem("http://localhost:8082", null, null));
	}

	@Test
	public void semOrigemInformadaSegue() {
		assertTrue(Action.mesmaOrigem(null, null, HOST));
		assertTrue(Action.mesmaOrigem("", " ", HOST));
	}
}
