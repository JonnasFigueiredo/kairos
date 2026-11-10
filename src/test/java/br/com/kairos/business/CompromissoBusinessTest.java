package br.com.kairos.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.Test;

import br.com.kairos.exception.BusinessException;
import br.com.kairos.vo.AgendaVo;
import br.com.kairos.vo.CompromissoVo;

/**
 * Regras do compromisso que não dependem do banco.
 */
public class CompromissoBusinessTest {

	private final CompromissoBusiness business = new CompromissoBusiness();

	@Test
	public void rnCo02_semFuncionarioERecusado() {
		CompromissoVo compromisso = completo();
		compromisso.setCodigoFuncionario(null);

		assertObrigatorio(compromisso, "Selecione o funcionário.");
	}

	@Test
	public void rnCo02_semAgendaERecusado() {
		CompromissoVo compromisso = completo();
		compromisso.setCodigoAgenda(null);

		assertObrigatorio(compromisso, "Selecione a agenda.");
	}

	@Test
	public void rnCo02_semDataERecusado() {
		CompromissoVo compromisso = completo();
		compromisso.setData(null);

		assertObrigatorio(compromisso, "Informe a data.");
	}

	@Test
	public void rnCo02_semHorarioERecusado() {
		CompromissoVo compromisso = completo();
		compromisso.setHorario(null);

		assertObrigatorio(compromisso, "Informe o horário.");
	}

	@Test
	public void rnCo02_compromissoCompletoPassa() {
		business.validarObrigatorios(completo());
	}

	@Test
	public void rnCo04_agendaDaManhaAceitaOnzeECinquentaENove() {
		business.validarHorario(agenda("Exames", 1), LocalTime.of(11, 59));
	}

	@Test
	public void rnCo04_agendaDaManhaRecusaMeioDia() {
		assertHorarioRecusado(agenda("Exames", 1), LocalTime.of(12, 0),
				"A agenda Exames atende só no período Manhã (até 11:59).");
	}

	@Test
	public void rnCo04_agendaDaTardeAceitaMeioDia() {
		business.validarHorario(agenda("Treinamentos", 2), LocalTime.of(12, 0));
	}

	@Test
	public void rnCo04_agendaDaTardeRecusaOnzeECinquentaENove() {
		assertHorarioRecusado(agenda("Treinamentos", 2), LocalTime.of(11, 59),
				"A agenda Treinamentos atende só no período Tarde (a partir das 12:00).");
	}

	@Test
	public void rnCo04_agendaDeAmbosAceitaQualquerHorario() {
		AgendaVo agenda = agenda("RH", 3);

		business.validarHorario(agenda, LocalTime.of(0, 0));
		business.validarHorario(agenda, LocalTime.of(12, 0));
		business.validarHorario(agenda, LocalTime.of(23, 59));
	}

	private void assertObrigatorio(CompromissoVo compromisso, String mensagemEsperada) {
		try {
			business.validarObrigatorios(compromisso);
			fail("Deveria recusar o compromisso");
		} catch (BusinessException e) {
			assertEquals(mensagemEsperada, e.getMessage());
		}
	}

	private void assertHorarioRecusado(AgendaVo agenda, LocalTime horario, String mensagemEsperada) {
		try {
			business.validarHorario(agenda, horario);
			fail("Deveria recusar o horário " + horario);
		} catch (BusinessException e) {
			assertEquals(mensagemEsperada, e.getMessage());
		}
	}

	private CompromissoVo completo() {
		CompromissoVo compromisso = new CompromissoVo();
		compromisso.setCodigoFuncionario(1L);
		compromisso.setCodigoAgenda(1L);
		compromisso.setData(LocalDate.of(2026, 11, 16));
		compromisso.setHorario(LocalTime.of(8, 30));
		return compromisso;
	}

	private AgendaVo agenda(String nome, Integer codigoPeriodo) {
		AgendaVo agenda = new AgendaVo();
		agenda.setNome(nome);
		agenda.setCodigoPeriodo(codigoPeriodo);
		return agenda;
	}
}
