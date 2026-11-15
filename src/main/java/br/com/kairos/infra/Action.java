package br.com.kairos.infra;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import br.com.kairos.vo.UsuarioVo;

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
	/** Mesmo nome do resultado global devolvido pelo AutorizacaoInterceptor. */
	public static final String SEM_ACESSO = AutorizacaoInterceptor.SEM_ACESSO;

	/** Usuário da sessão. Nunca é nulo aqui: o AutenticacaoInterceptor barrou antes quem não entrou. */
	protected UsuarioVo usuarioLogado() {
		Map<String, Object> sessao = ActionContext.getContext().getSession();
		Object usuario = sessao == null ? null : sessao.get(AutenticacaoInterceptor.USUARIO);
		return usuario instanceof UsuarioVo ? (UsuarioVo) usuario : new UsuarioVo();
	}

	/**
	 * O interceptor libera "salvar" para quem inclui ou altera. Aqui a Action
	 * confere o caso exato: código nulo é inclusão, preenchido é alteração (RN-PE-02).
	 */
	protected boolean podeGravar(Modulo modulo, Long codigo) {
		return usuarioLogado().pode(modulo, codigo == null ? Acao.INCLUIR : Acao.ALTERAR);
	}

	/**
	 * Gravar e excluir só valem por POST. Um link, uma imagem ou o pré-carregamento
	 * do navegador fazem GET, e GET não pode alterar dados.
	 *
	 * O POST também precisa vir de uma tela do próprio Kairos. Sem isso, uma página
	 * de outro site aberta no mesmo navegador enviaria um formulário escondido e
	 * gravaria em nome do usuário logado (CSRF).
	 */
	protected boolean requisicaoEhPost() {
		HttpServletRequest requisicao = ServletActionContext.getRequest();

		return "POST".equalsIgnoreCase(requisicao.getMethod())
				&& mesmaOrigem(requisicao.getHeader("Origin"), requisicao.getHeader("Referer"),
						requisicao.getHeader("Host"));
	}

	/**
	 * O navegador informa de onde veio o pedido no cabeçalho Origin e, na falta
	 * dele, no Referer. Quando informa, o endereço precisa ser o deste servidor.
	 * Sem nenhum dos dois não há como saber, e o pedido segue.
	 */
	static boolean mesmaOrigem(String origem, String referencia, String host) {
		String informada = vazio(origem) ? referencia : origem;

		if (vazio(informada))
			return true;

		if (vazio(host))
			return false;

		try {
			String autoridade = new URI(informada.trim()).getRawAuthority();
			return host.trim().equalsIgnoreCase(autoridade);
		} catch (URISyntaxException e) {
			return false;
		}
	}

	private static boolean vazio(String texto) {
		return texto == null || texto.trim().isEmpty();
	}
}
