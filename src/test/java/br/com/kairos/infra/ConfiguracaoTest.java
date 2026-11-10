package br.com.kairos.infra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

/**
 * Leitura do arquivo .env, no mesmo formato que o docker compose entende.
 */
public class ConfiguracaoTest {

	private static final char BARRA = (char) 92;

	@Test
	public void leChaveEValor() {
		Map<String, String> valores = Configuracao.interpretar(Arrays.asList("DB_HOST=localhost", "DB_PORT = 3307 "));

		assertEquals("localhost", valores.get("DB_HOST"));
		assertEquals("3307", valores.get("DB_PORT"));
	}

	@Test
	public void ignoraComentarioLinhaVaziaELinhaSemIgual() {
		Map<String, String> valores = Configuracao.interpretar(Arrays.asList("# comentario", "", "semIgual", "A=1"));

		assertEquals(1, valores.size());
		assertFalse(valores.containsKey("semIgual"));
	}

	@Test
	public void valorMantemBarraInvertidaEIgual() {
		// A senha chega ao MySQL exatamente como o docker compose a leu
		String senha = "ab" + BARRA + "cd=ef";

		Map<String, String> valores = Configuracao.interpretar(Arrays.asList("DB_PASSWORD=" + senha));

		assertEquals(senha, valores.get("DB_PASSWORD"));
	}
}
