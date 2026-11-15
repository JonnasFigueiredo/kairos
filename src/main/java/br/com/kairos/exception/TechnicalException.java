package br.com.kairos.exception;

/**
 * Falha técnica: banco fora do ar, SQL inválido, arquivo ilegível.
 * A mensagem é para o log, não para o usuário. Guarda a causa original
 * para o erro não se perder.
 */
public class TechnicalException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TechnicalException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}

	/** Sem causa: erro de configuração detectado pelo próprio sistema. */
	public TechnicalException(String mensagem) {
		super(mensagem);
	}
}
