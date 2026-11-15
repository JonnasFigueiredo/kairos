package br.com.kairos.infra.ia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.struts2.json.DefaultJSONWriter;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.infra.ia.ModeloDeLinguagem.FalhaDoModelo;

/**
 * POST de JSON com o HttpURLConnection do Java 8: sem cliente HTTP nem biblioteca
 * JSON nova. O JSON é o do plugin do Struts (struts2-json-plugin), já na pilha.
 */
final class HttpJson {

	private static final int TEMPO_DE_CONEXAO_MS = 15000;

	private HttpJson() {
		// só métodos estáticos
	}

	/** Resposta HTTP: código e corpo já interpretado (mapa de chaves e valores). */
	static final class Resposta {
		final int codigo;
		final Map<String, Object> corpo;
		final String texto;

		Resposta(int codigo, Map<String, Object> corpo, String texto) {
			this.codigo = codigo;
			this.corpo = corpo;
			this.texto = texto;
		}

		boolean sucesso() {
			return codigo >= 200 && codigo < 300;
		}
	}

	/**
	 * Envia o corpo como JSON e devolve a resposta interpretada. Erro de rede vira FalhaDoModelo.
	 *
	 * @param tempoMaximoMs quanto esperar pela resposta do modelo (pode levar um minuto)
	 */
	static Resposta post(String url, Map<String, String> cabecalhos, Map<String, Object> corpo, int tempoMaximoMs) {
		byte[] bytes = serializar(corpo).getBytes(StandardCharsets.UTF_8);
		HttpURLConnection conexao = null;

		try {
			conexao = (HttpURLConnection) new URL(url).openConnection();
			conexao.setRequestMethod("POST");
			conexao.setConnectTimeout(TEMPO_DE_CONEXAO_MS);
			conexao.setReadTimeout(tempoMaximoMs);
			conexao.setDoOutput(true);
			conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			conexao.setRequestProperty("Accept", "application/json");
			for (Map.Entry<String, String> cabecalho : cabecalhos.entrySet())
				conexao.setRequestProperty(cabecalho.getKey(), cabecalho.getValue());

			try (OutputStream saida = conexao.getOutputStream()) {
				saida.write(bytes);
			}

			int codigo = conexao.getResponseCode();
			InputStream entrada = codigo >= 400 ? conexao.getErrorStream() : conexao.getInputStream();
			String texto = entrada == null ? "" : ler(entrada);

			return new Resposta(codigo, interpretar(texto), texto);

		} catch (IOException e) {
			throw new FalhaDoModelo("Falha de rede ao chamar " + url + ": " + e.getMessage(), e);
		} finally {
			if (conexao != null)
				conexao.disconnect();
		}
	}

	static String serializar(Object valor) {
		try {
			// Fora do Struts ninguém injeta o writer: criado aqui. Só Map, List e tipos simples passam por aqui
			return new DefaultJSONWriter().write(valor);
		} catch (JSONException e) {
			throw new TechnicalException("O pedido para a IA não virou JSON", e);
		}
	}

	/** Devolve null quando o texto não é um objeto JSON. */
	@SuppressWarnings("unchecked")
	static Map<String, Object> interpretar(String texto) {
		if (texto == null || texto.trim().isEmpty())
			return null;
		try {
			Object valor = JSONUtil.deserialize(texto);
			return valor instanceof Map ? (Map<String, Object>) valor : null;
		} catch (JSONException e) {
			return null;
		}
	}

	private static String ler(InputStream entrada) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] bloco = new byte[8192];
		int lidos;
		while ((lidos = entrada.read(bloco)) != -1)
			buffer.write(bloco, 0, lidos);
		return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
	}

	/** Atalho para navegar no mapa: caminho("usage", "input_tokens"). Devolve null se faltar algo. */
	static Object caminho(Object raiz, String... chaves) {
		Object atual = raiz;
		for (String chave : chaves) {
			if (!(atual instanceof Map))
				return null;
			atual = ((Map<?, ?>) atual).get(chave);
		}
		return atual;
	}

	static Long inteiro(Object valor) {
		return valor instanceof Number ? ((Number) valor).longValue() : null;
	}

	static String texto(Object valor) {
		return valor == null ? null : valor.toString();
	}
}
