package br.com.kairos.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import br.com.kairos.exception.TechnicalException;

/**
 * Único lugar que sabe o endereço do banco e abre conexão com o MySQL.
 * Usado pelos Daos (uma operação) e pela Transacao (várias operações juntas).
 */
public final class Conexao {

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

	private Conexao() {
		// só métodos estáticos
	}

	public static Connection abrir() throws SQLException {
		return DriverManager.getConnection(URL, USUARIO, SENHA);
	}
}
