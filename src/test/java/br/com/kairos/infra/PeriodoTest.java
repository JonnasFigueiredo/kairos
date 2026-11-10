package br.com.kairos.infra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalTime;

import org.junit.Test;

/**
 * RN-CO-05: manhã de 00:00 a 11:59, tarde de 12:00 a 23:59, ambos qualquer horário.
 */
public class PeriodoTest {

	@Test
	public void manhaAceitaAteOnzeECinquentaENove() {
		assertTrue(Periodo.MANHA.aceita(LocalTime.of(0, 0)));
		assertTrue(Periodo.MANHA.aceita(LocalTime.of(8, 30)));
		assertTrue(Periodo.MANHA.aceita(LocalTime.of(11, 59)));
	}

	@Test
	public void manhaRecusaDoMeioDiaEmDiante() {
		assertFalse(Periodo.MANHA.aceita(LocalTime.of(12, 0)));
		assertFalse(Periodo.MANHA.aceita(LocalTime.of(15, 30)));
	}

	@Test
	public void tardeAceitaDoMeioDiaEmDiante() {
		assertTrue(Periodo.TARDE.aceita(LocalTime.of(12, 0)));
		assertTrue(Periodo.TARDE.aceita(LocalTime.of(23, 59)));
	}

	@Test
	public void tardeRecusaAntesDoMeioDia() {
		assertFalse(Periodo.TARDE.aceita(LocalTime.of(11, 59)));
		assertFalse(Periodo.TARDE.aceita(LocalTime.of(0, 0)));
	}

	@Test
	public void ambosAceitaQualquerHorario() {
		assertTrue(Periodo.AMBOS.aceita(LocalTime.of(0, 0)));
		assertTrue(Periodo.AMBOS.aceita(LocalTime.of(11, 59)));
		assertTrue(Periodo.AMBOS.aceita(LocalTime.of(12, 0)));
		assertTrue(Periodo.AMBOS.aceita(LocalTime.of(23, 59)));
	}

	@Test
	public void porCodigoEncontraOsTresPeriodos() {
		assertEquals(Periodo.MANHA, Periodo.porCodigo(1));
		assertEquals(Periodo.TARDE, Periodo.porCodigo(2));
		assertEquals(Periodo.AMBOS, Periodo.porCodigo(3));
	}

	@Test
	public void porCodigoDevolveNuloParaCodigoInvalido() {
		assertNull(Periodo.porCodigo(null));
		assertNull(Periodo.porCodigo(0));
		assertNull(Periodo.porCodigo(4));
	}
}
