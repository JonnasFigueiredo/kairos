package br.com.kairos.infra.ia;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.kairos.infra.ia.HttpJson.Resposta;

/**
 * Adaptador para provedores com API "compatível com OpenAI" (POST /chat/completions):
 * OpenAI, Gemini, DeepSeek e Llama via Ollama. Um adaptador só serve a todos, mudando
 * endereço, chave e formato. Pede JSON pelo esquema quando o provedor aceita; senão,
 * pede "objeto JSON" e o esquema vai descrito nas instruções.
 */
class OpenAiCompativelAdaptador implements ModeloDeLinguagem {

	private final String nome;
	private final String urlBase;
	private final String chave;
	private final String modelo;
	private final boolean comEsquema;
	private final int tempoMaximoMs;

	/** @param chave nula = sem cabeçalho de autorização (Ollama local) */
	OpenAiCompativelAdaptador(String nome, String urlBase, String chave, String modelo, boolean comEsquema, int tempoMaximoMs) {
		this.nome = nome;
		this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
		this.chave = chave;
		this.modelo = modelo;
		this.comEsquema = comEsquema;
		this.tempoMaximoMs = tempoMaximoMs;
	}

	@Override
	public String descricao() {
		return nome + " · " + modelo;
	}

	@Override
	public RespostaModelo responder(PedidoModelo pedido) {
		Map<String, String> cabecalhos = new LinkedHashMap<>();
		if (chave != null)
			cabecalhos.put("Authorization", "Bearer " + chave);

		Resposta resposta = HttpJson.post(urlBase + "/chat/completions", cabecalhos, corpo(pedido), tempoMaximoMs);

		if (!resposta.sucesso())
			throw new FalhaDoModelo(nome + " respondeu HTTP " + resposta.codigo + ": "
					+ HttpJson.texto(HttpJson.caminho(resposta.corpo, "error", "message")));

		Object escolhas = resposta.corpo == null ? null : resposta.corpo.get("choices");
		Object escolha = escolhas instanceof List && !((List<?>) escolhas).isEmpty() ? ((List<?>) escolhas).get(0) : null;
		if (escolha == null)
			throw new FalhaDoModelo(nome + ": resposta sem choices");

		if ("content_filter".equals(HttpJson.caminho(escolha, "finish_reason")))
			throw new FalhaDoModelo(nome + ": resposta bloqueada pelo filtro do provedor");

		String texto = HttpJson.texto(HttpJson.caminho(escolha, "message", "content"));
		if (texto == null || texto.trim().isEmpty())
			throw new FalhaDoModelo(nome + ": resposta sem texto");

		return new RespostaModelo(texto, descricao(),
				HttpJson.inteiro(HttpJson.caminho(resposta.corpo, "usage", "prompt_tokens")),
				HttpJson.inteiro(HttpJson.caminho(resposta.corpo, "usage", "completion_tokens")));
	}

	/** Separado para os testes conferirem o pedido sem chamar a API. */
	Map<String, Object> corpo(PedidoModelo pedido) {
		Map<String, Object> sistema = new LinkedHashMap<>();
		sistema.put("role", "system");
		sistema.put("content", pedido.getInstrucoes());

		Map<String, Object> usuario = new LinkedHashMap<>();
		usuario.put("role", "user");
		usuario.put("content", pedido.getContexto());

		Map<String, Object> formato = new LinkedHashMap<>();
		if (comEsquema) {
			Map<String, Object> esquema = new LinkedHashMap<>();
			esquema.put("name", "analise");
			esquema.put("strict", true);
			esquema.put("schema", pedido.getEsquema());
			formato.put("type", "json_schema");
			formato.put("json_schema", esquema);
		} else {
			formato.put("type", "json_object");
		}

		Map<String, Object> corpo = new LinkedHashMap<>();
		corpo.put("model", modelo);
		corpo.put("messages", Arrays.asList(sistema, usuario));
		corpo.put("response_format", formato);
		return corpo;
	}
}
