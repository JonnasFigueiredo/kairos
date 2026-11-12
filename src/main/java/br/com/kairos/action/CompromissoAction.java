package br.com.kairos.action;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.Preparable;

import br.com.kairos.business.AgendaBusiness;
import br.com.kairos.business.CompromissoBusiness;
import br.com.kairos.business.FuncionarioBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Action;
import br.com.kairos.infra.CalendarioMensal;
import br.com.kairos.vo.AgendaVo;
import br.com.kairos.vo.CompromissoVo;
import br.com.kairos.vo.FuncionarioVo;

/**
 * Telas do cadastro de compromissos (RN-CO-01).
 *
 * Implementa Preparable: antes de chamar "novo", "editar" ou "salvar", o Struts
 * chama o método "prepare" de mesmo nome. É ali que as opções dos selects são
 * carregadas, antes de qualquer página ser desenhada. Se o banco estiver fora
 * do ar, o erro acontece ainda dentro da Action e cai na página de erro.
 */
public class CompromissoAction extends Action implements Preparable {

	private static final long serialVersionUID = 1L;

	private final CompromissoBusiness business = new CompromissoBusiness();
	private final FuncionarioBusiness funcionarioBusiness = new FuncionarioBusiness();
	private final AgendaBusiness agendaBusiness = new AgendaBusiness();

	/** Campo de pesquisa da tela de consulta. */
	private String busca;
	/** Linhas da tabela da tela de consulta. */
	private List<CompromissoVo> compromissos = new ArrayList<>();
	/** Mês pedido na URL (aaaa-mm). Vazio ou inválido: o mês atual. */
	private String mes;
	/** Grade do mês na tela de consulta, com os mesmos compromissos da tabela. */
	private CalendarioMensal calendario;
	/** Compromisso do formulário (inclusão e alteração) e da exclusão. */
	private CompromissoVo compromissoVo = new CompromissoVo();

	/** Opções dos selects do formulário. */
	private List<FuncionarioVo> funcionarios = new ArrayList<>();
	private List<AgendaVo> agendas = new ArrayList<>();

	/** Exigido pela interface. Nada é comum a todos os métodos. */
	@Override
	public void prepare() {
		// de propósito vazio: consulta e exclusão não mostram o formulário
	}

	public void prepareNovo() {
		carregarOpcoes();
	}

	public void prepareEditar() {
		carregarOpcoes();
	}

	/** Também cobre o caso em que o Struts devolve o formulário por erro de conversão. */
	public void prepareSalvar() {
		carregarOpcoes();
	}

	private void carregarOpcoes() {
		funcionarios = funcionarioBusiness.pesquisar(null);
		agendas = agendaBusiness.pesquisar(null);
	}

	public String listar() {
		compromissos = business.pesquisar(busca);

		YearMonth mesExibido = CalendarioMensal.mesDe(mes, YearMonth.now());
		calendario = new CalendarioMensal(mesExibido, compromissos, LocalDate.now());
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
			business.salvar(compromissoVo);
			addActionMessage(getText("compromisso.salvo"));
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
			business.excluir(compromissoVo.getCodigo());
			addActionMessage(getText("compromisso.excluido"));
		} catch (BusinessException e) {
			addActionError(e.getMessage());
		}
		return REDIRECT;
	}

	public List<FuncionarioVo> getFuncionarios() {
		return funcionarios;
	}

	public List<AgendaVo> getAgendas() {
		return agendas;
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

	/** Devolve o mês em exibição, já corrigido, para o campo escondido do formulário de pesquisa. */
	public String getMes() {
		return calendario == null ? mes : calendario.getMes();
	}

	public void setMes(String mes) {
		this.mes = mes;
	}

	public CalendarioMensal getCalendario() {
		return calendario;
	}

	public CompromissoVo getCompromissoVo() {
		return compromissoVo;
	}

	public void setCompromissoVo(CompromissoVo compromissoVo) {
		this.compromissoVo = compromissoVo;
	}
}
