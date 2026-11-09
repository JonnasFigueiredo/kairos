package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.ResumoVo;

/**
 * Conta os registros de cada cadastro para a página inicial.
 */
public class ResumoDao extends Dao {

	private static final String SQL_TOTAIS =
			"SELECT (SELECT COUNT(*) FROM funcionario) AS funcionarios, "
			+ "(SELECT COUNT(*) FROM agenda) AS agendas, "
			+ "(SELECT COUNT(*) FROM compromisso) AS compromissos";

	public ResumoVo contarRegistros() {
		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(SQL_TOTAIS);
				ResultSet resultado = comando.executeQuery()) {

			resultado.next();

			ResumoVo resumo = new ResumoVo();
			resumo.setTotalFuncionarios(resultado.getInt("funcionarios"));
			resumo.setTotalAgendas(resultado.getInt("agendas"));
			resumo.setTotalCompromissos(resultado.getInt("compromissos"));
			return resumo;

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao contar os registros", e);
		}
	}
}
