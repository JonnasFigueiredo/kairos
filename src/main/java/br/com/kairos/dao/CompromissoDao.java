package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.exception.TechnicalException;

/**
 * SQL da tabela compromisso.
 */
public class CompromissoDao extends Dao {

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
}
