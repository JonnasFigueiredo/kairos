package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.UsuarioVo;

/**
 * SQL da tabela usuario. Só lê: nenhuma regra de negócio aqui.
 */
public class UsuarioDao extends Dao {

	/** Devolve null se o login não existe. */
	public UsuarioVo buscarPorLogin(String login) {
		String sql = "SELECT cd_usuario, ds_login, nm_usuario, ds_senha FROM usuario WHERE ds_login = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, login);

			try (ResultSet resultado = comando.executeQuery()) {
				if (!resultado.next())
					return null;

				UsuarioVo usuario = new UsuarioVo();
				usuario.setCodigo(resultado.getLong("cd_usuario"));
				usuario.setLogin(resultado.getString("ds_login"));
				usuario.setNome(resultado.getString("nm_usuario"));
				usuario.setSenha(resultado.getString("ds_senha"));
				return usuario;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao buscar usuário", e);
		}
	}
}
