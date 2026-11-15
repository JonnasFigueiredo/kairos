package br.com.kairos.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.infra.Indicador;

/**
 * Resultado do atendimento de um compromisso (RN-AT-01). Um por compromisso.
 *
 * Os dados do compromisso, do funcionário e do médico não ficam na tabela resultado:
 * vêm do JOIN nas consultas, para a tela não precisar buscar de novo.
 */
public class ResultadoVo {

	public static final String RASCUNHO = "RASCUNHO";
	public static final String LIBERADO = "LIBERADO";

	private static final DateTimeFormatter DIA_MES_ANO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter DIA_MES_ANO_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private static final DateTimeFormatter HORA_E_MINUTO = DateTimeFormatter.ofPattern("HH:mm");

	/** Gerado pelo banco. Nulo enquanto o resultado não foi gravado. */
	private Long codigo;
	private Long codigoCompromisso;
	private Long codigoMedico;
	/** Nome do enum Indicador; nulo no rascunho incompleto. */
	private String indicador;
	private String achados;
	private String parecer;
	private String situacao = RASCUNHO;
	private int versao = 1;
	private String assinatura;
	private LocalDateTime liberadoEm;
	private LocalDateTime atualizadoEm;

	/** Só leitura: preenchidos pelas consultas. */
	private Long codigoFuncionario;
	private String nomeFuncionario;
	private String nomeAgenda;
	private LocalDate dataCompromisso;
	private LocalTime horarioCompromisso;
	private List<VersaoResultadoVo> versoes = new ArrayList<>();

	public boolean isLiberado() {
		return LIBERADO.equals(situacao);
	}

	public boolean isRascunho() {
		return RASCUNHO.equals(situacao);
	}

	public Indicador getIndicadorEnum() {
		return Indicador.porNome(indicador);
	}

	public String getIndicadorDescricao() {
		Indicador valor = getIndicadorEnum();
		return valor == null ? "" : valor.getDescricao();
	}

	public String getDataCompromissoFormatada() {
		return dataCompromisso == null ? "" : dataCompromisso.format(DIA_MES_ANO);
	}

	public String getHorarioCompromissoFormatado() {
		return horarioCompromisso == null ? "" : horarioCompromisso.format(HORA_E_MINUTO);
	}

	public String getLiberadoEmFormatado() {
		return liberadoEm == null ? "" : liberadoEm.format(DIA_MES_ANO_HORA);
	}

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public Long getCodigoCompromisso() {
		return codigoCompromisso;
	}

	public void setCodigoCompromisso(Long codigoCompromisso) {
		this.codigoCompromisso = codigoCompromisso;
	}

	public Long getCodigoMedico() {
		return codigoMedico;
	}

	public void setCodigoMedico(Long codigoMedico) {
		this.codigoMedico = codigoMedico;
	}

	public String getIndicador() {
		return indicador;
	}

	public void setIndicador(String indicador) {
		this.indicador = indicador;
	}

	public String getAchados() {
		return achados;
	}

	public void setAchados(String achados) {
		this.achados = achados;
	}

	public String getParecer() {
		return parecer;
	}

	public void setParecer(String parecer) {
		this.parecer = parecer;
	}

	public String getSituacao() {
		return situacao;
	}

	public void setSituacao(String situacao) {
		this.situacao = situacao;
	}

	public int getVersao() {
		return versao;
	}

	public void setVersao(int versao) {
		this.versao = versao;
	}

	public String getAssinatura() {
		return assinatura;
	}

	public void setAssinatura(String assinatura) {
		this.assinatura = assinatura;
	}

	public LocalDateTime getLiberadoEm() {
		return liberadoEm;
	}

	public void setLiberadoEm(LocalDateTime liberadoEm) {
		this.liberadoEm = liberadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(LocalDateTime atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	public Long getCodigoFuncionario() {
		return codigoFuncionario;
	}

	public void setCodigoFuncionario(Long codigoFuncionario) {
		this.codigoFuncionario = codigoFuncionario;
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

	public LocalDate getDataCompromisso() {
		return dataCompromisso;
	}

	public void setDataCompromisso(LocalDate dataCompromisso) {
		this.dataCompromisso = dataCompromisso;
	}

	public LocalTime getHorarioCompromisso() {
		return horarioCompromisso;
	}

	public void setHorarioCompromisso(LocalTime horarioCompromisso) {
		this.horarioCompromisso = horarioCompromisso;
	}

	public List<VersaoResultadoVo> getVersoes() {
		return versoes;
	}

	public void setVersoes(List<VersaoResultadoVo> versoes) {
		this.versoes = versoes;
	}
}
