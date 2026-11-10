package br.com.kairos.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.business.RelatorioBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Action;
import br.com.kairos.infra.PlanilhaDeCompromissos;
import br.com.kairos.vo.CompromissoVo;

/**
 * Relatório de compromissos por período, em dois formatos (RN-RE-05):
 * na tela (HTML) e em planilha (.xlsx).
 */
public class RelatorioAction extends Action {

	private static final long serialVersionUID = 1L;

	/** Resultado do struts.xml que devolve um arquivo em vez de uma página. */
	private static final String EXCEL = "excel";
	private static final DateTimeFormatter DIA_MES_ANO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final RelatorioBusiness business = new RelatorioBusiness();

	/** Filtros da tela (RN-RE-02). */
	private LocalDate dataInicial;
	private LocalDate dataFinal;

	/** Linhas do relatório em tela. */
	private List<CompromissoVo> compromissos = new ArrayList<>();
	/** Verdadeiro depois de uma consulta bem-sucedida: a tela mostra a tabela, mesmo vazia. */
	private boolean gerado;

	/** Conteúdo e nome do arquivo .xlsx. Lidos pelo resultado "stream" do Struts. */
	private InputStream arquivo;
	private String nomeDoArquivo;

	/** Abre a tela só com os filtros. */
	public String abrir() {
		return SUCCESS;
	}

	/** Formato HTML: mostra o resultado na própria tela. */
	public String gerar() {
		try {
			compromissos = business.compromissosDoPeriodo(dataInicial, dataFinal);
			gerado = true;
		} catch (BusinessException e) {
			addActionError(e.getMessage());
		}
		return SUCCESS;
	}

	/** Formato Excel: devolve o arquivo para download. Filtro inválido volta à tela com a mensagem. */
	public String excel() {
		try {
			List<CompromissoVo> linhas = business.compromissosDoPeriodo(dataInicial, dataFinal);

			arquivo = new ByteArrayInputStream(PlanilhaDeCompromissos.gerar(linhas, dataInicial, dataFinal));
			nomeDoArquivo = "compromissos_" + dataInicial + "_a_" + dataFinal + ".xlsx";
			return EXCEL;

		} catch (BusinessException e) {
			addActionError(e.getMessage());
			return SUCCESS;
		}
	}

	public LocalDate getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(LocalDate dataInicial) {
		this.dataInicial = dataInicial;
	}

	public LocalDate getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(LocalDate dataFinal) {
		this.dataFinal = dataFinal;
	}

	public List<CompromissoVo> getCompromissos() {
		return compromissos;
	}

	/** Datas no formato da tela (dd/MM/aaaa), para o título do resultado. */
	public String getDataInicialFormatada() {
		return dataInicial == null ? "" : dataInicial.format(DIA_MES_ANO);
	}

	public String getDataFinalFormatada() {
		return dataFinal == null ? "" : dataFinal.format(DIA_MES_ANO);
	}

	public boolean isGerado() {
		return gerado;
	}

	public InputStream getArquivo() {
		return arquivo;
	}

	public String getNomeDoArquivo() {
		return nomeDoArquivo;
	}
}
