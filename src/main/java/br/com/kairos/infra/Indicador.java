package br.com.kairos.infra;

/**
 * Indicador do resultado do exame (RN-AT-02), com os mesmos valores do eSocial S-2220.
 * O banco guarda o nome; a descrição aparece na tela.
 */
public enum Indicador {

	NORMAL("Normal"),
	ALTERADO("Alterado"),
	ESTAVEL("Estável"),
	AGRAVAMENTO("Agravamento");

	private final String descricao;

	Indicador(String descricao) {
		this.descricao = descricao;
	}

	/** Devolve null se o nome for nulo, vazio ou desconhecido. */
	public static Indicador porNome(String nome) {
		if (nome == null)
			return null;

		for (Indicador indicador : values())
			if (indicador.name().equals(nome.trim()))
				return indicador;

		return null;
	}

	public String getDescricao() {
		return descricao;
	}
}
