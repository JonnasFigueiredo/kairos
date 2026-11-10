package br.com.kairos.business;

import br.com.kairos.dao.UsuarioDao;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Senha;
import br.com.kairos.vo.UsuarioVo;

/**
 * Regras do login: confere usuário e senha.
 */
public class AutenticacaoBusiness {

	/**
	 * RN-AC-02: a mesma mensagem para login inexistente e para senha errada.
	 * Mensagens diferentes contariam a um estranho quais logins existem.
	 */
	static final String RECUSADO = "Usuário ou senha inválidos.";

	/**
	 * Usado quando o login não existe, só para gastar o mesmo tempo de cálculo.
	 * Sem isso, a resposta mais rápida também revelaria que o login não existe.
	 */
	private static final String SENHA_DE_NINGUEM = Senha.gerar("login inexistente");

	private final UsuarioDao dao = new UsuarioDao();

	/** Devolve o usuário, sem a senha, pronto para ir para a sessão. */
	public UsuarioVo autenticar(String login, String senha) {
		validar(login, senha);

		UsuarioVo usuario = dao.buscarPorLogin(login.trim());
		boolean senhaConfere = Senha.confere(senha, usuario == null ? SENHA_DE_NINGUEM : usuario.getSenha());

		if (usuario == null || !senhaConfere)
			throw new BusinessException(RECUSADO);

		usuario.setSenha(null);
		return usuario;
	}

	/** RN-AC-02: usuário e senha são obrigatórios. Não vai ao banco. */
	void validar(String login, String senha) {
		if (login == null || login.trim().isEmpty())
			throw new BusinessException("Informe o usuário.");

		// A senha não passa por trim: espaço pode fazer parte dela
		if (senha == null || senha.isEmpty())
			throw new BusinessException("Informe a senha.");
	}
}
