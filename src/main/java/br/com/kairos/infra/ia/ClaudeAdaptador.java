package br.com.kairos.infra.ia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.kairos.infra.ia.HttpJson.Resposta;

/**
 * Adaptador do Claude pela API Messages (POST /v1/messages), em HTTP puro.
 *
 * Pede a resposta em JSON validado pelo esquema (saída estruturada), mantém as
 * instruções em cache do lado da Anthropic (não mudam entre pedidos) e liga o
 * fallback do servidor: se o modelo recusar, a Anthropic tenta o modelo indicado
 * para a categoria da recusa (beta "server-side-fallback").
 */
class ClaudeAdaptador implements ModeloDeLinguagem {

	static final String URL_PADRAO = "https://api.anthropic.com/v1/messages";
	static final String MODELO_PADRAO = "claude-opus-5-5";
	static final String VERSAO_DA_API = "2026-10-21";
	static final String BETA_FALLBACK = "server-side-fallback-2026-07-01";
	/** Resposta sem streaming: cabe folgado e fica abaixo do tempo máximo do cliente HTTP. */
	static final int MAX_TOKENS = 16000;

	private final String url;
	private final String chave;
	private final String modelo;
	private final String esforco;
	private final int tempoMaximoMs;

	ClaudeAdaptador(String url, String chave, String modelo, String esforco, int tempoMaximoMs) {
		this.url = url;
		this.chave = chave;
		this.modelo = modelo;
		this.esforco = esforco;
		this.tempoMaximoMs = tempoMaximoMs;
	}

	@Override
	public String descricao() {
		return "Claude · " + modelo;
	}

	@Override
	public RespostaModelo responder(PedidoModelo pedido) {
		Map<String, String> cabecalhos = new LinkedHashMap<>();
		cabecalhos.put("x-api-key", chave);
		cabecalhos.put("anthropic-version", VERSAO_DA_API);
		cabecalhos.put("anthropic-beta", BETA_FALLBACK);

		Resposta resposta = HttpJson.post(url, cabecalhos, corpo(pedido), tempoMaximoMs);

		if (!resposta.sucesso())
			throw new FalhaDoModelo("Claude respondeu HTTP " + resposta.codigo + ": "
					+ HttpJson.texto(HttpJson.caminho(resposta.corpo, "error", "message")));

		if ("refusal".equals(HttpJson.caminho(resposta.corpo, "stop_reason")))
			throw new FalhaDoModelo("Claude recusou o pedido (stop_reason refusal)");

		String texto = textoDosBlocos(resposta.corpo == null ? null : resposta.corpo.get("content"));
		if (texto.trim().isEmpty())
			throw new FalhaDoModelo("Claude respondeu sem texto");

		return new RespostaModelo(texto, descricao(),
				HttpJson.inteiro(HttpJson.caminho(resposta.corpo, "usage", "input_tokens")),
				HttpJson.inteiro(HttpJson.caminho(resposta.corpo, "usage", "output_tokens")));
	}

	/** Separado para os testes conferirem o pedido sem chamar a API. */
	Map<String, Object> corpo(PedidoModelo pedido) {
		Map<String, Object> instrucoes = new LinkedHashMap<>();
		instrucoes.put("type", "text");
		instrucoes.put("text", pedido.getInstrucoes());
		// As instruções não mudam entre pedidos: ficam em cache (só ajuda quando passam do mínimo do modelo)
		instrucoes.put("cache_control", Collections.singletonMap("type", "ephemeral"));

		Map<String, Object> mensagem = new LinkedHashMap<>();
		mensagem.put("role", "user");
		mensagem.put("content", pedido.getContexto());

		Map<String, Object> formato = new LinkedHashMap<>();
		formato.put("type", "json_schema");
		formato.put("schema", pedido.getEsquema());

		Map<String, Object> saida = new LinkedHashMap<>();
		saida.put("effort", esforco);
		saida.put("format", formato);

		Map<String, Object> corpo = new LinkedHashMap<>();
		corpo.put("model", modelo);
		corpo.put("max_tokens", MAX_TOKENS);
		corpo.put("system", Collections.singletonList(instrucoes));
		corpo.put("messages", Collections.singletonList(mensagem));
		corpo.put("output_config", saida);
		// Se o modelo recusar, o servidor tenta o modelo recomendado para a categoria da recusa
		corpo.put("fallbacks", "default");
		return corpo;
	}

	/** Junta os blocos de texto da resposta; blocos de outro tipo (thinking, fallback) são ignorados. */
	static String textoDosBlocos(Object conteudo) {
		if (!(conteudo instanceof List))
			return "";

		List<String> textos = new ArrayList<>();
		for (Object bloco : (List<?>) conteudo)
			if ("text".equals(HttpJson.caminho(bloco, "type")))
				textos.add(HttpJson.texto(HttpJson.caminho(bloco, "text")));

		return String.join("", textos);
	}
}
