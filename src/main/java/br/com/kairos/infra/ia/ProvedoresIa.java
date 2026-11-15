package br.com.kairos.infra.ia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.com.kairos.infra.Configuracao;
import br.com.kairos.infra.ia.ModeloDeLinguagem.FalhaDoModelo;
import br.com.kairos.infra.ia.ModeloDeLinguagem.PedidoModelo;
import br.com.kairos.infra.ia.ModeloDeLinguagem.RespostaModelo;

/**
 * Monta os adaptadores a partir da configuração (RN-IA-06). Cada provedor tem a sua chave;
 * KAIROS_IA_PROVEDOR diz a ordem de tentativa. Se o primeiro falhar, tenta o próximo da lista.
 *
 * Configuração (variável de ambiente ou .env):
 *   KAIROS_IA_PROVEDOR          ordem, separada por vírgula: claude, openai, gemini (ou google),
 *                               deepseek, ollama ou falso. Vazio = desligado. Ex.: claude,gemini,deepseek
 *   KAIROS_IA_CHAVE_CLAUDE      chave de cada provedor (Ollama não usa chave)
 *   KAIROS_IA_CHAVE_OPENAI
 *   KAIROS_IA_CHAVE_GEMINI
 *   KAIROS_IA_CHAVE_DEEPSEEK
 *   KAIROS_IA_MODELO_<PROVEDOR> modelo, para trocar o padrão (obrigatório em openai e ollama)
 *   KAIROS_IA_URL_<PROVEDOR>    endereço, só para trocar o padrão (ex.: Ollama em outra máquina)
 *   KAIROS_IA_ESFORCO           esforço do Claude: low, medium (padrão) ou high
 *   KAIROS_IA_TEMPO_MAXIMO      segundos de espera pela resposta (padrão 120)
 *
 * Provedor da lista sem chave ou sem modelo é pulado, com aviso no log.
 */
public final class ProvedoresIa {

	private static final Logger LOG = LogManager.getLogger(ProvedoresIa.class);
	private static final int TEMPO_MAXIMO_PADRAO_SEGUNDOS = 120;

	private static ProvedoresIa instancia;

	/** Na ordem de tentativa. Vazia = IA desligada. */
	private final List<ModeloDeLinguagem> modelos;

	/** Público para os testes montarem provedores de mentira; a aplicação usa instancia(). */
	public ProvedoresIa(ModeloDeLinguagem... modelos) {
		List<ModeloDeLinguagem> lista = new ArrayList<>();
		for (ModeloDeLinguagem modelo : modelos)
			if (modelo != null)
				lista.add(modelo);
		this.modelos = Collections.unmodifiableList(lista);
	}

	/** Lida uma vez: a configuração não muda com a aplicação no ar. */
	public static synchronized ProvedoresIa instancia() {
		if (instancia == null)
			instancia = daConfiguracao();
		return instancia;
	}

	static ProvedoresIa daConfiguracao() {
		int tempoMaximoMs = inteiro(Configuracao.obter("KAIROS_IA_TEMPO_MAXIMO", null), TEMPO_MAXIMO_PADRAO_SEGUNDOS) * 1000;
		String esforco = Configuracao.obter("KAIROS_IA_ESFORCO", "medium");
		String ordem = Configuracao.obter("KAIROS_IA_PROVEDOR", "");

		List<ModeloDeLinguagem> montados = new ArrayList<>();
		for (String nome : Arrays.asList(ordem.split(","))) {
			ModeloDeLinguagem modelo = montar(nome, esforco, tempoMaximoMs);
			if (modelo != null)
				montados.add(modelo);
		}

		ProvedoresIa provedores = new ProvedoresIa(montados.toArray(new ModeloDeLinguagem[0]));
		if (montados.isEmpty())
			LOG.info("Assistente de IA desligado: nenhum provedor configurado em KAIROS_IA_PROVEDOR");
		else
			LOG.info("Assistente de IA, na ordem de tentativa: {}", provedores.descricoes());
		return provedores;
	}

	/** Devolve null quando o nome está vazio ou a configuração do provedor está incompleta (avisa no log). */
	static ModeloDeLinguagem montar(String nome, String esforco, int tempoMaximoMs) {
		Provedor provedor = Provedor.de(nome);

		if (provedor == null) {
			LOG.warn("KAIROS_IA_PROVEDOR: \"{}\" não é um provedor conhecido; ignorado", nome.trim());
			return null;
		}
		if (provedor == Provedor.NENHUM)
			return null;
		if (provedor == Provedor.FALSO)
			return new AdaptadorFalso();

		String modelo = Configuracao.obter("KAIROS_IA_MODELO_" + provedor.sufixo(), provedor.modeloPadrao);
		String chave = Configuracao.obter("KAIROS_IA_CHAVE_" + provedor.sufixo(), null);
		String url = Configuracao.obter("KAIROS_IA_URL_" + provedor.sufixo(), provedor.urlPadrao);

		if (modelo == null) {
			LOG.warn("{} exige KAIROS_IA_MODELO_{}; ignorado", provedor.nome, provedor.sufixo());
			return null;
		}
		if (provedor.exigeChave && chave == null) {
			LOG.warn("{} exige KAIROS_IA_CHAVE_{}; ignorado", provedor.nome, provedor.sufixo());
			return null;
		}

		if (provedor == Provedor.CLAUDE)
			return new ClaudeAdaptador(url, chave, modelo, esforco, tempoMaximoMs);

		return new OpenAiCompativelAdaptador(provedor.nome, url, chave, modelo, provedor.aceitaEsquema, tempoMaximoMs);
	}

	public boolean disponivel() {
		return !modelos.isEmpty();
	}

	/** Descrição do primeiro provedor da lista; null quando desligado. */
	public String descricao() {
		return modelos.isEmpty() ? null : modelos.get(0).descricao();
	}

	String descricoes() {
		List<String> nomes = new ArrayList<>();
		for (ModeloDeLinguagem modelo : modelos)
			nomes.add(modelo.descricao());
		return String.join(", ", nomes);
	}

	/** RN-IA-06: tenta cada provedor na ordem; o primeiro que responder vale. Se todos falharem, sobe a última falha. */
	public RespostaModelo responder(PedidoModelo pedido) {
		if (modelos.isEmpty())
			throw new FalhaDoModelo("Nenhum provedor de IA configurado");

		FalhaDoModelo ultima = null;
		for (ModeloDeLinguagem modelo : modelos) {
			try {
				return modelo.responder(pedido);
			} catch (FalhaDoModelo falha) {
				LOG.warn("{} falhou: {}", modelo.descricao(), falha.getMessage());
				if (ultima != null)
					falha.addSuppressed(ultima);
				ultima = falha;
			}
		}
		throw ultima;
	}

	private static int inteiro(String valor, int padrao) {
		try {
			return valor == null ? padrao : Integer.parseInt(valor.trim());
		} catch (NumberFormatException e) {
			return padrao;
		}
	}
}
