package br.com.kairos.infra;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * O que um usuário pode fazer em um módulo (RN-PE-01).
 *
 * Também sabe qual ação cada método de Action exige: o AutorizacaoInterceptor
 * usa isso para conferir a permissão antes de a Action rodar.
 */
public enum Acao {
	VER,
	INCLUIR,
	ALTERAR,
	EXCLUIR;

	private static final List<String> METODOS_DE_INCLUSAO = Arrays.asList("novo", "liberar");
	private static final List<String> METODOS_DE_ALTERACAO = Arrays.asList("editar", "retificar");
	/** "salvar" serve tanto para incluir quanto para alterar; a Action confere o caso exato. */
	private static final List<String> METODOS_DE_GRAVACAO = Collections.singletonList("salvar");
	private static final List<String> METODOS_DE_EXCLUSAO = Collections.singletonList("excluir");

	/** Ações que liberam o método: basta o usuário ter uma delas. Método desconhecido exige VER. */
	public static Set<Acao> exigidasPor(String metodo) {
		if (METODOS_DE_INCLUSAO.contains(metodo))
			return EnumSet.of(INCLUIR);
		if (METODOS_DE_ALTERACAO.contains(metodo))
			return EnumSet.of(ALTERAR);
		if (METODOS_DE_GRAVACAO.contains(metodo))
			return EnumSet.of(INCLUIR, ALTERAR);
		if (METODOS_DE_EXCLUSAO.contains(metodo))
			return EnumSet.of(EXCLUIR);
		return EnumSet.of(VER);
	}

	/** Devolve null se o nome não é uma ação. */
	public static Acao porNome(String nome) {
		for (Acao acao : values())
			if (acao.name().equals(nome))
				return acao;
		return null;
	}
}
