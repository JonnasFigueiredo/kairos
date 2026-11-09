package br.com.kairos.business;

import java.util.ArrayList;
import java.util.List;

import br.com.kairos.dao.CompromissoDao;
import br.com.kairos.dao.FuncionarioDao;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Transacao;
import br.com.kairos.vo.FuncionarioVo;

/**
 * Regras do cadastro de funcionários.
 */
public class FuncionarioBusiness {

	static final int TAMANHO_MAXIMO_DO_NOME = 255;

	private final FuncionarioDao dao = new FuncionarioDao();
	private final CompromissoDao compromissoDao = new CompromissoDao();

	/**
	 * Pesquisa em um campo só: vazio traz todos, número procura pelo código,
	 * texto procura pelo nome.
	 */
	public List<FuncionarioVo> pesquisar(String busca) {
		if (busca == null || busca.trim().isEmpty())
			return dao.listarTodos();

		String termo = busca.trim();

		if (termo.matches("\\d{1,18}")) {
			List<FuncionarioVo> encontrados = new ArrayList<>();
			FuncionarioVo funcionario = dao.buscarPorCodigo(Long.valueOf(termo));
			if (funcionario != null)
				encontrados.add(funcionario);
			return encontrados;
		}

		return dao.listarPorNome(termo);
	}

	public FuncionarioVo buscar(Long codigo) {
		FuncionarioVo funcionario = codigo == null ? null : dao.buscarPorCodigo(codigo);

		if (funcionario == null)
			throw new BusinessException("Funcionário não encontrado.");

		return funcionario;
	}

	/** Inclui quando o código é nulo; altera quando o código vem preenchido. */
	public void salvar(FuncionarioVo funcionario) {
		validar(funcionario);

		if (funcionario.getCodigo() == null) {
			dao.inserir(funcionario);
			return;
		}

		if (dao.alterar(funcionario) == 0)
			throw new BusinessException("Funcionário não encontrado.");
	}

	/**
	 * RN-FU-03: excluir um funcionário exclui também os compromissos dele.
	 * As duas exclusões rodam na mesma transação: se a segunda falhar,
	 * a primeira é desfeita e nenhum compromisso se perde.
	 */
	public void excluir(Long codigo) {
		if (codigo == null)
			throw new BusinessException("Funcionário não encontrado.");

		Transacao.executar(conexao -> {
			// Primeiro os compromissos: a chave estrangeira impede excluir o funcionário antes
			compromissoDao.excluirPorFuncionario(conexao, codigo);

			if (dao.excluir(conexao, codigo) == 0)
				throw new BusinessException("Funcionário não encontrado.");
		});
	}

	/** RN-FU-02: nome obrigatório. Também tira os espaços das pontas. */
	void validar(FuncionarioVo funcionario) {
		String nome = funcionario.getNome() == null ? "" : funcionario.getNome().trim();

		if (nome.isEmpty())
			throw new BusinessException("Informe o nome do funcionário.");

		if (nome.length() > TAMANHO_MAXIMO_DO_NOME)
			throw new BusinessException("O nome pode ter no máximo " + TAMANHO_MAXIMO_DO_NOME + " caracteres.");

		funcionario.setNome(nome);
	}
}
