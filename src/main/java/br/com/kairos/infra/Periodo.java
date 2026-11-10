package br.com.kairos.infra;

import java.time.LocalTime;

/**
 * Período em que uma agenda atende (RN-AG-03).
 *
 * É um enum: a lista de valores possíveis é fixa e conhecida pelo código.
 * O banco guarda só o código (1, 2 ou 3); a descrição aparece na tela.
 */
public enum Periodo {

	MANHA(1, "Manhã"),
	TARDE(2, "Tarde"),
	AMBOS(3, "Ambos");

	/** RN-CO-05: a manhã vai até 11:59 e a tarde começa às 12:00. */
	private static final LocalTime MEIO_DIA = LocalTime.NOON;

	private final int codigo;
	private final String descricao;

	Periodo(int codigo, String descricao) {
		this.codigo = codigo;
		this.descricao = descricao;
	}

	/** Devolve null se o código for nulo ou não existir. */
	public static Periodo porCodigo(Integer codigo) {
		if (codigo == null)
			return null;

		for (Periodo periodo : values())
			if (periodo.codigo == codigo)
				return periodo;

		return null;
	}

	/** RN-CO-04 e RN-CO-05: diz se um horário cabe neste período. */
	public boolean aceita(LocalTime horario) {
		switch (this) {
			case MANHA:
				return horario.isBefore(MEIO_DIA);
			case TARDE:
				return !horario.isBefore(MEIO_DIA);
			default:
				return true;
		}
	}

	public int getCodigo() {
		return codigo;
	}

	public String getDescricao() {
		return descricao;
	}
}
