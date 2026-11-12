package br.com.kairos.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.kairos.business.AgendaBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Action;
import br.com.kairos.infra.Periodo;
import br.com.kairos.vo.AgendaVo;

/**
 * Telas do cadastro de agendas (RN-AG-01).
 */
public class AgendaAction extends Action {

	private static final long serialVersionUID = 1L;

	private final AgendaBusiness business = new AgendaBusiness();

	/** Campo de pesquisa da tela de consulta. */
	private String busca;
	/** Linhas da tabela da tela de consulta. */
	private List<AgendaVo> agendas = new ArrayList<>();
	/** Agenda do formulário (inclusão e alteração) e da exclusão. */
	private AgendaVo agendaVo = new AgendaVo();

	public String listar() {
		agendas = business.pesquisar(busca);
		return SUCCESS;
	}

	public String novo() {
		return INPUT;
	}

	public String editar() {
		try {
			agendaVo = business.buscar(agendaVo.getCodigo());
			return INPUT;
		} catch (BusinessException e) {
			// Código com letra na URL já deixou "valor inválido"; basta a mensagem abaixo
			clearFieldErrors();
			addActionError(e.getMessage());
			return REDIRECT;
		}
	}

	public String salvar() {
		if (!requisicaoEhPost())
			return REDIRECT;

		try {
			business.salvar(agendaVo);
			addActionMessage(getText("agenda.salva"));
			return REDIRECT;
		} catch (BusinessException e) {
			addActionError(e.getMessage());
			return INPUT;
		}
	}

	public String excluir() {
		if (!requisicaoEhPost())
			return REDIRECT;

		try {
			business.excluir(agendaVo.getCodigo());
			addActionMessage(getText("agenda.excluida"));
		} catch (BusinessException e) {
			addActionError(e.getMessage());
		}
		return REDIRECT;
	}

	/** Opções do select de período (RN-AG-03): o JSP lê codigo e descricao de cada uma. */
	public List<Periodo> getPeriodos() {
		return Arrays.asList(Periodo.values());
	}

	public String getBusca() {
		return busca;
	}

	public void setBusca(String busca) {
		this.busca = busca;
	}

	public List<AgendaVo> getAgendas() {
		return agendas;
	}

	public AgendaVo getAgendaVo() {
		return agendaVo;
	}

	public void setAgendaVo(AgendaVo agendaVo) {
		this.agendaVo = agendaVo;
	}
}
