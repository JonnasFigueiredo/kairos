package br.com.kairos.business;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.dao.AnaliseIaDao;
import br.com.kairos.dao.CompromissoDao;
import br.com.kairos.dao.ResultadoDao;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Periodo;
import br.com.kairos.infra.Transacao;
import br.com.kairos.vo.AgendaVo;
import br.com.kairos.vo.CompromissoVo;

/**
 * Regras do cadastro de compromissos.
 */
public class CompromissoBusiness {

	private final CompromissoDao dao = new CompromissoDao();
	/** Só dentro da transação de exclusão (RN-AT-05); o resto passa pela AtendimentoBusiness. */
	private final ResultadoDao resultadoDao = new ResultadoDao();
	private final AnaliseIaDao analiseIaDao = new AnaliseIaDao();
	private final FuncionarioBusiness funcionarioBusiness = new FuncionarioBusiness();
	private final AgendaBusiness agendaBusiness = new AgendaBusiness();

	/**
	 * Pesquisa em um campo só: vazio traz todos, número procura pelo código,
	 * texto procura no nome do funcionário ou da agenda.
	 */
	public List<CompromissoVo> pesquisar(String busca) {
		if (busca == null || busca.trim().isEmpty())
			return dao.listarTodos();

		String termo = busca.trim();

		if (termo.matches("\\d{1,18}")) {
			List<CompromissoVo> encontrados = new ArrayList<>();
			CompromissoVo compromisso = dao.buscarPorCodigo(Long.valueOf(termo));
			if (compromisso != null)
				encontrados.add(compromisso);
			return encontrados;
		}

		return dao.listarPorNome(termo);
	}

	public CompromissoVo buscar(Long codigo) {
		CompromissoVo compromisso = codigo == null ? null : dao.buscarPorCodigo(codigo);

		if (compromisso == null)
			throw new BusinessException("Compromisso não encontrado.");

		return compromisso;
	}

	/** Inclui quando o código é nulo; altera quando o código vem preenchido. */
	public void salvar(CompromissoVo compromisso) {
		validarObrigatorios(compromisso);

		// RN-CO-03: o funcionário e a agenda precisam existir. "buscar" lança erro se não existirem.
		funcionarioBusiness.buscar(compromisso.getCodigoFuncionario());
		AgendaVo agenda = agendaBusiness.buscar(compromisso.getCodigoAgenda());

		validarHorario(agenda, compromisso.getHorario());

		if (compromisso.getCodigo() == null) {
			dao.inserir(compromisso);
			return;
		}

		// RN-AT-04: o resultado foi lançado para este funcionário, nesta agenda e nesta data
		if (atendimento().temResultado(compromisso.getCodigo()))
			throw new BusinessException("Compromisso com resultado não pode ser alterado.");

		if (dao.alterar(compromisso) == 0)
			throw new BusinessException("Compromisso não encontrado.");
	}

	/**
	 * RN-AT-05: excluir o compromisso apaga também o resultado e as versões dele,
	 * na mesma transação.
	 */
	public void excluir(Long codigo) {
		if (codigo == null)
			throw new BusinessException("Compromisso não encontrado.");

		Transacao.executar(conexao -> {
			// Primeiro as análises e o resultado: as chaves estrangeiras impedem excluir o compromisso antes
			analiseIaDao.excluirPorCompromisso(conexao, codigo);
			resultadoDao.excluirPorCompromisso(conexao, codigo);

			if (dao.excluir(conexao, codigo) == 0)
				throw new BusinessException("Compromisso não encontrado.");
		});
	}

	/**
	 * Criado só quando precisa: AtendimentoBusiness também cria um CompromissoBusiness,
	 * e os dois se criando no construtor nunca terminariam.
	 */
	private AtendimentoBusiness atendimento() {
		return new AtendimentoBusiness();
	}

	/** Usado pelo relatório (RN-RE-01). */
	public List<CompromissoVo> listarDoPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
		return dao.listarPorPeriodo(dataInicial, dataFinal);
	}

	/** Usado pelo cadastro de agendas (RN-AG-04). */
	public int contarDaAgenda(Long codigoAgenda) {
		return dao.contarPorAgenda(codigoAgenda);
	}

	/** Usado pelo cadastro de agendas (RN-AG-05). */
	public List<LocalTime> horariosDaAgenda(Long codigoAgenda) {
		return dao.listarHorariosPorAgenda(codigoAgenda);
	}

	/** RN-CO-02: funcionário, agenda, data e horário são obrigatórios. */
	void validarObrigatorios(CompromissoVo compromisso) {
		if (compromisso.getCodigoFuncionario() == null)
			throw new BusinessException("Selecione o funcionário.");

		if (compromisso.getCodigoAgenda() == null)
			throw new BusinessException("Selecione a agenda.");

		if (compromisso.getData() == null)
			throw new BusinessException("Informe a data.");

		if (compromisso.getHorario() == null)
			throw new BusinessException("Informe o horário.");
	}

	/**
	 * RN-CO-04: o horário precisa estar dentro do período disponível da agenda.
	 * Vale na inclusão e na alteração, porque "salvar" passa por aqui nos dois casos.
	 */
	void validarHorario(AgendaVo agenda, LocalTime horario) {
		Periodo periodo = agenda.getPeriodo();

		if (periodo.aceita(horario))
			return;

		String limite = periodo == Periodo.MANHA ? "até 11:59" : "a partir das 12:00";

		throw new BusinessException("A agenda " + agenda.getNome() + " atende só no período "
				+ periodo.getDescricao() + " (" + limite + ").");
	}
}
