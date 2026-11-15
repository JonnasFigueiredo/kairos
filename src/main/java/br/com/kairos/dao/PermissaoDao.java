package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import br.com.kairos.exception.TechnicalException;

/**
 * SQL da tabela permissao: uma linha por ação liberada de um módulo para um usuário.
 * As chaves trafegam no formato MODULO:ACAO (ver Modulo.chave).
 */
public class PermissaoDao extends Dao {

	public Set<String> listarPorUsuario(Long codigoUsuario) {
		String sql = "SELECT nm_modulo, nm_acao FROM permissao WHERE cd_usuario = ? ORDER BY nm_modulo, nm_acao";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigoUsuario);

			try (ResultSet resultado = comando.executeQuery()) {
				Set<String> permissoes = new LinkedHashSet<>();
				while (resultado.next())
					permissoes.add(resultado.getString("nm_modulo") + ":" + resultado.getString("nm_acao"));
				return permissoes;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar as permissões do usuário", e);
		}
	}

	/**
	 * Troca todas as permissões do usuário pelas informadas.
	 * Participa de transação: usa a conexão recebida e não a fecha.
	 */
	public void substituir(Connection conexao, Long codigoUsuario, Set<String> permissoes) throws SQLException {
		try (PreparedStatement apagar = conexao.prepareStatement("DELETE FROM permissao WHERE cd_usuario = ?")) {
			apagar.setLong(1, codigoUsuario);
			apagar.executeUpdate();
		}

		String sql = "INSERT INTO permissao (cd_usuario, nm_modulo, nm_acao) VALUES (?, ?, ?)";
		try (PreparedStatement inserir = conexao.prepareStatement(sql)) {
			for (String chave : permissoes) {
				String[] partes = chave.split(":", 2);
				inserir.setLong(1, codigoUsuario);
				inserir.setString(2, partes[0]);
				inserir.setString(3, partes[1]);
				inserir.addBatch();
			}
			inserir.executeBatch();
		}
	}
}
