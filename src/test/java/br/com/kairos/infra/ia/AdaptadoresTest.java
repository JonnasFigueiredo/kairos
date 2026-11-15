package br.com.kairos.infra.ia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import br.com.kairos.infra.ia.ModeloDeLinguagem.FalhaDoModelo;
import br.com.kairos.infra.ia.ModeloDeLinguagem.PedidoModelo;
import br.com.kairos.infra.ia.ModeloDeLinguagem.RespostaModelo;

/**
 * RN-IA-06: o pedido que cada adaptador monta e a troca pela reserva quando o principal falha.
 * Nada aqui chama a internet.
 */
public class AdaptadoresTest {

	private final PedidoModelo pedido = new PedidoModelo("instrucoes", "{\"caso\":1}", Instrucoes.ESQUEMA);

	@Test
	public void claudeMontaOPedidoDaApiMessages() {
		ClaudeAdaptador claude = new ClaudeAdaptador(ClaudeAdaptador.URL_PADRAO, "chave", "claude-opus-5-5", "medium", 1000);
		Map<String, Object> corpo = claude.corpo(pedido);

		assertEquals("claude-opus-5-5", corpo.get("model"));
		assertEquals(ClaudeAdaptador.MAX_TOKENS, corpo.get("max_tokens"));
		assertEquals("default", corpo.get("fallbacks"));

		Map<?, ?> sistema = (Map<?, ?>) ((List<?>) corpo.get("system")).get(0);
		assertEquals("instrucoes", sistema.get("text"));
		assertTrue(sistema.containsKey("cache_control"));

		Map<?, ?> saida = (Map<?, ?>) corpo.get("output_config");
		assertEquals("medium", saida.get("effort"));
		Map<?, ?> formato = (Map<?, ?>) saida.get("format");
		assertEquals("json_schema", formato.get("type"));
		assertEquals(Instrucoes.ESQUEMA, formato.get("schema"));

		assertEquals("Claude · claude-opus-5-5", claude.descricao());
	}

	@Test
	public void claudeJuntaSoOsBlocosDeTexto() {
		List<Object> blocos = Arrays.<Object>asList(
				HttpJson.interpretar("{\"type\":\"thinking\",\"thinking\":\"...\"}"),
				HttpJson.interpretar("{\"type\":\"text\",\"text\":\"{\\\"resumo\\\":\"}"),
				HttpJson.interpretar("{\"type\":\"text\",\"text\":\"\\\"ok\\\"}\"}"));

		assertEquals("{\"resumo\":\"ok\"}", ClaudeAdaptador.textoDosBlocos(blocos));
		assertEquals("", ClaudeAdaptador.textoDosBlocos(null));
	}

	@Test
	public void compativelComOpenAiPedeEsquemaOuObjetoJson() {
		OpenAiCompativelAdaptador comEsquema = new OpenAiCompativelAdaptador("OpenAI", "https://api.openai.com/v1/", "k", "gpt", true, 1000);
		Map<?, ?> formato = (Map<?, ?>) comEsquema.corpo(pedido).get("response_format");
		assertEquals("json_schema", formato.get("type"));
		assertEquals(Instrucoes.ESQUEMA, ((Map<?, ?>) formato.get("json_schema")).get("schema"));

		OpenAiCompativelAdaptador semEsquema = new OpenAiCompativelAdaptador("Ollama", "http://localhost:11434/v1", null, "llama", false, 1000);
		formato = (Map<?, ?>) semEsquema.corpo(pedido).get("response_format");
		assertEquals("json_object", formato.get("type"));

		List<?> mensagens = (List<?>) semEsquema.corpo(pedido).get("messages");
		assertEquals("system", ((Map<?, ?>) mensagens.get(0)).get("role"));
		assertEquals("user", ((Map<?, ?>) mensagens.get(1)).get("role"));
	}

	@Test
	public void rnIa06_semProvedorFicaIndisponivel() {
		ProvedoresIa provedores = new ProvedoresIa(null, null);

		assertFalse(provedores.disponivel());
		assertNull(provedores.descricao());
	}

	@Test
	public void rnIa06_principalFalhaEAReservaResponde() {
		ProvedoresIa provedores = new ProvedoresIa(falha("principal caiu"), fixo("reserva"));

		assertEquals("reserva", provedores.responder(pedido).getTexto());
	}

	@Test
	public void rnIa06_tentaCadaProvedorNaOrdem() {
		ProvedoresIa provedores = new ProvedoresIa(falha("claude caiu"), falha("gemini caiu"), fixo("deepseek"), fixo("openai"));

		assertEquals("deepseek", provedores.responder(pedido).getTexto());
		assertEquals("falho", provedores.descricao());
	}

	@Test
	public void rnIa06_todosFalhamESobeAUltimaFalha() {
		ProvedoresIa provedores = new ProvedoresIa(falha("claude caiu"), falha("openai caiu"));

		try {
			provedores.responder(pedido);
			fail("devia falhar");
		} catch (FalhaDoModelo e) {
			assertEquals("openai caiu", e.getMessage());
			assertEquals("claude caiu", e.getSuppressed()[0].getMessage());
		}
	}

	@Test
	public void rnIa06_semReservaAFalhaSobe() {
		ProvedoresIa provedores = new ProvedoresIa(falha("principal caiu"), null);

		try {
			provedores.responder(pedido);
			fail("devia falhar");
		} catch (FalhaDoModelo e) {
			assertEquals("principal caiu", e.getMessage());
		}
	}

	@Test
	public void provedorEhLidoSemDiferenciarMaiusculas() {
		assertEquals(Provedor.CLAUDE, Provedor.de(" Claude "));
		assertEquals(Provedor.NENHUM, Provedor.de(""));
		assertEquals(Provedor.NENHUM, Provedor.de(null));
		assertNull(Provedor.de("gpt"));
		assertEquals(Provedor.GEMINI, Provedor.de("google"));
		assertEquals("KAIROS_IA_CHAVE_DEEPSEEK", "KAIROS_IA_CHAVE_" + Provedor.DEEPSEEK.sufixo());
	}

	@Test
	public void httpJsonNavegaNoMapaSemQuebrar() {
		Map<String, Object> corpo = HttpJson.interpretar("{\"usage\":{\"input_tokens\":12},\"stop_reason\":\"end_turn\"}");

		assertEquals(Long.valueOf(12), HttpJson.inteiro(HttpJson.caminho(corpo, "usage", "input_tokens")));
		assertNull(HttpJson.caminho(corpo, "usage", "nada", "mais"));
		assertNull(HttpJson.interpretar("isto não é json"));
		assertNull(HttpJson.interpretar(""));
	}

	private static ModeloDeLinguagem fixo(final String texto) {
		return new ModeloDeLinguagem() {
			@Override
			public String descricao() {
				return "fixo";
			}

			@Override
			public RespostaModelo responder(PedidoModelo pedido) {
				return new RespostaModelo(texto, descricao(), null, null);
			}
		};
	}

	private static ModeloDeLinguagem falha(final String mensagem) {
		return new ModeloDeLinguagem() {
			@Override
			public String descricao() {
				return "falho";
			}

			@Override
			public RespostaModelo responder(PedidoModelo pedido) {
				throw new FalhaDoModelo(mensagem);
			}
		};
	}
}
