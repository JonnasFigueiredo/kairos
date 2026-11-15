package br.com.kairos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.exception.TechnicalException;
import br.com.kairos.vo.CompromissoVo;
import br.com.kairos.vo.ResultadoVo;
import br.com.kairos.vo.VersaoResultadoVo;

/**
 * SQL das tabelas resultado e resultado_versao, e as consultas da fila do médico.
 * Só lê e grava: nenhuma regra de negócio aqui.
 */
public class ResultadoDao extends Dao {

	/** Toda consulta de resultado traz junto o compromisso, o funcionário e a agenda. */
	private static final String COLUNAS =
			"SELECT r.cd_resultado, r.cd_compromisso, r.cd_medico, r.cd_indicador, r.ds_achados, r.ds_parecer, "
			+ "r.cd_situacao, r.nr_versao, r.ds_assinatura, r.dt_liberacao, r.dt_atualizacao, "
			+ "c.cd_funcionario, f.nm_funcionario, a.nm_agenda, c.dt_compromisso, c.hr_compromisso "
			+ "FROM resultado r "
			+ "JOIN compromisso c ON c.cd_compromisso = r.cd_compromisso "
			+ "JOIN funcionario f ON f.cd_funcionario = c.cd_funcionario "
			+ "JOIN agenda a ON a.cd_agenda = c.cd_agenda";

	/** Compromissos com a situação do resultado ao lado (nula quando ainda não há resultado). */
	private static final String COLUNAS_FILA =
			"SELECT c.cd_compromisso, c.cd_funcionario, f.nm_funcionario, c.cd_agenda, a.nm_agenda, a.cd_periodo, "
			+ "c.dt_compromisso, c.hr_compromisso, r.cd_situacao "
			+ "FROM compromisso c "
			+ "JOIN funcionario f ON f.cd_funcionario = c.cd_funcionario "
			+ "JOIN agenda a ON a.cd_agenda = c.cd_agenda "
			+ "LEFT JOIN resultado r ON r.cd_compromisso = c.cd_compromisso";

	/** Devolve null se o compromisso ainda não tem resultado. */
	public ResultadoVo buscarPorCompromisso(Long codigoCompromisso) {
		return buscarUm(COLUNAS + " WHERE r.cd_compromisso = ?", codigoCompromisso);
	}

	/** Devolve null se o código não existe. */
	public ResultadoVo buscarPorCodigo(Long codigo) {
		return buscarUm(COLUNAS + " WHERE r.cd_resultado = ?", codigo);
	}

	private ResultadoVo buscarUm(String sql, Long parametro) {
		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, parametro);

			try (ResultSet resultado = comando.executeQuery()) {
				return resultado.next() ? montar(resultado) : null;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao buscar resultado", e);
		}
	}

	/** RN-AT-07: os compromissos de um dia, com a situação do resultado. */
	public List<CompromissoVo> listarDoDia(LocalDate dia) {
		String sql = COLUNAS_FILA + " WHERE c.dt_compromisso = ? ORDER BY c.hr_compromisso, f.nm_funcionario";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setObject(1, dia);

			try (ResultSet resultado = comando.executeQuery()) {
				return montarFila(resultado);
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar os compromissos do dia", e);
		}
	}

	/** RN-AT-07: compromissos anteriores ao dia, sem resultado liberado, do mais novo para o mais antigo. */
	public List<CompromissoVo> listarPendentesAntesDe(LocalDate dia, int maximo) {
		String sql = COLUNAS_FILA + " WHERE c.dt_compromisso < ? AND (r.cd_situacao IS NULL OR r.cd_situacao <> ?) "
				+ "ORDER BY c.dt_compromisso DESC, c.hr_compromisso DESC LIMIT ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setObject(1, dia);
			comando.setString(2, ResultadoVo.LIBERADO);
			comando.setInt(3, maximo);

			try (ResultSet resultado = comando.executeQuery()) {
				return montarFila(resultado);
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar os atendimentos pendentes", e);
		}
	}

	/**
	 * Histórico do funcionário: os outros compromissos dele, com o resultado quando houver
	 * (código nulo = sem resultado). Do mais novo para o mais antigo.
	 */
	public List<ResultadoVo> listarHistorico(Long codigoFuncionario, Long excetoCompromisso) {
		String sql = "SELECT r.cd_resultado, r.cd_compromisso AS cd_resultado_compromisso, r.cd_medico, r.cd_indicador, "
				+ "r.ds_achados, r.ds_parecer, r.cd_situacao, r.nr_versao, r.ds_assinatura, r.dt_liberacao, r.dt_atualizacao, "
				+ "c.cd_compromisso, c.cd_funcionario, f.nm_funcionario, a.nm_agenda, c.dt_compromisso, c.hr_compromisso "
				+ "FROM compromisso c "
				+ "JOIN funcionario f ON f.cd_funcionario = c.cd_funcionario "
				+ "JOIN agenda a ON a.cd_agenda = c.cd_agenda "
				+ "LEFT JOIN resultado r ON r.cd_compromisso = c.cd_compromisso "
				+ "WHERE c.cd_funcionario = ? AND c.cd_compromisso <> ? "
				+ "ORDER BY c.dt_compromisso DESC, c.hr_compromisso DESC";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigoFuncionario);
			comando.setLong(2, excetoCompromisso == null ? -1 : excetoCompromisso);

			try (ResultSet resultado = comando.executeQuery()) {
				List<ResultadoVo> historico = new ArrayList<>();
				while (resultado.next()) {
					ResultadoVo item = montar(resultado);
					// Sem resultado, o LEFT JOIN devolve 0 em cd_resultado: fica nulo
					if (resultado.getObject("cd_resultado") == null) {
						item.setCodigo(null);
						item.setSituacao(null);
					}
					historico.add(item);
				}
				return historico;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar o histórico do funcionário", e);
		}
	}

	/** RN-AT-06: resultados liberados, para quem tem leitura. Trecho vazio traz todos. */
	public List<ResultadoVo> listarLiberados(String trecho) {
		boolean filtra = trecho != null && !trecho.trim().isEmpty();
		String sql = COLUNAS + " WHERE r.cd_situacao = ?"
				+ (filtra ? " AND (f.nm_funcionario LIKE ? OR a.nm_agenda LIKE ?)" : "")
				+ " ORDER BY r.dt_liberacao DESC";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setString(1, ResultadoVo.LIBERADO);
			if (filtra) {
				comando.setString(2, contendo(trecho.trim()));
				comando.setString(3, contendo(trecho.trim()));
			}

			try (ResultSet resultado = comando.executeQuery()) {
				List<ResultadoVo> resultados = new ArrayList<>();
				while (resultado.next())
					resultados.add(montar(resultado));
				return resultados;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar os resultados", e);
		}
	}

	public List<VersaoResultadoVo> listarVersoes(Long codigoResultado) {
		String sql = "SELECT cd_versao, cd_resultado, nr_versao, cd_indicador, ds_achados, ds_parecer, ds_assinatura, "
				+ "dt_liberacao, ds_motivo, ds_retificado_por, dt_retificacao "
				+ "FROM resultado_versao WHERE cd_resultado = ? ORDER BY nr_versao DESC";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigoResultado);

			try (ResultSet resultado = comando.executeQuery()) {
				List<VersaoResultadoVo> versoes = new ArrayList<>();
				while (resultado.next()) {
					VersaoResultadoVo versao = new VersaoResultadoVo();
					versao.setCodigo(resultado.getLong("cd_versao"));
					versao.setCodigoResultado(resultado.getLong("cd_resultado"));
					versao.setVersao(resultado.getInt("nr_versao"));
					versao.setIndicador(resultado.getString("cd_indicador"));
					versao.setAchados(resultado.getString("ds_achados"));
					versao.setParecer(resultado.getString("ds_parecer"));
					versao.setAssinatura(resultado.getString("ds_assinatura"));
					versao.setLiberadoEm(resultado.getObject("dt_liberacao", LocalDateTime.class));
					versao.setMotivo(resultado.getString("ds_motivo"));
					versao.setRetificadoPor(resultado.getString("ds_retificado_por"));
					versao.setRetificadoEm(resultado.getObject("dt_retificacao", LocalDateTime.class));
					versoes.add(versao);
				}
				return versoes;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao listar as versões do resultado", e);
		}
	}

	/** O compromisso já tem resultado (RN-AT-04)? */
	public boolean existePorCompromisso(Long codigoCompromisso) {
		String sql = "SELECT COUNT(*) FROM resultado WHERE cd_compromisso = ?";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setLong(1, codigoCompromisso);

			try (ResultSet resultado = comando.executeQuery()) {
				resultado.next();
				return resultado.getInt(1) > 0;
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao conferir o resultado do compromisso", e);
		}
	}

	/** Preenche o código gerado pelo banco no objeto. */
	public void inserir(ResultadoVo r) {
		String sql = "INSERT INTO resultado (cd_compromisso, cd_medico, cd_indicador, ds_achados, ds_parecer, "
				+ "cd_situacao, nr_versao, ds_assinatura, dt_liberacao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conexao = abrirConexao();
				PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			comando.setLong(1, r.getCodigoCompromisso());
			comando.setLong(2, r.getCodigoMedico());
			comando.setString(3, r.getIndicador());
			comando.setString(4, r.getAchados());
			comando.setString(5, r.getParecer());
			comando.setString(6, r.getSituacao());
			comando.setInt(7, r.getVersao());
			comando.setString(8, r.getAssinatura());
			comando.setObject(9, r.getLiberadoEm());
			comando.executeUpdate();

			try (ResultSet chaves = comando.getGeneratedKeys()) {
				if (chaves.next())
					r.setCodigo(chaves.getLong(1));
			}

		} catch (SQLException e) {
			throw new TechnicalException("Erro ao gravar o resultado", e);
		}
	}

	/** Devolve quantas linhas foram alteradas: 0 se o código não existe. */
	public int alterar(ResultadoVo r) {
		try (Connection conexao = abrirConexao()) {
			return alterar(conexao, r);
		} catch (SQLException e) {
			throw new TechnicalException("Erro ao alterar o resultado", e);
		}
	}

	/** Participa de transação: usa a conexão recebida e não a fecha. */
	public int alterar(Connection conexao, ResultadoVo r) throws SQLException {
		String sql = "UPDATE resultado SET cd_medico = ?, cd_indicador = ?, ds_achados = ?, ds_parecer = ?, "
				+ "cd_situacao = ?, nr_versao = ?, ds_assinatura = ?, dt_liberacao = ? WHERE cd_resultado = ?";

		try (PreparedStatement comando = conexao.prepareStatement(sql)) {
			comando.setLong(1, r.getCodigoMedico());
			comando.setString(2, r.getIndicador());
			comando.setString(3, r.getAchados());
			comando.setString(4, r.getParecer());
			comando.setString(5, r.getSituacao());
			comando.setInt(6, r.getVersao());
			comando.setString(7, r.getAssinatura());
			comando.setObject(8, r.getLiberadoEm());
			comando.setLong(9, r.getCodigo());
			return comando.executeUpdate();
		}
	}

	/** RN-AT-03: guarda a versão substituída. Participa de transação. */
	public void inserirVersao(Connection conexao, VersaoResultadoVo v) throws SQLException {
		String sql = "INSERT INTO resultado_versao (cd_resultado, nr_versao, cd_indicador, ds_achados, ds_parecer, "
				+ "ds_assinatura, dt_liberacao, ds_motivo, ds_retificado_por, dt_retificacao) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement comando = conexao.prepareStatement(sql)) {
			comando.setLong(1, v.getCodigoResultado());
			comando.setInt(2, v.getVersao());
			comando.setString(3, v.getIndicador());
			comando.setString(4, v.getAchados());
			comando.setString(5, v.getParecer());
			comando.setString(6, v.getAssinatura());
			comando.setObject(7, v.getLiberadoEm());
			comando.setString(8, v.getMotivo());
			comando.setString(9, v.getRetificadoPor());
			comando.setObject(10, v.getRetificadoEm());
			comando.executeUpdate();
		}
	}

	/**
	 * RN-AT-05: apaga o resultado e as versões de um compromisso. Participa de transação.
	 * Primeiro as versões: a chave estrangeira impede excluir o resultado antes.
	 */
	public void excluirPorCompromisso(Connection conexao, Long codigoCompromisso) throws SQLException {
		String versoes = "DELETE v FROM resultado_versao v JOIN resultado r ON r.cd_resultado = v.cd_resultado "
				+ "WHERE r.cd_compromisso = ?";
		String resultados = "DELETE FROM resultado WHERE cd_compromisso = ?";

		try (PreparedStatement comando = conexao.prepareStatement(versoes)) {
			comando.setLong(1, codigoCompromisso);
			comando.executeUpdate();
		}
		try (PreparedStatement comando = conexao.prepareStatement(resultados)) {
			comando.setLong(1, codigoCompromisso);
			comando.executeUpdate();
		}
	}

	/** RN-AT-05: apaga os resultados e versões de todos os compromissos de um funcionário. Participa de transação. */
	public void excluirPorFuncionario(Connection conexao, Long codigoFuncionario) throws SQLException {
		String versoes = "DELETE v FROM resultado_versao v "
				+ "JOIN resultado r ON r.cd_resultado = v.cd_resultado "
				+ "JOIN compromisso c ON c.cd_compromisso = r.cd_compromisso WHERE c.cd_funcionario = ?";
		String resultados = "DELETE r FROM resultado r "
				+ "JOIN compromisso c ON c.cd_compromisso = r.cd_compromisso WHERE c.cd_funcionario = ?";

		try (PreparedStatement comando = conexao.prepareStatement(versoes)) {
			comando.setLong(1, codigoFuncionario);
			comando.executeUpdate();
		}
		try (PreparedStatement comando = conexao.prepareStatement(resultados)) {
			comando.setLong(1, codigoFuncionario);
			comando.executeUpdate();
		}
	}

	private List<CompromissoVo> montarFila(ResultSet resultado) throws SQLException {
		List<CompromissoVo> compromissos = new ArrayList<>();

		while (resultado.next()) {
			CompromissoVo compromisso = new CompromissoVo();
			compromisso.setCodigo(resultado.getLong("cd_compromisso"));
			compromisso.setCodigoFuncionario(resultado.getLong("cd_funcionario"));
			compromisso.setNomeFuncionario(resultado.getString("nm_funcionario"));
			compromisso.setCodigoAgenda(resultado.getLong("cd_agenda"));
			compromisso.setNomeAgenda(resultado.getString("nm_agenda"));
			compromisso.setCodigoPeriodoAgenda(resultado.getInt("cd_periodo"));
			compromisso.setData(resultado.getObject("dt_compromisso", LocalDate.class));
			compromisso.setHorario(resultado.getObject("hr_compromisso", LocalTime.class));
			compromisso.setSituacaoResultado(resultado.getString("cd_situacao"));
			compromissos.add(compromisso);
		}
		return compromissos;
	}

	private ResultadoVo montar(ResultSet resultado) throws SQLException {
		ResultadoVo r = new ResultadoVo();
		r.setCodigo(resultado.getLong("cd_resultado"));
		r.setCodigoCompromisso(resultado.getLong("cd_compromisso"));
		r.setCodigoMedico(resultado.getLong("cd_medico"));
		r.setIndicador(resultado.getString("cd_indicador"));
		r.setAchados(resultado.getString("ds_achados"));
		r.setParecer(resultado.getString("ds_parecer"));
		r.setSituacao(resultado.getString("cd_situacao"));
		r.setVersao(resultado.getInt("nr_versao"));
		r.setAssinatura(resultado.getString("ds_assinatura"));
		r.setLiberadoEm(resultado.getObject("dt_liberacao", LocalDateTime.class));
		r.setAtualizadoEm(resultado.getObject("dt_atualizacao", LocalDateTime.class));
		r.setCodigoFuncionario(resultado.getLong("cd_funcionario"));
		r.setNomeFuncionario(resultado.getString("nm_funcionario"));
		r.setNomeAgenda(resultado.getString("nm_agenda"));
		r.setDataCompromisso(resultado.getObject("dt_compromisso", LocalDate.class));
		r.setHorarioCompromisso(resultado.getObject("hr_compromisso", LocalTime.class));
		return r;
	}
}
