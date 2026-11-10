package br.com.kairos.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDate;

import org.junit.Test;

import br.com.kairos.exception.BusinessException;

/**
 * Regras do relatório que não dependem do banco.
 */
public class RelatorioBusinessTest {

	private static final LocalDate DIA_16 = LocalDate.of(2026, 11, 16);
	private static final LocalDate DIA_18 = LocalDate.of(2026, 11, 18);

	private final RelatorioBusiness business = new RelatorioBusiness();

	@Test
	public void rnRe02_semDataInicialERecusado() {
		assertRecusado(null, DIA_18, "Informe a data inicial.");
	}

	@Test
	public void rnRe02_semDataFinalERecusado() {
		assertRecusado(DIA_16, null, "Informe a data final.");
	}

	@Test
	public void rnRe03_dataInicialMaiorQueAFinalERecusada() {
		assertRecusado(DIA_18, DIA_16, "A data inicial não pode ser maior que a data final.");
	}

	@Test
	public void periodoDeUmDiaSoEAceito() {
		business.validarPeriodo(DIA_16, DIA_16);
	}

	@Test
	public void periodoCrescenteEAceito() {
		business.validarPeriodo(DIA_16, DIA_18);
	}

	private void assertRecusado(LocalDate dataInicial, LocalDate dataFinal, String mensagemEsperada) {
		try {
			business.validarPeriodo(dataInicial, dataFinal);
			fail("Deveria recusar o período");
		} catch (BusinessException e) {
			assertEquals(mensagemEsperada, e.getMessage());
		}
	}
}
