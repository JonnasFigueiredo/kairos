package br.com.kairos.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Montagem do parâmetro de LIKE: os curingas digitados pelo usuário são procurados ao pé da letra.
 */
public class DaoTest {

	/** A barra invertida, que o MySQL usa como escape no LIKE. */
	private static final char BARRA = (char) 92;

	@Test
	public void trechoComumGanhaCuringaNasDuasPontas() {
		assertEquals("%jo%", Dao.contendo("jo"));
	}

	@Test
	public void porcentoDigitadoViraCaractereLiteral() {
		assertEquals("%50" + BARRA + "%%", Dao.contendo("50%"));
	}

	@Test
	public void sublinhadoDigitadoViraCaractereLiteral() {
		assertEquals("%a" + BARRA + "_b%", Dao.contendo("a_b"));
	}

	@Test
	public void barraInvertidaDigitadaViraCaractereLiteral() {
		assertEquals("%a" + BARRA + BARRA + "b%", Dao.contendo("a" + BARRA + "b"));
	}
}
