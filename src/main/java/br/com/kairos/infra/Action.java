package br.com.kairos.infra;

import com.opensymphony.xwork2.ActionSupport;

/**
 * Classe base das Actions do projeto.
 *
 * Herda de ActionSupport, do Struts, que já traz os resultados SUCCESS e INPUT,
 * a leitura de textos (getText) e as mensagens de erro para a tela (addActionError).
 * Acrescenta o resultado REDIRECT, usado depois de gravar para voltar à consulta.
 */
public abstract class Action extends ActionSupport {

	private static final long serialVersionUID = 1L;

	public static final String REDIRECT = "redirect";
}
