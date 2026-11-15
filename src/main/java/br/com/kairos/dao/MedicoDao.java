package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.MedicoVo;

/**
 * SQL da tabela medico: dados profissionais de um usuário (RN-ME-01).
 */
public class MedicoDao extends Dao {

	/** Devolve null se o usuário não é médico. */
	public MedicoVo buscarPorUsuario(Long codigoUsuario) {
		String sql = "SELECT nr_crm, sg_uf_crm, ds_especialidade FROM medico WHERE cd_usuario = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigoUsuario);

			try (ResultSet resultado = comando.executeQuery()) {
				if (!resultado.next())
					return null;

				MedicoVo medico = new MedicoVo();
				medico.setCrm(resultado.getString("nr_crm"));
				medico.setUfCrm(resultado.getString("sg_uf_crm"));
				medico.setEspecialidade(resultado.getString("ds_especialidade"));
				return medico;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao buscar os dados do médico", e);
		}
	}

	/** RN-ME-02: existe outro usuário com o mesmo CRM na mesma UF? */
	public boolean existeCrm(String crm, String uf, Long excetoUsuario) {
		String sql = "SELECT COUNT(*) FROM medico WHERE nr_crm = ? AND sg_uf_crm = ? AND cd_usuario <> ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, crm);
			comando.setString(2, uf);
			comando.setLong(3, excetoUsuario == null ? -1 : excetoUsuario);

			try (ResultSet resultado = comando.executeQuery()) {
				resultado.next();
				return resultado.getInt(1) > 0;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao conferir o CRM", e);
		}
	}

	/** Insere ou atualiza. Participa de transação. */
	public void gravar(Connection conexao, Long codigoUsuario, MedicoVo medico) throws SQLException {
		String sql = "INSERT INTO medico (cd_usuario, nr_crm, sg_uf_crm, ds_especialidade) VALUES (?, ?, ?, ?) "
				+ "ON DUPLICATE KEY UPDATE nr_crm = VALUES(nr_crm), sg_uf_crm = VALUES(sg_uf_crm), "
				+ "ds_especialidade = VALUES(ds_especialidade)";

		try (PreparedStatement comando = conexao.prepareStatement(sql)) {
			comando.setLong(1, codigoUsuario);
			comando.setString(2, medico.getCrm());
			comando.setString(3, medico.getUfCrm());
			comando.setString(4, medico.getEspecialidade());
			comando.executeUpdate();
		}
	}

	/** RN-ME-04: tirar os dados de médico volta a conta a ser comum. Participa de transação. */
	public void excluir(Connection conexao, Long codigoUsuario) throws SQLException {
		try (PreparedStatement comando = conexao.prepareStatement("DELETE FROM medico WHERE cd_usuario = ?")) {
			comando.setLong(1, codigoUsuario);
			comando.executeUpdate();
		}
	}
}
