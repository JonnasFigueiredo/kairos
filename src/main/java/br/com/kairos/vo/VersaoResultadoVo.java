package br.com.kairos.vo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import br.com.kairos.infra.Indicador;

/**
 * Versão de um resultado substituída por retificação (RN-AT-03). Só leitura depois de gravada.
 */
public class VersaoResultadoVo {

	private static final DateTimeFormatter DIA_MES_ANO_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private Long codigo;
	private Long codigoResultado;
	private int versao;
	private String indicador;
	private String achados;
	private String parecer;
	private String assinatura;
	private LocalDateTime liberadoEm;
	private String motivo;
	private String retificadoPor;
	private LocalDateTime retificadoEm;

	public String getIndicadorDescricao() {
		Indicador valor = Indicador.porNome(indicador);
		return valor == null ? "" : valor.getDescricao();
	}

	public String getLiberadoEmFormatado() {
		return liberadoEm == null ? "" : liberadoEm.format(DIA_MES_ANO_HORA);
	}

	public String getRetificadoEmFormatado() {
		return retificadoEm == null ? "" : retificadoEm.format(DIA_MES_ANO_HORA);
	}

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public Long getCodigoResultado() {
		return codigoResultado;
	}

	public void setCodigoResultado(Long codigoResultado) {
		this.codigoResultado = codigoResultado;
	}

	public int getVersao() {
		return versao;
	}

	public void setVersao(int versao) {
		this.versao = versao;
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

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public String getRetificadoPor() {
		return retificadoPor;
	}

	public void setRetificadoPor(String retificadoPor) {
		this.retificadoPor = retificadoPor;
	}

	public LocalDateTime getRetificadoEm() {
		return retificadoEm;
	}

	public void setRetificadoEm(LocalDateTime retificadoEm) {
		this.retificadoEm = retificadoEm;
	}
}
