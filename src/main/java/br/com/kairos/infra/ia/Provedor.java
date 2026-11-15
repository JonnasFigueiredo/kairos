package br.com.kairos.infra.ia;

import java.util.Locale;

/**
 * Provedores de IA que o Kairos sabe usar (RN-IA-06). O Claude tem adaptador próprio;
 * os demais falam a API "compatível com OpenAI", então um adaptador só serve a todos.
 */
enum Provedor {
	NENHUM(null, null, null, false, false),
	FALSO("Falso", null, null, false, false),
	CLAUDE("Claude", ClaudeAdaptador.URL_PADRAO, ClaudeAdaptador.MODELO_PADRAO, true, false),
	OPENAI("OpenAI", "https://api.openai.com/v1", null, true, true),
	/** Google. gemini-3.5-flash é o modelo que respondeu com conta gratuita; troque por KAIROS_IA_MODELO_GEMINI. */
	GEMINI("Gemini", "https://generativelanguage.googleapis.com/v1beta/openai", "gemini-3.5-flash", true, true),
	DEEPSEEK("DeepSeek", "https://api.deepseek.com", "deepseek-chat", true, false),
	OLLAMA("Ollama", "http://localhost:11434/v1", null, false, false);

	final String nome;
	final String urlPadrao;
	/** Nulo = o modelo tem de vir da configuração (os nomes mudam com frequência nesses provedores). */
	final String modeloPadrao;
	final boolean exigeChave;
	/** Formato de JSON pedido na API compatível: esquema (mais rígido) ou só "objeto JSON". */
	final boolean aceitaEsquema;

	Provedor(String nome, String urlPadrao, String modeloPadrao, boolean exigeChave, boolean aceitaEsquema) {
		this.nome = nome;
		this.urlPadrao = urlPadrao;
		this.modeloPadrao = modeloPadrao;
		this.exigeChave = exigeChave;
		this.aceitaEsquema = aceitaEsquema;
	}

	/** Sufixo das variáveis deste provedor: KAIROS_IA_CHAVE_CLAUDE, KAIROS_IA_MODELO_GEMINI... */
	String sufixo() {
		return name();
	}

	/** Em branco = NENHUM. "google" = GEMINI. Nome desconhecido = nulo (a configuração avisa no log). */
	static Provedor de(String nome) {
		if (nome == null || nome.trim().isEmpty())
			return NENHUM;
		String limpo = nome.trim().toUpperCase(Locale.ROOT);
		if ("GOOGLE".equals(limpo))
			return GEMINI;
		try {
			return valueOf(limpo);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
