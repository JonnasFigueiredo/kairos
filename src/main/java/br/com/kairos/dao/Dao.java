package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.infra.Configuracao;

/**
 * Classe base de todos os Daos: sabe abrir conexão com o MySQL.
 *
 * Cada método de um Dao abre a conexão dentro de um try-with-resources,
 * usa e fecha. Nenhuma conexão fica guardada em atributo.
 */
public abstract class Dao {

	private static final String URL = "jdbc:mysql://"
			+ Configuracao.obter("DB_HOST", "localhost") + ":"
			+ Configuracao.obter("DB_PORT", "3306") + "/"
			+ Configuracao.obter("DB_NAME", "kairos")
			+ "?characterEncoding=UTF-8";

	private static final String USUARIO = Configuracao.obter("DB_USER", "kairos");
	private static final String SENHA = Configuracao.obter("DB_PASSWORD", "kairos");

	static {
		try {
			// Registra o driver do MySQL. Dentro do servidor web o registro automático pode falhar.
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new TechnicalException("Driver do MySQL não encontrado", e);
		}
	}

	protected Connection abrirConexao() throws SQLException {
		return DriverManager.getConnection(URL, USUARIO, SENHA);
	}
}
