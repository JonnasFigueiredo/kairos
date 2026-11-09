package br.com.kairos.vo;

/**
 * Dados de um funcionário. Trafega entre a tela, a Business e o Dao.
 */
public class FuncionarioVo {

	/** Gerado pelo banco. Nulo enquanto o funcionário não foi gravado. */
	private Long codigo;
	private String nome;

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
}
