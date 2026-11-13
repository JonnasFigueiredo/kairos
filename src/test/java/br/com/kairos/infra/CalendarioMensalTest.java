package br.com.kairos.infra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import br.com.kairos.infra.CalendarioMensal.Dia;
import br.com.kairos.vo.CompromissoVo;

/**
 * Confere a grade do mês: onde começa, onde termina e em que dia cada compromisso cai.
 */
public class CalendarioMensalTest {

	private static final YearMonth NOVEMBRO_2026 = YearMonth.of(2026, 11);
	private static final LocalDate HOJE = LocalDate.of(2026, 11, 10);
	private static final List<CompromissoVo> SEM_COMPROMISSOS = Collections.<CompromissoVo>emptyList();

	@Test
	public void mesQueComecaNoDomingoNaoTemDiaDoMesAnterior() {
		// 01/11/2026 é domingo
		CalendarioMensal calendario = new CalendarioMensal(NOVEMBRO_2026, SEM_COMPROMISSOS, HOJE);

		Dia primeiro = calendario.getSemanas().get(0).get(0);
		assertEquals(LocalDate.of(2026, 11, 1), primeiro.getData());
		assertTrue(primeiro.isDoMes());
		assertEquals(5, calendario.getSemanas().size());
	}

	@Test
	public void primeiraEUltimaSemanaSaoCompletadasComOsMesesVizinhos() {
		// 01/02/2024 é quinta e 29/02/2024 também (ano bissexto)
		CalendarioMensal calendario = new CalendarioMensal(YearMonth.of(2024, 2), SEM_COMPROMISSOS, HOJE);

		List<List<Dia>> semanas = calendario.getSemanas();
		assertEquals(5, semanas.size());
		for (List<Dia> semana : semanas)
			assertEquals(7, semana.size());

		Dia primeiro = semanas.get(0).get(0);
		assertEquals(LocalDate.of(2024, 1, 28), primeiro.getData());
		assertFalse(primeiro.isDoMes());

		assertEquals(LocalDate.of(2024, 2, 29), semanas.get(4).get(4).getData());

		Dia ultimo = semanas.get(4).get(6);
		assertEquals(LocalDate.of(2024, 3, 2), ultimo.getData());
		assertFalse(ultimo.isDoMes());
	}

	@Test
	public void compromissoApareceSoNoDiaDele() {
		List<CompromissoVo> compromissos = Arrays.asList(
				compromisso(1L, LocalDate.of(2026, 11, 20)),
				compromisso(2L, LocalDate.of(2026, 11, 20)),
				compromisso(3L, LocalDate.of(2026, 12, 2)));

		CalendarioMensal calendario = new CalendarioMensal(NOVEMBRO_2026, compromissos, HOJE);

		int total = 0;
		for (List<Dia> semana : calendario.getSemanas())
			for (Dia dia : semana) {
				total += dia.getCompromissos().size();
				if (dia.getData().equals(LocalDate.of(2026, 11, 20)))
					assertEquals(2, dia.getCompromissos().size());
			}

		// O de dezembro fica fora: o dia 02/12 aparece na última semana, mas sem compromissos
		assertEquals(2, total);
	}

	@Test
	public void soODiaDeHojeEMarcado() {
		CalendarioMensal calendario = new CalendarioMensal(NOVEMBRO_2026, SEM_COMPROMISSOS, HOJE);

		int marcados = 0;
		for (List<Dia> semana : calendario.getSemanas())
			for (Dia dia : semana)
				if (dia.isHoje()) {
					marcados++;
					assertEquals(HOJE, dia.getData());
				}

		assertEquals(1, marcados);
	}

	@Test
	public void navegacaoETituloDoMes() {
		CalendarioMensal janeiro = new CalendarioMensal(YearMonth.of(2027, 1), SEM_COMPROMISSOS, HOJE);

		assertEquals("2027-01", janeiro.getMes());
		assertEquals("2026-12", janeiro.getMesAnterior());
		assertEquals("2027-02", janeiro.getMesSeguinte());
		assertEquals("Janeiro de 2027", janeiro.getTitulo());
	}

	@Test
	public void mesInvalidoNaUrlViraOMesPadrao() {
		assertEquals(YearMonth.of(2026, 12), CalendarioMensal.mesDe("2026-12", NOVEMBRO_2026));

		assertEquals(NOVEMBRO_2026, CalendarioMensal.mesDe(null, NOVEMBRO_2026));
		assertEquals(NOVEMBRO_2026, CalendarioMensal.mesDe("", NOVEMBRO_2026));
		assertEquals(NOVEMBRO_2026, CalendarioMensal.mesDe("abc", NOVEMBRO_2026));
		assertEquals(NOVEMBRO_2026, CalendarioMensal.mesDe("2026-13", NOVEMBRO_2026));
		assertEquals(NOVEMBRO_2026, CalendarioMensal.mesDe("+10000-01", NOVEMBRO_2026));
		assertEquals(NOVEMBRO_2026, CalendarioMensal.mesDe("0999-12", NOVEMBRO_2026));
		assertEquals(YearMonth.of(1000, 1), CalendarioMensal.mesDe("1000-01", NOVEMBRO_2026));
	}

	private CompromissoVo compromisso(Long codigo, LocalDate data) {
		CompromissoVo compromisso = new CompromissoVo();
		compromisso.setCodigo(codigo);
		compromisso.setData(data);
		compromisso.setHorario(LocalTime.of(9, 0));
		return compromisso;
	}
}
