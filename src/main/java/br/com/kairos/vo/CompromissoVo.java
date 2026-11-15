package br.com.kairos.vo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import br.com.kairos.infra.Periodo;

/**
 * Dados de um compromisso: um funcionário, em uma agenda, em uma data e horário.
 *
 * Os nomes e o período da agenda não são gravados na tabela compromisso:
 * vêm do JOIN nas consultas, para a tela e o relatório não precisarem buscar de novo.
 */
public class CompromissoVo {

	private static final DateTimeFormatter DIA_MES_ANO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter HORA_E_MINUTO = DateTimeFormatter.ofPattern("HH:mm");

	/** Gerado pelo banco. Nulo enquanto o compromisso não foi gravado. */
	private Long codigo;
	private Long codigoFuncionario;
	private Long codigoAgenda;
	private LocalDate data;
	private LocalTime horario;

	/** Só leitura: preenchidos pelas consultas. */
	private String nomeFuncionario;
	private String nomeAgenda;
	private Integer codigoPeriodoAgenda;
	/** Só na fila do médico (RN-AT-07): RASCUNHO, LIBERADO ou nulo quando ainda não há resultado. */
	private String situacaoResultado;

	public String getSituacaoResultado() {
		return situacaoResultado;
	}

	public void setSituacaoResultado(String situacaoResultado) {
		this.situacaoResultado = situacaoResultado;
	}

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public Long getCodigoFuncionario() {
		return codigoFuncionario;
	}

	public void setCodigoFuncionario(Long codigoFuncionario) {
		this.codigoFuncionario = codigoFuncionario;
	}

	public Long getCodigoAgenda() {
		return codigoAgenda;
	}

	public void setCodigoAgenda(Long codigoAgenda) {
		this.codigoAgenda = codigoAgenda;
	}

	public LocalDate getData() {
		return data;
	}

	public void setData(LocalDate data) {
		this.data = data;
	}

	public LocalTime getHorario() {
		return horario;
	}

	public void setHorario(LocalTime horario) {
		this.horario = horario;
	}

	public String getNomeFuncionario() {
		return nomeFuncionario;
	}

	public void setNomeFuncionario(String nomeFuncionario) {
		this.nomeFuncionario = nomeFuncionario;
	}

	public String getNomeAgenda() {
		return nomeAgenda;
	}

	public void setNomeAgenda(String nomeAgenda) {
		this.nomeAgenda = nomeAgenda;
	}

	public Integer getCodigoPeriodoAgenda() {
		return codigoPeriodoAgenda;
	}

	public void setCodigoPeriodoAgenda(Integer codigoPeriodoAgenda) {
		this.codigoPeriodoAgenda = codigoPeriodoAgenda;
	}

	/** Atalhos de leitura para a tela e o relatório. */
	public Periodo getPeriodoAgenda() {
		return Periodo.porCodigo(codigoPeriodoAgenda);
	}

	public String getDataFormatada() {
		return data == null ? "" : data.format(DIA_MES_ANO);
	}

	public String getHorarioFormatado() {
		return horario == null ? "" : horario.format(HORA_E_MINUTO);
	}
}
