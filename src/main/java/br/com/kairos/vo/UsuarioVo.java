package br.com.kairos.vo;

import java.io.Serializable;

/**
 * Dados de um usuário do sistema. Depois do login, fica guardado na sessão,
 * por isso é Serializable.
 */
public class UsuarioVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long codigo;
	/** O que a pessoa digita para entrar. */
	private String login;
	/** O que aparece no menu. */
	private String nome;
	/** Resultado do PBKDF2 (RN-AC-03), nunca a senha. Nulo no objeto que vai para a sessão. */
	private String senha;

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
}
