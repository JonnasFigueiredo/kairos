package br.com.kairos.infra.ia;

import java.util.Map;

/**
 * Porta para o modelo de linguagem (RN-IA). O resto do sistema só conhece esta
 * interface; cada provedor (Claude, os compatíveis com OpenAI, o falso dos testes)
 * é um adaptador. Trocar de provedor é configuração, não código.
 */
public interface ModeloDeLinguagem {

	/** Ex.: "Claude · claude-opus-5-5". Vai na auditoria e na tela. */
	String descricao();

	/** @throws FalhaDoModelo se o provedor não responder, recusar ou devolver algo inutilizável */
	RespostaModelo responder(PedidoModelo pedido);

	/** O que vai para o modelo: o papel e as regras, os dados do caso (JSON) e o esquema da resposta. */
	final class PedidoModelo {
		private final String instrucoes;
		private final String contexto;
		private final Map<String, Object> esquema;

		public PedidoModelo(String instrucoes, String contexto, Map<String, Object> esquema) {
			this.instrucoes = instrucoes;
			this.contexto = contexto;
			this.esquema = esquema;
		}

		public String getInstrucoes() {
			return instrucoes;
		}

		public String getContexto() {
			return contexto;
		}

		public Map<String, Object> getEsquema() {
			return esquema;
		}
	}

	/** O que volta do modelo: o texto (JSON pedido) e os tokens gastos, quando o provedor informa. */
	final class RespostaModelo {
		private final String texto;
		private final String descricao;
		private final Long tokensEntrada;
		private final Long tokensSaida;

		public RespostaModelo(String texto, String descricao, Long tokensEntrada, Long tokensSaida) {
			this.texto = texto;
			this.descricao = descricao;
			this.tokensEntrada = tokensEntrada;
			this.tokensSaida = tokensSaida;
		}

		public String getTexto() {
			return texto;
		}

		public String getDescricao() {
			return descricao;
		}

		public Long getTokensEntrada() {
			return tokensEntrada;
		}

		public Long getTokensSaida() {
			return tokensSaida;
		}
	}

	/** O provedor falhou. A mensagem vai para o log e para a auditoria, nunca para a tela. */
	class FalhaDoModelo extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public FalhaDoModelo(String mensagem) {
			super(mensagem);
		}

		public FalhaDoModelo(String mensagem, Throwable causa) {
			super(mensagem, causa);
		}
	}
}
