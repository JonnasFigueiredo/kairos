package br.com.kairos.vo;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import br.com.kairos.infra.Acao;
import br.com.kairos.infra.Modulo;

/**
 * Dados de um usuário do sistema. Depois do login, fica guardado na sessão,
 * por isso é Serializable.
 */
public class UsuarioVo implements Serializable {

	private static final long serialVersionUID = 2L;

	private Long codigo;
	/** O que a pessoa digita para entrar. */
	private String login;
	/** O que aparece no menu. */
	private String nome;
	/** Resultado do PBKDF2 (RN-AC-03), nunca a senha. Nulo no objeto que vai para a sessão. */
	private String senha;
	/** Conta inativa não entra (RN-PE-06). */
	private boolean ativo = true;
	/** Permissões no formato MODULO:ACAO (RN-PE-01). */
	private Set<String> permissoes = new LinkedHashSet<>();
	/** Preenchido só quando a conta é de médico (RN-ME-01). */
	private MedicoVo medico;

	/**
	 * Conferência usada pelo interceptor e pelo menu. Módulo só de médico exige
	 * também o cadastro de médico (RN-ME-03), conferido a cada pedido.
	 */
	public boolean pode(Modulo modulo, Acao acao) {
		if (modulo.isSoMedico() && medico == null)
			return false;
		return permissoes.contains(modulo.chave(acao));
	}

	/** Versão para o JSP: #session.usuario.pode('FUNCIONARIOS:VER'). */
	public boolean pode(String chave) {
		if (chave == null || !chave.contains(":"))
			return false;

		String[] partes = chave.split(":", 2);
		Modulo modulo = Modulo.porNome(partes[0]);
		Acao acao = Acao.porNome(partes[1]);

		return modulo != null && acao != null && pode(modulo, acao);
	}

	public boolean ehMedico() {
		return medico != null;
	}

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

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public Set<String> getPermissoes() {
		return permissoes;
	}

	public void setPermissoes(Set<String> permissoes) {
		this.permissoes = permissoes == null ? new LinkedHashSet<String>() : new LinkedHashSet<>(permissoes);
	}

	public MedicoVo getMedico() {
		return medico;
	}

	public void setMedico(MedicoVo medico) {
		this.medico = medico;
	}
}
