package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.FuncionarioVo;

/**
 * SQL da tabela funcionario. Só lê e grava: nenhuma regra de negócio aqui.
 */
public class FuncionarioDao extends Dao {

	private static final String COLUNAS = "SELECT cd_funcionario, nm_funcionario FROM funcionario";

	public void inserir(FuncionarioVo funcionario) {
		String sql = "INSERT INTO funcionario (nm_funcionario) VALUES (?)";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, funcionario.getNome());
			comando.executeUpdate();

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao inserir funcionário", e);
		}
	}

	/** Devolve quantas linhas foram alteradas: 0 se o código não existe. */
	public int alterar(FuncionarioVo funcionario) {
		String sql = "UPDATE funcionario SET nm_funcionario = ? WHERE cd_funcionario = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, funcionario.getNome());
			comando.setLong(2, funcionario.getCodigo());
			return comando.executeUpdate();

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao alterar funcionário", e);
		}
	}

	/**
	 * Participa de transação: usa a conexão recebida e não a fecha.
	 * Devolve quantas linhas foram excluídas: 0 se o código não existe.
	 */
	public int excluir(Connection conexao, Long codigo) throws SQLException {
		String sql = "DELETE FROM funcionario WHERE cd_funcionario = ?";

		try (PreparedStatement comando = conexao.prepareStatement(sql)) {
			comando.setLong(1, codigo);
			return comando.executeUpdate();
		}
	}

	/** Devolve null se o código não existe. */
	public FuncionarioVo buscarPorCodigo(Long codigo) {
		String sql = COLUNAS + " WHERE cd_funcionario = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigo);

			try (ResultSet resultado = comando.executeQuery()) {
				return resultado.next() ? montar(resultado) : null;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao buscar funcionário", e);
		}
	}

	public List<FuncionarioVo> listarTodos() {
		String sql = COLUNAS + " ORDER BY nm_funcionario";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql);
				ResultSet resultado = comando.executeQuery()) {

			return montarLista(resultado);

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar funcionários", e);
		}
	}

	public List<FuncionarioVo> listarPorNome(String trecho) {
		String sql = COLUNAS + " WHERE nm_funcionario LIKE ? ORDER BY nm_funcionario";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			// O valor vai como parâmetro, nunca concatenado no SQL
			comando.setString(1, "%" + trecho + "%");

			try (ResultSet resultado = comando.executeQuery()) {
				return montarLista(resultado);
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao pesquisar funcionários", e);
		}
	}

	private List<FuncionarioVo> montarLista(ResultSet resultado) throws SQLException {
		List<FuncionarioVo> funcionarios = new ArrayList<>();

		while (resultado.next())
			funcionarios.add(montar(resultado));

		return funcionarios;
	}

	/** Transforma a linha atual do resultado em um objeto. */
	private FuncionarioVo montar(ResultSet resultado) throws SQLException {
		FuncionarioVo funcionario = new FuncionarioVo();
		funcionario.setCodigo(resultado.getLong("cd_funcionario"));
		funcionario.setNome(resultado.getString("nm_funcionario"));
		return funcionario;
	}
}
