package br.com.kairos.infra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import br.com.kairos.vo.CompromissoVo;

/**
 * Gera a planilha em memória e abre de novo com o POI para conferir o conteúdo.
 */
public class PlanilhaDeCompromissosTest {

	private static final LocalDate DIA_16 = LocalDate.of(2026, 11, 16);
	private static final LocalDate DIA_18 = LocalDate.of(2026, 11, 18);

	@Test
	public void rnRe04_cabecalhoTemAsSeisColunasDoRelatorio() throws IOException {
		Sheet aba = abrir(PlanilhaDeCompromissos.gerar(Collections.<CompromissoVo>emptyList(), DIA_16, DIA_18));

		assertEquals("Compromissos de 16/11/2026 a 18/11/2026", aba.getRow(0).getCell(0).getStringCellValue());

		Row cabecalho = aba.getRow(2);
		assertEquals("Código do funcionário", cabecalho.getCell(0).getStringCellValue());
		assertEquals("Nome do funcionário", cabecalho.getCell(1).getStringCellValue());
		assertEquals("Código da agenda", cabecalho.getCell(2).getStringCellValue());
		assertEquals("Nome da agenda", cabecalho.getCell(3).getStringCellValue());
		assertEquals("Data do compromisso", cabecalho.getCell(4).getStringCellValue());
		assertEquals("Hora do compromisso", cabecalho.getCell(5).getStringCellValue());
	}

	@Test
	public void cadaCompromissoViraUmaLinhaComOsSeisValores() throws IOException {
		List<CompromissoVo> compromissos = Arrays.asList(
				compromisso(1L, "João", 1L, "Exames clínicos", DIA_16, LocalTime.of(8, 30)),
				compromisso(2L, "Maria", 2L, "Treinamentos", DIA_18, LocalTime.of(12, 0)));

		Sheet aba = abrir(PlanilhaDeCompromissos.gerar(compromissos, DIA_16, DIA_18));

		Row primeira = aba.getRow(3);
		assertEquals(1, (long) primeira.getCell(0).getNumericCellValue());
		assertEquals("João", primeira.getCell(1).getStringCellValue());
		assertEquals(1, (long) primeira.getCell(2).getNumericCellValue());
		assertEquals("Exames clínicos", primeira.getCell(3).getStringCellValue());
		assertEquals(DIA_16, primeira.getCell(4).getLocalDateTimeCellValue().toLocalDate());

		Row segunda = aba.getRow(4);
		assertEquals("Maria", segunda.getCell(1).getStringCellValue());
		// Meio-dia é metade do dia: 0,5
		assertEquals(0.5, segunda.getCell(5).getNumericCellValue(), 0.00001);

		assertNull("não deve haver terceira linha de dados", aba.getRow(5));
	}

	@Test
	public void listaVaziaGeraSoOCabecalho() throws IOException {
		Sheet aba = abrir(PlanilhaDeCompromissos.gerar(Collections.<CompromissoVo>emptyList(), DIA_16, DIA_18));

		assertNull(aba.getRow(3));
	}

	private Sheet abrir(byte[] conteudo) throws IOException {
		try (Workbook arquivo = new XSSFWorkbook(new ByteArrayInputStream(conteudo))) {
			return arquivo.getSheetAt(0);
		}
	}

	private CompromissoVo compromisso(Long codigoFuncionario, String nomeFuncionario, Long codigoAgenda,
			String nomeAgenda, LocalDate data, LocalTime horario) {
		CompromissoVo compromisso = new CompromissoVo();
		compromisso.setCodigoFuncionario(codigoFuncionario);
		compromisso.setNomeFuncionario(nomeFuncionario);
		compromisso.setCodigoAgenda(codigoAgenda);
		compromisso.setNomeAgenda(nomeAgenda);
		compromisso.setData(data);
		compromisso.setHorario(horario);
		return compromisso;
	}
}
