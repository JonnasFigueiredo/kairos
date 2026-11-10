package br.com.kairos.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import br.com.kairos.exception.BusinessException;
import br.com.kairos.vo.AgendaVo;

/**
 * Regras da agenda que não dependem do banco.
 */
public class AgendaBusinessTest {

	private final AgendaBusiness business = new AgendaBusiness();

	@Test
	public void rnAg02_nomeVazioERecusado() {
		assertRecusada(agenda("  ", 1), "Informe o nome da agenda.");
	}

	@Test
	public void rnAg02_periodoNaoInformadoERecusado() {
		assertRecusada(agenda("Exames", null), "Selecione o período disponível.");
	}

	@Test
	public void rnAg03_periodoForaDaListaERecusado() {
		assertRecusada(agenda("Exames", 4), "Selecione o período disponível.");
	}

	@Test
	public void agendaValidaPerdeOsEspacosDoNome() {
		AgendaVo agenda = agenda("  Exames  ", 3);

		business.validar(agenda);

		assertEquals("Exames", agenda.getNome());
	}

	private void assertRecusada(AgendaVo agenda, String mensagemEsperada) {
		try {
			business.validar(agenda);
			fail("Deveria recusar a agenda");
		} catch (BusinessException e) {
			assertEquals(mensagemEsperada, e.getMessage());
		}
	}

	private AgendaVo agenda(String nome, Integer codigoPeriodo) {
		AgendaVo agenda = new AgendaVo();
		agenda.setNome(nome);
		agenda.setCodigoPeriodo(codigoPeriodo);
		return agenda;
	}
}
