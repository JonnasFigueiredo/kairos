package br.com.kairos.action;

import java.util.ArrayList;
import java.util.List;

import br.com.kairos.business.AgendaBusiness;
import br.com.kairos.business.CompromissoBusiness;
import br.com.kairos.business.FuncionarioBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Action;
import br.com.kairos.vo.AgendaVo;
import br.com.kairos.vo.CompromissoVo;
import br.com.kairos.vo.FuncionarioVo;

/**
 * Telas do cadastro de compromissos.
 */
public class CompromissoAction extends Action {

	private static final long serialVersionUID = 1L;

	private final CompromissoBusiness business = new CompromissoBusiness();
	private final FuncionarioBusiness funcionarioBusiness = new FuncionarioBusiness();
	private final AgendaBusiness agendaBusiness = new AgendaBusiness();

	/** Campo de pesquisa da tela de consulta. */
	private String busca;
	/** Linhas da tabela da tela de consulta. */
	private List<CompromissoVo> compromissos = new ArrayList<>();
	/** Compromisso do formulário (inclusão e alteração) e da exclusão. */
	private CompromissoVo compromissoVo = new CompromissoVo();

	public String listar() {
		compromissos = business.pesquisar(busca);
		return SUCCESS;
	}

	public String novo() {
		return INPUT;
	}

	public String editar() {
		try {
			compromissoVo = business.buscar(compromissoVo.getCodigo());
			return INPUT;
		} catch (BusinessException e) {
			addActionError(e.getMessage());
			return REDIRECT;
		}
	}

	public String salvar() {
		try {
			business.salvar(compromissoVo);
			addActionMessage(getText("compromisso.salvo"));
			return REDIRECT;
		} catch (BusinessException e) {
			addActionError(e.getMessage());
			return INPUT;
		}
	}

	public String excluir() {
		try {
			business.excluir(compromissoVo.getCodigo());
			addActionMessage(getText("compromisso.excluido"));
		} catch (BusinessException e) {
			addActionError(e.getMessage());
		}
		return REDIRECT;
	}

	/** Opções do select de funcionário. Só é chamado quando o formulário é desenhado. */
	public List<FuncionarioVo> getFuncionarios() {
		return funcionarioBusiness.pesquisar(null);
	}

	/** Opções do select de agenda. */
	public List<AgendaVo> getAgendas() {
		return agendaBusiness.pesquisar(null);
	}

	public String getBusca() {
		return busca;
	}

	public void setBusca(String busca) {
		this.busca = busca;
	}

	public List<CompromissoVo> getCompromissos() {
		return compromissos;
	}

	public CompromissoVo getCompromissoVo() {
		return compromissoVo;
	}

	public void setCompromissoVo(CompromissoVo compromissoVo) {
		this.compromissoVo = compromissoVo;
	}
}
