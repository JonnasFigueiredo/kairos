package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.SQLException;

import br.com.kairos.infra.Conexao;

/**
 * Classe base de todos os Daos.
 *
 * Cada método de um Dao abre a conexão dentro de um try-with-resources,
 * usa e fecha. Nenhuma conexão fica guardada em atributo.
 *
 * Método que participa de uma transação não abre conexão: recebe a
 * conexão da Transacao por parâmetro.
 */
public abstract class Dao {

	protected Connection abrirConexao() throws SQLException {
		return Conexao.abrir();
	}
}
