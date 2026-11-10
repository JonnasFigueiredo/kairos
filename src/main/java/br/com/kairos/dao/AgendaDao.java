package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.AgendaVo;

/**
 * SQL da tabela agenda. Só lê e grava: nenhuma regra de negócio aqui.
 */
public class AgendaDao extends Dao {

	private static final String COLUNAS = "SELECT cd_agenda, nm_agenda, cd_periodo FROM agenda";

	public void inserir(AgendaVo agenda) {
		String sql = "INSERT INTO agenda (nm_agenda, cd_periodo) VALUES (?, ?)";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, agenda.getNome());
			comando.setInt(2, agenda.getCodigoPeriodo());
			comando.executeUpdate();

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao inserir agenda", e);
		}
	}

	/** Devolve quantas linhas foram alteradas: 0 se o código não existe. */
	public int alterar(AgendaVo agenda) {
		String sql = "UPDATE agenda SET nm_agenda = ?, cd_periodo = ? WHERE cd_agenda = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, agenda.getNome());
			comando.setInt(2, agenda.getCodigoPeriodo());
			comando.setLong(3, agenda.getCodigo());
			return comando.executeUpdate();

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao alterar agenda", e);
		}
	}

	/** Devolve quantas linhas foram excluídas: 0 se o código não existe. */
	public int excluir(Long codigo) {
		String sql = "DELETE FROM agenda WHERE cd_agenda = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigo);
			return comando.executeUpdate();

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao excluir agenda", e);
		}
	}

	/** Devolve null se o código não existe. */
	public AgendaVo buscarPorCodigo(Long codigo) {
		String sql = COLUNAS + " WHERE cd_agenda = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigo);

			try (ResultSet resultado = comando.executeQuery()) {
				return resultado.next() ? montar(resultado) : null;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao buscar agenda", e);
		}
	}

	public List<AgendaVo> listarTodas() {
		String sql = COLUNAS + " ORDER BY nm_agenda";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql);
				ResultSet resultado = comando.executeQuery()) {

			return montarLista(resultado);

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar agendas", e);
		}
	}

	public List<AgendaVo> listarPorNome(String trecho) {
		String sql = COLUNAS + " WHERE nm_agenda LIKE ? ORDER BY nm_agenda";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, contendo(trecho));

			try (ResultSet resultado = comando.executeQuery()) {
				return montarLista(resultado);
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao pesquisar agendas", e);
		}
	}

	private List<AgendaVo> montarLista(ResultSet resultado) throws SQLException {
		List<AgendaVo> agendas = new ArrayList<>();

		while (resultado.next())
			agendas.add(montar(resultado));

		return agendas;
	}

	private AgendaVo montar(ResultSet resultado) throws SQLException {
		AgendaVo agenda = new AgendaVo();
		agenda.setCodigo(resultado.getLong("cd_agenda"));
		agenda.setNome(resultado.getString("nm_agenda"));
		agenda.setCodigoPeriodo(resultado.getInt("cd_periodo"));
		return agenda;
	}
}
