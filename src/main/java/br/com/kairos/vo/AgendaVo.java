package br.com.kairos.vo;

import br.com.kairos.infra.Periodo;

/**
 * Dados de uma agenda.
 */
public class AgendaVo {

	/** Gerado pelo banco. Nulo enquanto a agenda não foi gravada. */
	private Long codigo;
	private String nome;
	/** 1 = Manhã, 2 = Tarde, 3 = Ambos. É o valor que o select envia e que o banco guarda. */
	private Integer codigoPeriodo;

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Integer getCodigoPeriodo() {
		return codigoPeriodo;
	}

	public void setCodigoPeriodo(Integer codigoPeriodo) {
		this.codigoPeriodo = codigoPeriodo;
	}

	/** Atalho de leitura: o período correspondente ao código, ou null. A tela usa periodo.descricao. */
	public Periodo getPeriodo() {
		return Periodo.porCodigo(codigoPeriodo);
	}
}
