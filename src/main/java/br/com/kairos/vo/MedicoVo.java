package br.com.kairos.vo;

import java.io.Serializable;

/**
 * Dados profissionais de um usuário que é médico (RN-ME-01). Ter este objeto
 * preenchido é o que faz da conta um médico. Vai para a sessão junto com o usuário.
 */
public class MedicoVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String crm;
	private String ufCrm;
	private String especialidade;

	public String getCrm() {
		return crm;
	}

	public void setCrm(String crm) {
		this.crm = crm;
	}

	public String getUfCrm() {
		return ufCrm;
	}

	public void setUfCrm(String ufCrm) {
		this.ufCrm = ufCrm;
	}

	public String getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(String especialidade) {
		this.especialidade = especialidade;
	}

	/** Como o CRM aparece na assinatura do resultado: "CRM 12345/SP". */
	public String getCrmFormatado() {
		return "CRM " + crm + "/" + ufCrm;
	}
}
