package br.com.kairos.action;

import br.com.kairos.business.ResumoBusiness;
import br.com.kairos.infra.Action;
import br.com.kairos.vo.ResumoVo;

/**
 * Página inicial: mostra o total de funcionários, agendas e compromissos.
 */
public class InicioAction extends Action {

	private static final long serialVersionUID = 1L;

	private final ResumoBusiness business = new ResumoBusiness();
	private ResumoVo resumo;

	@Override
	public String execute() {
		resumo = business.carregar();
		return SUCCESS;
	}

	/** O JSP lê os totais por aqui: resumo.totalFuncionarios */
	public ResumoVo getResumo() {
		return resumo;
	}
}
