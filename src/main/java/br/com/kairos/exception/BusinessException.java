package br.com.kairos.exception;

/**
 * Regra de negócio violada. A mensagem é escrita para o usuário e aparece na tela.
 * Exemplo: tentar excluir uma agenda que tem compromissos.
 */
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BusinessException(String mensagem) {
		super(mensagem);
	}
}
