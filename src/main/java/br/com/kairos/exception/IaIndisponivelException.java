package br.com.kairos.exception;

/**
 * O assistente de IA não respondeu: todos os provedores falharam (RN-IA-05).
 * A mensagem é para o usuário seguir sem a IA. A tela responde HTTP 503, e não 400,
 * porque o pedido estava certo: quem falhou foi o serviço externo.
 */
public class IaIndisponivelException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public IaIndisponivelException(String mensagem) {
		super(mensagem);
	}
}
