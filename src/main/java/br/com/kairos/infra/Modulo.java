package br.com.kairos.infra;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Módulos do sistema e as ações que cada um aceita (RN-PE-01).
 *
 * É a fonte da verdade da matriz de acessos: a tela de usuários monta as
 * linhas a partir daqui, e a Business recusa permissão que não exista.
 * Módulo "só de médico" (RN-ME-03) só pode ser liberado para conta com cadastro de médico.
 */
public enum Modulo {
	FUNCIONARIOS("Funcionários", false, Acao.VER, Acao.INCLUIR, Acao.ALTERAR, Acao.EXCLUIR),
	AGENDAS("Agendas", false, Acao.VER, Acao.INCLUIR, Acao.ALTERAR, Acao.EXCLUIR),
	COMPROMISSOS("Compromissos", false, Acao.VER, Acao.INCLUIR, Acao.ALTERAR, Acao.EXCLUIR),
	RELATORIO("Relatório", false, Acao.VER),
	/** Ler os resultados liberados pelos médicos. */
	RESULTADOS("Resultados (leitura)", false, Acao.VER),
	/** Fila e histórico (VER), lançar e liberar resultado (INCLUIR), retificar (ALTERAR). */
	ATENDIMENTO("Atendimento", true, Acao.VER, Acao.INCLUIR, Acao.ALTERAR),
	/** Usar a IA na tela do atendimento (VER = usar). */
	ASSISTENTE_IA("Assistente de IA (usar)", true, Acao.VER),
	ACESSOS("Usuários e acessos", false, Acao.VER, Acao.INCLUIR, Acao.ALTERAR);

	private final String descricao;
	private final boolean soMedico;
	private final Set<Acao> acoes;

	Modulo(String descricao, boolean soMedico, Acao primeira, Acao... outras) {
		this.descricao = descricao;
		this.soMedico = soMedico;
		this.acoes = Collections.unmodifiableSet(EnumSet.of(primeira, outras));
	}

	public String getDescricao() {
		return descricao;
	}

	public boolean isSoMedico() {
		return soMedico;
	}

	public Set<Acao> getAcoes() {
		return acoes;
	}

	public boolean aceita(Acao acao) {
		return acoes.contains(acao);
	}

	/** Texto guardado no banco e nas checkboxes da tela: MODULO:ACAO. */
	public String chave(Acao acao) {
		return name() + ":" + acao.name();
	}

	/** Devolve null se o nome não é um módulo. */
	public static Modulo porNome(String nome) {
		for (Modulo modulo : values())
			if (modulo.name().equals(nome))
				return modulo;
		return null;
	}
}
