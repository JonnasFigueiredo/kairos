package br.com.kairos.infra;

import java.sql.Connection;
import java.sql.SQLException;

import br.com.kairos.exception.TechnicalException;

/**
 * Executa várias gravações como uma só: ou todas acontecem, ou nenhuma.
 *
 * Uso, na camada Business:
 *
 *   Transacao.executar(conexao -> {
 *       compromissoDao.excluirPorFuncionario(conexao, codigo);
 *       funcionarioDao.excluir(conexao, codigo);
 *   });
 *
 * Se qualquer passo falhar, o rollback desfaz os anteriores.
 */
public final class Transacao {

	/** O conjunto de gravações. Recebe a conexão que todas devem usar. */
	public interface Operacao {
		void executar(Connection conexao) throws SQLException;
	}

	private Transacao() {
		// só métodos estáticos
	}

	public static void executar(Operacao operacao) {
		try (Connection conexao = Conexao.abrir()) {
			// Desliga a confirmação automática: nada é gravado de vez até o commit
			conexao.setAutoCommit(false);
			try {
				operacao.executar(conexao);
				conexao.commit();
			} catch (SQLException | RuntimeException e) {
				desfazer(conexao, e);
				throw e;
			}
		} catch (SQLException e) {
			throw new TechnicalException("Erro ao executar a transação", e);
		}
	}

	/**
	 * Se o próprio rollback falhar (a conexão caiu, por exemplo), o erro dele é
	 * anexado ao erro original, que continua sendo o que sobe e vai para o log.
	 */
	private static void desfazer(Connection conexao, Exception erroOriginal) {
		try {
			conexao.rollback();
		} catch (SQLException erroDoRollback) {
			erroOriginal.addSuppressed(erroDoRollback);
		}
	}
}
