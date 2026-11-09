package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
