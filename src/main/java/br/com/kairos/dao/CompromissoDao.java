package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.CompromissoVo;

/**
 * SQL da tabela compromisso. Só lê e grava: nenhuma regra de negócio aqui.
 */
public class CompromissoDao extends Dao {

	/** Toda consulta traz junto o nome do funcionário e o nome e período da agenda. */
	private static final String COLUNAS =
			"SELECT c.cd_compromisso, c.cd_funcionario, f.nm_funcionario, "
			+ "c.cd_agenda, a.nm_agenda, a.cd_periodo, c.dt_compromisso, c.hr_compromisso "
			+ "FROM compromisso c "
			+ "JOIN funcionario f ON f.cd_funcionario = c.cd_funcionario "
			+ "JOIN agenda a ON a.cd_agenda = c.cd_agenda";

	private static final String ORDEM = " ORDER BY c.dt_compromisso, c.hr_compromisso, f.nm_funcionario";

	public void inserir(CompromissoVo compromisso) {
		String sql = "INSERT INTO compromisso (cd_funcionario, cd_agenda, dt_compromisso, hr_compromisso) "
				+ "VALUES (?, ?, ?, ?)";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, compromisso.getCodigoFuncionario());
			comando.setLong(2, compromisso.getCodigoAgenda());
			comando.setObject(3, compromisso.getData());
			comando.setObject(4, compromisso.getHorario());
			comando.executeUpdate();

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao inserir compromisso", e);
		}
	}

	/** Devolve quantas linhas foram alteradas: 0 se o código não existe. */
	public int alterar(CompromissoVo compromisso) {
		String sql = "UPDATE compromisso SET cd_funcionario = ?, cd_agenda = ?, "
				+ "dt_compromisso = ?, hr_compromisso = ? WHERE cd_compromisso = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, compromisso.getCodigoFuncionario());
			comando.setLong(2, compromisso.getCodigoAgenda());
			comando.setObject(3, compromisso.getData());
			comando.setObject(4, compromisso.getHorario());
			comando.setLong(5, compromisso.getCodigo());
			return comando.executeUpdate();

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao alterar compromisso", e);
		}
	}

	/**
	 * Participa de transação: usa a conexão recebida e não a fecha (RN-AT-05).
	 * Devolve quantas linhas foram excluídas: 0 se o código não existe.
	 */
	public int excluir(Connection conexao, Long codigo) throws SQLException {
		String sql = "DELETE FROM compromisso WHERE cd_compromisso = ?";

		try (PreparedStatement comando = conexao.prepareStatement(sql)) {
			comando.setLong(1, codigo);
			return comando.executeUpdate();
		}
	}

	/**
	 * Exclui todos os compromissos de um funcionário (RN-FU-03).
	 * Participa de transação: usa a conexão recebida e não a fecha.
	 */
	public int excluirPorFuncionario(Connection conexao, Long codigoFuncionario) throws SQLException {
		String sql = "DELETE FROM compromisso WHERE cd_funcionario = ?";

		try (PreparedStatement comando = conexao.prepareStatement(sql)) {
			comando.setLong(1, codigoFuncionario);
			return comando.executeUpdate();
		}
	}

	/** Devolve null se o código não existe. */
	public CompromissoVo buscarPorCodigo(Long codigo) {
		String sql = COLUNAS + " WHERE c.cd_compromisso = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigo);

			try (ResultSet resultado = comando.executeQuery()) {
				return resultado.next() ? montar(resultado) : null;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao buscar compromisso", e);
		}
	}

	public List<CompromissoVo> listarTodos() {
		String sql = COLUNAS + ORDEM;

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql);
				ResultSet resultado = comando.executeQuery()) {

			return montarLista(resultado);

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar compromissos", e);
		}
	}

	/** Procura o trecho no nome do funcionário ou no nome da agenda. */
	public List<CompromissoVo> listarPorNome(String trecho) {
		String sql = COLUNAS + " WHERE f.nm_funcionario LIKE ? OR a.nm_agenda LIKE ?" + ORDEM;

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, contendo(trecho));
			comando.setString(2, contendo(trecho));

			try (ResultSet resultado = comando.executeQuery()) {
				return montarLista(resultado);
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao pesquisar compromissos", e);
		}
	}

	/** RN-RE-01: compromissos com data entre as duas datas, incluindo as duas. */
	public List<CompromissoVo> listarPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
		String sql = COLUNAS + " WHERE c.dt_compromisso BETWEEN ? AND ?" + ORDEM;

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setObject(1, dataInicial);
			comando.setObject(2, dataFinal);

			try (ResultSet resultado = comando.executeQuery()) {
				return montarLista(resultado);
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar os compromissos do período", e);
		}
	}

	/** Quantos compromissos uma agenda tem (RN-AG-04). */
	public int contarPorAgenda(Long codigoAgenda) {
		String sql = "SELECT COUNT(*) FROM compromisso WHERE cd_agenda = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigoAgenda);

			try (ResultSet resultado = comando.executeQuery()) {
				resultado.next();
				return resultado.getInt(1);
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao contar os compromissos da agenda", e);
		}
	}

	/** Horários distintos dos compromissos de uma agenda (RN-AG-05). */
	public List<LocalTime> listarHorariosPorAgenda(Long codigoAgenda) {
		String sql = "SELECT DISTINCT hr_compromisso FROM compromisso WHERE cd_agenda = ? ORDER BY hr_compromisso";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigoAgenda);

			try (ResultSet resultado = comando.executeQuery()) {
				List<LocalTime> horarios = new ArrayList<>();

				while (resultado.next())
					horarios.add(resultado.getObject("hr_compromisso", LocalTime.class));

				return horarios;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar os horários da agenda", e);
		}
	}

	private List<CompromissoVo> montarLista(ResultSet resultado) throws SQLException {
		List<CompromissoVo> compromissos = new ArrayList<>();

		while (resultado.next())
			compromissos.add(montar(resultado));

		return compromissos;
	}

	private CompromissoVo montar(ResultSet resultado) throws SQLException {
		CompromissoVo compromisso = new CompromissoVo();
		compromisso.setCodigo(resultado.getLong("cd_compromisso"));
		compromisso.setCodigoFuncionario(resultado.getLong("cd_funcionario"));
		compromisso.setNomeFuncionario(resultado.getString("nm_funcionario"));
		compromisso.setCodigoAgenda(resultado.getLong("cd_agenda"));
		compromisso.setNomeAgenda(resultado.getString("nm_agenda"));
		compromisso.setCodigoPeriodoAgenda(resultado.getInt("cd_periodo"));
		compromisso.setData(resultado.getObject("dt_compromisso", LocalDate.class));
		compromisso.setHorario(resultado.getObject("hr_compromisso", LocalTime.class));
		return compromisso;
	}
}
