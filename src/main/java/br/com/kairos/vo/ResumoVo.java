package br.com.kairos.vo;

/**
 * Totais mostrados na página inicial.
 */
public class ResumoVo {

	private int totalFuncionarios;
	private int totalAgendas;
	private int totalCompromissos;

	public int getTotalFuncionarios() {
		return totalFuncionarios;
	}

	public void setTotalFuncionarios(int totalFuncionarios) {
		this.totalFuncionarios = totalFuncionarios;
	}

	public int getTotalAgendas() {
		return totalAgendas;
	}

	public void setTotalAgendas(int totalAgendas) {
		this.totalAgendas = totalAgendas;
	}

	public int getTotalCompromissos() {
		return totalCompromissos;
	}

	public void setTotalCompromissos(int totalCompromissos) {
		this.totalCompromissos = totalCompromissos;
	}
}
