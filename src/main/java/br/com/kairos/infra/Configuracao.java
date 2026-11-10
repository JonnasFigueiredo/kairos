package br.com.kairos.infra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.kairos.exception.TechnicalException;

/**
 * Lê as configurações que mudam de máquina para máquina (endereço e senha do banco).
 *
 * Ordem de procura de cada chave:
 *   1. variável de ambiente;
 *   2. arquivo .env na pasta onde a aplicação foi iniciada;
 *   3. valor padrão informado por quem chama.
 *
 * Assim nenhuma senha fica escrita no código, e o mesmo .env serve
 * para o docker-compose e para a aplicação.
 */
public final class Configuracao {

	private static final String ARQUIVO = ".env";
	private static final Map<String, String> VALORES_DO_ARQUIVO = lerArquivo(Paths.get(ARQUIVO));

	private Configuracao() {
		// só métodos estáticos
	}

	public static String obter(String chave, String padrao) {
		String valor = System.getenv(chave);

		if (estaVazio(valor))
			valor = VALORES_DO_ARQUIVO.get(chave);

		return estaVazio(valor) ? padrao : valor.trim();
	}

	static Map<String, String> lerArquivo(Path arquivo) {
		if (!Files.isRegularFile(arquivo))
			return new HashMap<>();

		try {
			return interpretar(Files.readAllLines(arquivo, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new TechnicalException("Não foi possível ler o arquivo " + arquivo.toAbsolutePath(), e);
		}
	}

	/**
	 * Lê as linhas no formato CHAVE=valor, do mesmo jeito que o docker compose:
	 * o valor é tudo que vem depois do primeiro "=", sem interpretar barras
	 * invertidas. Linha vazia e linha começada por # são ignoradas.
	 */
	static Map<String, String> interpretar(List<String> linhas) {
		Map<String, String> valores = new HashMap<>();

		for (String linha : linhas) {
			String texto = linha.trim();
			int igual = texto.indexOf('=');

			if (texto.isEmpty() || texto.startsWith("#") || igual <= 0)
				continue;

			valores.put(texto.substring(0, igual).trim(), texto.substring(igual + 1).trim());
		}
		return valores;
	}

	private static boolean estaVazio(String valor) {
		return valor == null || valor.trim().isEmpty();
	}
}
