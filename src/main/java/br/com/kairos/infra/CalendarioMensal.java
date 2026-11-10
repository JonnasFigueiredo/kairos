package br.com.kairos.infra;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import br.com.kairos.vo.CompromissoVo;

/**
 * Grade de um mês para a tela de compromissos: semanas de domingo a sábado,
 * com os compromissos de cada dia.
 *
 * É só apresentação. Recebe a lista já consultada e a distribui pelos dias;
 * não consulta o banco nem aplica regra de negócio.
 */
public final class CalendarioMensal {

	private static final Locale BRASIL = new Locale("pt", "BR");
	private static final DateTimeFormatter MES_E_ANO = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", BRASIL);
	private static final DateTimeFormatter DIA_MES_ANO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final int DIAS_DA_SEMANA = 7;

	private final YearMonth mes;
	private final List<List<Dia>> semanas = new ArrayList<>();

	/**
	 * @param hoje recebido de fora, e não lido do relógio aqui dentro, para o teste poder fixar a data
	 */
	public CalendarioMensal(YearMonth mes, List<CompromissoVo> compromissos, LocalDate hoje) {
		this.mes = mes;

		// A grade começa no domingo da semana em que cai o dia 1.
		// getValue() devolve 1 (segunda) a 7 (domingo); o resto por 7 dá quantos dias voltar.
		LocalDate primeiroDoMes = mes.atDay(1);
		LocalDate dia = primeiroDoMes.minusDays(primeiroDoMes.getDayOfWeek().getValue() % DIAS_DA_SEMANA);
		LocalDate ultimoDoMes = mes.atEndOfMonth();

		while (!dia.isAfter(ultimoDoMes)) {
			List<Dia> semana = new ArrayList<>();
			for (int i = 0; i < DIAS_DA_SEMANA; i++) {
				boolean doMes = YearMonth.from(dia).equals(mes);
				// Dia do mês vizinho aparece na grade só para completar a semana, sem compromissos
				List<CompromissoVo> doDia = doMes ? compromissosDe(dia, compromissos) : Collections.<CompromissoVo>emptyList();
				semana.add(new Dia(dia, doMes, dia.equals(hoje), doDia));
				dia = dia.plusDays(1);
			}
			semanas.add(semana);
		}
	}

	/**
	 * Lê o mês que veio da URL, no formato aaaa-mm. Texto vazio, inválido ou
	 * fora da faixa de anos que o banco aceita vira o mês padrão.
	 */
	public static YearMonth mesDe(String texto, YearMonth padrao) {
		if (texto == null || texto.trim().isEmpty())
			return padrao;

		try {
			YearMonth mes = YearMonth.parse(texto.trim());
			return mes.getYear() < 1 || mes.getYear() > 9999 ? padrao : mes;
		} catch (DateTimeParseException e) {
			return padrao;
		}
	}

	private static List<CompromissoVo> compromissosDe(LocalDate dia, List<CompromissoVo> compromissos) {
		List<CompromissoVo> doDia = new ArrayList<>();
		for (CompromissoVo compromisso : compromissos)
			if (dia.equals(compromisso.getData()))
				doDia.add(compromisso);
		return doDia;
	}

	/** No formato da URL: aaaa-mm. */
	public String getMes() {
		return mes.toString();
	}

	public String getMesAnterior() {
		return mes.minusMonths(1).toString();
	}

	public String getMesSeguinte() {
		return mes.plusMonths(1).toString();
	}

	/** Título da grade: "Novembro de 2026". */
	public String getTitulo() {
		String titulo = mes.format(MES_E_ANO);
		return titulo.substring(0, 1).toUpperCase(BRASIL) + titulo.substring(1);
	}

	public List<List<Dia>> getSemanas() {
		return semanas;
	}

	/** Uma casa da grade. */
	public static final class Dia {

		private final LocalDate data;
		private final boolean doMes;
		private final boolean hoje;
		private final List<CompromissoVo> compromissos;

		Dia(LocalDate data, boolean doMes, boolean hoje, List<CompromissoVo> compromissos) {
			this.data = data;
			this.doMes = doMes;
			this.hoje = hoje;
			this.compromissos = compromissos;
		}

		public LocalDate getData() {
			return data;
		}

		public String getDataFormatada() {
			return data.format(DIA_MES_ANO);
		}

		public int getNumero() {
			return data.getDayOfMonth();
		}

		/** Falso para os dias do mês vizinho que completam a primeira e a última semana. */
		public boolean isDoMes() {
			return doMes;
		}

		public boolean isHoje() {
			return hoje;
		}

		public List<CompromissoVo> getCompromissos() {
			return compromissos;
		}
	}
}
