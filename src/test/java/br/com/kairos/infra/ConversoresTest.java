package br.com.kairos.infra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.Test;

import com.opensymphony.xwork2.conversion.TypeConversionException;

/**
 * Conversores de data e hora usados pelo Struts nos campos das telas.
 */
public class ConversoresTest {

	private final LocalDateConverter data = new LocalDateConverter();
	private final LocalTimeConverter hora = new LocalTimeConverter();

	@Test
	public void dataNoFormatoDoCampoEConvertida() {
		assertEquals(LocalDate.of(2026, 11, 20), converterData("2026-11-20"));
	}

	@Test
	public void dataVaziaViraNulo() {
		assertNull(converterData(""));
		assertNull(converterData("   "));
	}

	@Test
	public void dataQueNaoExisteERecusada() {
		assertDataRecusada("2026-02-31");
		assertDataRecusada("31/02/2026");
		assertDataRecusada("abc");
	}

	@Test
	public void anoForaDaFaixaDoBancoERecusado() {
		assertDataRecusada("+10000-01-01");
		assertDataRecusada("0999-12-31");
	}

	@Test
	public void dataVoltaParaATelaNoFormatoDoCampo() {
		assertEquals("2026-11-20", data.convertToString(null, LocalDate.of(2026, 11, 20)));
		assertEquals("", data.convertToString(null, null));
	}

	@Test
	public void horaNoFormatoDoCampoEConvertida() {
		assertEquals(LocalTime.of(15, 30), converterHora("15:30"));
	}

	@Test
	public void rnCo05_segundosEFracoesSaoDescartados() {
		// Sem isso, 11:59:59.9 passaria como manhã e seria gravado como 12:00
		assertEquals(LocalTime.of(11, 59), converterHora("11:59:59.9"));
		assertEquals(LocalTime.of(11, 59), converterHora("11:59:30"));
	}

	@Test
	public void horaInvalidaERecusada() {
		try {
			converterHora("25:99");
			fail("Deveria recusar 25:99");
		} catch (TypeConversionException e) {
			// esperado
		}
	}

	@Test
	public void horaVoltaParaATelaComHoraEMinuto() {
		assertEquals("08:05", hora.convertToString(null, LocalTime.of(8, 5)));
		assertEquals("", hora.convertToString(null, null));
	}

	private Object converterData(String texto) {
		return data.convertFromString(null, new String[] { texto }, LocalDate.class);
	}

	private Object converterHora(String texto) {
		return hora.convertFromString(null, new String[] { texto }, LocalTime.class);
	}

	private void assertDataRecusada(String texto) {
		try {
			converterData(texto);
			fail("Deveria recusar a data: " + texto);
		} catch (TypeConversionException e) {
			// esperado
		}
	}
}
