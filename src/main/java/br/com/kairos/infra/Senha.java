package br.com.kairos.infra;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import br.com.kairos.exception.TechnicalException;

/**
 * Transforma uma senha em um texto que pode ser guardado no banco, e confere
 * uma senha digitada contra esse texto (RN-AC-03).
 *
 * Usa PBKDF2, que já vem no Java 8. O algoritmo é lento de propósito: repete o
 * cálculo milhares de vezes, para que testar senhas em massa custe caro a quem
 * copiar a tabela. O sal é um trecho aleatório, diferente para cada senha:
 * duas pessoas com a mesma senha ficam com textos diferentes no banco.
 *
 * Formato guardado: pbkdf2$iterações$sal$resultado (sal e resultado em Base64).
 * As iterações e o sal vão junto para a conferência repetir o mesmo cálculo.
 */
public final class Senha {

	private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
	private static final String PREFIXO = "pbkdf2";
	private static final String SEPARADOR = "$";
	static final int ITERACOES = 120000;
	private static final int BYTES_DO_SAL = 16;
	private static final int BITS_DO_RESULTADO = 256;

	private static final SecureRandom SORTEIO = new SecureRandom();

	private Senha() {
		// só métodos estáticos
	}

	/** Devolve o texto a guardar no banco. Cada chamada sorteia um sal novo. */
	public static String gerar(String senha) {
		byte[] sal = new byte[BYTES_DO_SAL];
		SORTEIO.nextBytes(sal);

		byte[] resultado = calcular(senha, sal, ITERACOES);

		return PREFIXO + SEPARADOR + ITERACOES
				+ SEPARADOR + Base64.getEncoder().encodeToString(sal)
				+ SEPARADOR + Base64.getEncoder().encodeToString(resultado);
	}

	/** Falso também quando o texto guardado está fora do formato: nunca lança erro para a tela. */
	public static boolean confere(String senha, String guardada) {
		if (senha == null || guardada == null)
			return false;

		String[] partes = guardada.split("\\" + SEPARADOR);
		if (partes.length != 4 || !PREFIXO.equals(partes[0]))
			return false;

		try {
			int iteracoes = Integer.parseInt(partes[1]);
			byte[] sal = Base64.getDecoder().decode(partes[2]);
			byte[] esperado = Base64.getDecoder().decode(partes[3]);

			// isEqual compara em tempo constante: a demora não revela quantos bytes coincidem
			return MessageDigest.isEqual(esperado, calcular(senha, sal, iteracoes));

		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static byte[] calcular(String senha, byte[] sal, int iteracoes) {
		PBEKeySpec entrada = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, BITS_DO_RESULTADO);
		try {
			return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(entrada).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new TechnicalException("Erro ao calcular o resumo da senha", e);
		} finally {
			entrada.clearPassword();
		}
	}
}
