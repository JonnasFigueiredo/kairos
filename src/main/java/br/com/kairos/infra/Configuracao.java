package br.com.kairos.infra;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

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
	private static final Properties VALORES_DO_ARQUIVO = lerArquivo();

	private Configuracao() {
		// só métodos estáticos
	}

	public static String obter(String chave, String padrao) {
		String valor = System.getenv(chave);

		if (estaVazio(valor))
			valor = VALORES_DO_ARQUIVO.getProperty(chave);

		return estaVazio(valor) ? padrao : valor.trim();
	}

	private static Properties lerArquivo() {
		Properties valores = new Properties();
		File arquivo = new File(ARQUIVO);

		if (!arquivo.isFile())
			return valores;

		try (Reader leitor = new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8)) {
			valores.load(leitor);
		} catch (IOException e) {
			throw new TechnicalException("Não foi possível ler o arquivo " + arquivo.getAbsolutePath(), e);
		}
		return valores;
	}

	private static boolean estaVazio(String valor) {
		return valor == null || valor.trim().isEmpty();
	}
}
