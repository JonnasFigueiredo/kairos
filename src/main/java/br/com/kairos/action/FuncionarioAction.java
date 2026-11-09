package br.com.kairos.action;

import java.util.ArrayList;
import java.util.List;

import br.com.kairos.business.FuncionarioBusiness;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Action;
import br.com.kairos.vo.FuncionarioVo;

/**
 * Telas do cadastro de funcionários.
 *
 * O Struts cria um objeto desta classe a cada requisição, preenche os atributos
 * com os campos enviados pela tela (pelos setters) e chama um dos métodos abaixo.
 * O texto devolvido escolhe a próxima página no struts.xml.
 */
public class FuncionarioAction extends Action {

	private static final long serialVersionUID = 1L;

	private final FuncionarioBusiness business = new FuncionarioBusiness();

	/** Campo de pesquisa da tela de consulta. */
	private String busca;
	/** Linhas da tabela da tela de consulta. */
	private List<FuncionarioVo> funcionarios = new ArrayList<>();
	/** Funcionário do formulário (inclusão e alteração) e da exclusão. */
	private FuncionarioVo funcionarioVo = new FuncionarioVo();

	/** Tela de consulta, com ou sem pesquisa. */
	public String listar() {
		funcionarios = business.pesquisar(busca);
		return SUCCESS;
	}

	/** Abre o formulário vazio. */
	public String novo() {
		return INPUT;
	}

	/** Abre o formulário com os dados do funcionário escolhido. */
	public String editar() {
		try {
			funcionarioVo = business.buscar(funcionarioVo.getCodigo());
			return INPUT;
		} catch (BusinessException e) {
			addActionError(e.getMessage());
			return REDIRECT;
		}
	}

	/** Grava o formulário. Regra violada: volta ao formulário com a mensagem. */
	public String salvar() {
		try {
			business.salvar(funcionarioVo);
			addActionMessage(getText("funcionario.salvo"));
			return REDIRECT;
		} catch (BusinessException e) {
			addActionError(e.getMessage());
			return INPUT;
		}
	}

	public String excluir() {
		try {
			business.excluir(funcionarioVo.getCodigo());
			addActionMessage(getText("funcionario.excluido"));
		} catch (BusinessException e) {
			addActionError(e.getMessage());
		}
		return REDIRECT;
	}

	public String getBusca() {
		return busca;
	}

	public void setBusca(String busca) {
		this.busca = busca;
	}

	public List<FuncionarioVo> getFuncionarios() {
		return funcionarios;
	}

	public FuncionarioVo getFuncionarioVo() {
		return funcionarioVo;
	}

	public void setFuncionarioVo(FuncionarioVo funcionarioVo) {
		this.funcionarioVo = funcionarioVo;
	}
}
