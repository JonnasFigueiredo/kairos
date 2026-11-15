package br.com.kairos.action;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import br.com.kairos.business.AnaliseIaBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.exception.IaIndisponivelException;
import br.com.kairos.infra.Action;
import br.com.kairos.vo.AnaliseVo;
import br.com.kairos.vo.ResultadoVo;

/**
 * Assistente de IA na tela do atendimento (RN-IA). Chamado pelo JavaScript da página
 * (fetch), responde JSON pelo plugin do Struts: a análise em seções ou um erro para o usuário.
 */
public class AnaliseIaAction extends Action {

	private static final long serialVersionUID = 1L;

	private final AnaliseIaBusiness business = new AnaliseIaBusiness();

	private Long codigoCompromisso;
	/** O que está no formulário agora: indicador, achados e parecer (RN-IA-02). */
	private ResultadoVo resultadoVo = new ResultadoVo();
	/** Raiz do JSON de resposta (struts.xml: <param name="root">analise</param>). */
	private AnaliseVo analise;

	public String analisar() {
		HttpServletResponse resposta = ServletActionContext.getResponse();

		if (!requisicaoEhPost()) {
			resposta.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			analise = AnaliseVo.comErro(getText("ia.erro.metodo"));
			return SUCCESS;
		}

		try {
			analise = business.analisar(codigoCompromisso, resultadoVo, usuarioLogado(), LocalDateTime.now().withNano(0));
		} catch (IaIndisponivelException e) {
			// O pedido estava certo; quem falhou foi o provedor
			resposta.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			analise = AnaliseVo.comErro(e.getMessage());
		} catch (BusinessException e) {
			resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			analise = AnaliseVo.comErro(e.getMessage());
		}
		return SUCCESS;
	}

	public AnaliseVo getAnalise() {
		return analise;
	}

	public Long getCodigoCompromisso() {
		return codigoCompromisso;
	}

	public void setCodigoCompromisso(Long codigoCompromisso) {
		this.codigoCompromisso = codigoCompromisso;
	}

	public ResultadoVo getResultadoVo() {
		return resultadoVo;
	}

	public void setResultadoVo(ResultadoVo resultadoVo) {
		this.resultadoVo = resultadoVo;
	}
}
