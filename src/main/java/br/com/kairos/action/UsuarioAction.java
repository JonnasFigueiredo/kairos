package br.com.kairos.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.kairos.business.UsuarioBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Acao;
import br.com.kairos.infra.Action;
import br.com.kairos.infra.Modulo;
import br.com.kairos.vo.UsuarioVo;

/**
 * Tela Usuários e acessos (RN-PE-01): lista as contas, cria e altera conta,
 * permissões por módulo e dados de médico. Conta não se exclui, só se desativa (RN-PE-06).
 */
public class UsuarioAction extends Action {

	private static final long serialVersionUID = 1L;

	private final UsuarioBusiness business = new UsuarioBusiness();

	private List<UsuarioVo> usuarios = new ArrayList<>();
	private UsuarioVo usuarioVo = new UsuarioVo();
	/** Caixa "É médico" do formulário. */
	private boolean ehMedico;

	public String listar() {
		usuarios = business.listar();
		return SUCCESS;
	}

	public String novo() {
		return INPUT;
	}

	public String editar() {
		try {
			usuarioVo = business.buscar(usuarioVo.getCodigo());
			ehMedico = usuarioVo.ehMedico();
			return INPUT;
		} catch (BusinessException e) {
			clearFieldErrors();
			addActionError(e.getMessage());
			return REDIRECT;
		}
	}

	public String salvar() {
		if (!requisicaoEhPost())
			return REDIRECT;

		if (!podeGravar(Modulo.ACESSOS, usuarioVo.getCodigo()))
			return SEM_ACESSO;

		try {
			business.salvar(usuarioVo, ehMedico, usuarioLogado());
			addActionMessage(getText("usuario.salvo"));
			return REDIRECT;
		} catch (BusinessException e) {
			addActionError(e.getMessage());
			usuarioVo.setSenha(null);
			return INPUT;
		}
	}

	/** Linhas da matriz de acessos, na ordem do enum. */
	public List<Modulo> getModulos() {
		return Arrays.asList(Modulo.values());
	}

	public List<Acao> getAcoes() {
		return Arrays.asList(Acao.values());
	}

	public List<String> getUfs() {
		return UsuarioBusiness.UFS;
	}

	public List<UsuarioVo> getUsuarios() {
		return usuarios;
	}

	public UsuarioVo getUsuarioVo() {
		return usuarioVo;
	}

	public void setUsuarioVo(UsuarioVo usuarioVo) {
		this.usuarioVo = usuarioVo;
	}

	public boolean isEhMedico() {
		return ehMedico;
	}

	public void setEhMedico(boolean ehMedico) {
		this.ehMedico = ehMedico;
	}
}
