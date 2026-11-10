package br.com.kairos.infra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.CompromissoVo;

/**
 * Monta a planilha .xlsx do relatório de compromissos, com o Apache POI.
 *
 * Único lugar do projeto que conhece o POI. Recebe a lista pronta e devolve
 * os bytes do arquivo; não consulta o banco nem aplica regra.
 */
public final class PlanilhaDeCompromissos {

	/** RN-RE-04: as colunas do relatório. */
	static final String[] TITULOS = {
			"Código do funcionário", "Nome do funcionário",
			"Código da agenda", "Nome da agenda",
			"Data do compromisso", "Hora do compromisso" };

	private static final DateTimeFormatter DIA_MES_ANO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final int SEGUNDOS_DO_DIA = 24 * 60 * 60;

	private PlanilhaDeCompromissos() {
		// só métodos estáticos
	}

	public static byte[] gerar(List<CompromissoVo> compromissos, LocalDate dataInicial, LocalDate dataFinal) {
		// XSSFWorkbook é o arquivo .xlsx em memória. O try fecha e libera a memória no fim.
		try (Workbook arquivo = new XSSFWorkbook();
				ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

			Sheet aba = arquivo.createSheet("Compromissos");

			CellStyle negrito = estiloNegrito(arquivo);
			CellStyle formatoData = estiloComFormato(arquivo, "dd/mm/yyyy");
			CellStyle formatoHora = estiloComFormato(arquivo, "hh:mm");

			// Linha 0: o período pedido
			Cell titulo = aba.createRow(0).createCell(0);
			titulo.setCellValue("Compromissos de " + dataInicial.format(DIA_MES_ANO)
					+ " a " + dataFinal.format(DIA_MES_ANO));
			titulo.setCellStyle(negrito);

			// Linha 2: os nomes das colunas
			Row cabecalho = aba.createRow(2);
			for (int coluna = 0; coluna < TITULOS.length; coluna++) {
				Cell celula = cabecalho.createCell(coluna);
				celula.setCellValue(TITULOS[coluna]);
				celula.setCellStyle(negrito);
			}

			// Da linha 3 em diante: um compromisso por linha
			int numeroDaLinha = 3;
			for (CompromissoVo compromisso : compromissos) {
				Row linha = aba.createRow(numeroDaLinha++);

				linha.createCell(0).setCellValue(compromisso.getCodigoFuncionario());
				linha.createCell(1).setCellValue(compromisso.getNomeFuncionario());
				linha.createCell(2).setCellValue(compromisso.getCodigoAgenda());
				linha.createCell(3).setCellValue(compromisso.getNomeAgenda());

				// Data e hora vão como valores de verdade, não como texto: o Excel consegue ordenar e filtrar
				Cell data = linha.createCell(4);
				data.setCellValue(compromisso.getData());
				data.setCellStyle(formatoData);

				// Para o Excel, hora é a fração do dia: 12:00 = 0,5
				Cell hora = linha.createCell(5);
				hora.setCellValue((double) compromisso.getHorario().toSecondOfDay() / SEGUNDOS_DO_DIA);
				hora.setCellStyle(formatoHora);
			}

			// Mantém o cabeçalho visível ao rolar e ajusta a largura das colunas ao conteúdo
			aba.createFreezePane(0, 3);
			for (int coluna = 0; coluna < TITULOS.length; coluna++)
				aba.autoSizeColumn(coluna);

			arquivo.write(saida);
			return saida.toByteArray();

		} catch (IOException e) {
			throw new TechnicalException("Erro ao gerar a planilha do relatório", e);
		}
	}

	private static CellStyle estiloNegrito(Workbook arquivo) {
		Font fonte = arquivo.createFont();
		fonte.setBold(true);

		CellStyle estilo = arquivo.createCellStyle();
		estilo.setFont(fonte);
		return estilo;
	}

	private static CellStyle estiloComFormato(Workbook arquivo, String formato) {
		CellStyle estilo = arquivo.createCellStyle();
		estilo.setDataFormat(arquivo.createDataFormat().getFormat(formato));
		return estilo;
	}
}
