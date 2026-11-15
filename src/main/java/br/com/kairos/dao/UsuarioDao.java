package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.UsuarioVo;

/**
 * SQL da tabela usuario. Só lê e grava: nenhuma regra de negócio aqui.
 * Permissões e dados de médico ficam em PermissaoDao e MedicoDao.
 */
public class UsuarioDao extends Dao {

	private static final String COLUNAS = "SELECT cd_usuario, ds_login, nm_usuario, ds_senha, fl_ativo FROM usuario";

	/** Devolve null se o login não existe. */
	public UsuarioVo buscarPorLogin(String login) {
		String sql = COLUNAS + " WHERE ds_login = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, login);

			try (ResultSet resultado = comando.executeQuery()) {
				return resultado.next() ? montar(resultado) : null;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao buscar usuário", e);
		}
	}

	/** Devolve null se o código não existe. */
	public UsuarioVo buscarPorCodigo(Long codigo) {
		String sql = COLUNAS + " WHERE cd_usuario = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigo);

			try (ResultSet resultado = comando.executeQuery()) {
				return resultado.next() ? montar(resultado) : null;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao buscar usuário", e);
		}
	}

	public List<UsuarioVo> listarTodos() {
		String sql = COLUNAS + " ORDER BY nm_usuario";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql);
				ResultSet resultado = comando.executeQuery()) {

			List<UsuarioVo> usuarios = new ArrayList<>();
			while (resultado.next())
				usuarios.add(montar(resultado));
			return usuarios;

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar usuários", e);
		}
	}

	/**
	 * Participa de transação: usa a conexão recebida e não a fecha.
	 * Preenche o código gerado pelo banco no objeto.
	 */
	public void inserir(Connection conexao, UsuarioVo usuario) throws SQLException {
		String sql = "INSERT INTO usuario (ds_login, nm_usuario, ds_senha, fl_ativo) VALUES (?, ?, ?, ?)";

		try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			comando.setString(1, usuario.getLogin());
			comando.setString(2, usuario.getNome());
			comando.setString(3, usuario.getSenha());
			comando.setBoolean(4, usuario.isAtivo());
			comando.executeUpdate();

			try (ResultSet chaves = comando.getGeneratedKeys()) {
				if (chaves.next())
					usuario.setCodigo(chaves.getLong(1));
			}
		}
	}

	/**
	 * Participa de transação. Senha nula no objeto mantém a senha gravada.
	 * Devolve quantas linhas foram alteradas: 0 se o código não existe.
	 */
	public int alterar(Connection conexao, UsuarioVo usuario) throws SQLException {
		boolean trocaSenha = usuario.getSenha() != null;
		String sql = "UPDATE usuario SET nm_usuario = ?, fl_ativo = ?"
				+ (trocaSenha ? ", ds_senha = ?" : "")
				+ " WHERE cd_usuario = ?";

		try (PreparedStatement comando = conexao.prepareStatement(sql)) {
			int indice = 1;
			comando.setString(indice++, usuario.getNome());
			comando.setBoolean(indice++, usuario.isAtivo());
			if (trocaSenha)
				comando.setString(indice++, usuario.getSenha());
			comando.setLong(indice, usuario.getCodigo());
			return comando.executeUpdate();
		}
	}

	/**
	 * Quantos usuários ativos, fora o informado, têm a permissão (RN-PE-04).
	 * Participa de transação: a conferência e a gravação precisam ver o mesmo estado.
	 */
	public int contarAtivosComPermissao(Connection conexao, String modulo, String acao, Long excetoCodigo)
			throws SQLException {
		String sql = "SELECT COUNT(*) FROM usuario u "
				+ "JOIN permissao p ON p.cd_usuario = u.cd_usuario "
				+ "WHERE u.fl_ativo = TRUE AND p.nm_modulo = ? AND p.nm_acao = ? AND u.cd_usuario <> ?";

		try (PreparedStatement comando = conexao.prepareStatement(sql)) {
			comando.setString(1, modulo);
			comando.setString(2, acao);
			comando.setLong(3, excetoCodigo == null ? -1 : excetoCodigo);

			try (ResultSet resultado = comando.executeQuery()) {
				resultado.next();
				return resultado.getInt(1);
			}
		}
	}

	private UsuarioVo montar(ResultSet resultado) throws SQLException {
		UsuarioVo usuario = new UsuarioVo();
		usuario.setCodigo(resultado.getLong("cd_usuario"));
		usuario.setLogin(resultado.getString("ds_login"));
		usuario.setNome(resultado.getString("nm_usuario"));
		usuario.setSenha(resultado.getString("ds_senha"));
		usuario.setAtivo(resultado.getBoolean("fl_ativo"));
		return usuario;
	}
}
