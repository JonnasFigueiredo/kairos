package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import br.com.kairos.exception.TechnicalException;

/**
 * SQL da tabela analise_ia: auditoria de cada análise pedida (RN-IA-05) e base do limite diário (RN-IA-04).
 */
public class AnaliseIaDao extends Dao {

	/** Uma linha por análise, com sucesso ou com erro. */
	public void registrar(Long codigoCompromisso, Long codigoMedico, String provedor, String contextoEnviado,
			String resposta, boolean sucesso, String erro, Long tokensEntrada, Long tokensSaida, int duracaoMs,
			LocalDateTime criadoEm) {
		String sql = "INSERT INTO analise_ia (cd_compromisso, cd_medico, ds_provedor, ds_contexto, ds_resposta, "
				+ "fl_sucesso, ds_erro, nr_tokens_entrada, nr_tokens_saida, nr_duracao_ms, dt_criacao) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigoCompromisso);
			comando.setLong(2, codigoMedico);
			comando.setString(3, provedor);
			comando.setString(4, contextoEnviado);
			comando.setString(5, resposta);
			comando.setBoolean(6, sucesso);
			comando.setString(7, erro);
			comando.setObject(8, tokensEntrada);
			comando.setObject(9, tokensSaida);
			comando.setInt(10, duracaoMs);
			comando.setObject(11, criadoEm);
			comando.executeUpdate();

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao registrar a análise da IA", e);
		}
	}

	/** RN-IA-04: quantas análises o médico pediu desde o instante informado (as que falharam também contam). */
	public int contarDesde(Long codigoMedico, LocalDateTime desde) {
		String sql = "SELECT COUNT(*) FROM analise_ia WHERE cd_medico = ? AND dt_criacao >= ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigoMedico);
			comando.setObject(2, desde);

			try (ResultSet resultado = comando.executeQuery()) {
				resultado.next();
				return resultado.getInt(1);
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao contar as análises do dia", e);
		}
	}

	/** RN-AT-05: as análises de um compromisso vão junto com ele. Participa de transação. */
	public void excluirPorCompromisso(Connection conexao, Long codigoCompromisso) throws SQLException {
		try (PreparedStatement comando = conexao.prepareStatement("DELETE FROM analise_ia WHERE cd_compromisso = ?")) {
			comando.setLong(1, codigoCompromisso);
			comando.executeUpdate();
		}
	}

	/** RN-AT-05: as análises dos compromissos de um funcionário. Participa de transação. */
	public void excluirPorFuncionario(Connection conexao, Long codigoFuncionario) throws SQLException {
		String sql = "DELETE a FROM analise_ia a JOIN compromisso c ON c.cd_compromisso = a.cd_compromisso "
				+ "WHERE c.cd_funcionario = ?";

		try (PreparedStatement comando = conexao.prepareStatement(sql)) {
			comando.setLong(1, codigoFuncionario);
			comando.executeUpdate();
		}
	}
}
