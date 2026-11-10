package br.com.kairos.infra;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

/**
 * Classe base das Actions do projeto.
 *
 * Herda de ActionSupport, do Struts, que já traz os resultados SUCCESS e INPUT,
 * a leitura de textos (getText) e as mensagens para a tela (addActionMessage, addActionError).
 * Acrescenta o resultado REDIRECT, usado depois de gravar para voltar à consulta.
 */
public abstract class Action extends ActionSupport {

	private static final long serialVersionUID = 1L;

	public static final String REDIRECT = "redirect";

	/**
	 * Gravar e excluir só valem por POST. Um link, uma imagem ou o pré-carregamento
	 * do navegador fazem GET, e GET não pode alterar dados.
	 */
	protected boolean requisicaoEhPost() {
		return "POST".equalsIgnoreCase(ServletActionContext.getRequest().getMethod());
	}
}
