package br.com.kairos.business;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import br.com.kairos.dao.AgendaDao;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Periodo;
import br.com.kairos.vo.AgendaVo;

/**
 * Regras do cadastro de agendas.
 */
public class AgendaBusiness {

	static final int TAMANHO_MAXIMO_DO_NOME = 255;

	private final AgendaDao dao = new AgendaDao();

	/**
	 * Pesquisa em um campo só: vazio traz todas, número procura pelo código,
	 * texto procura pelo nome.
	 */
	public List<AgendaVo> pesquisar(String busca) {
		if (busca == null || busca.trim().isEmpty())
			return dao.listarTodas();

		String termo = busca.trim();

		if (termo.matches("\\d{1,18}")) {
			List<AgendaVo> encontradas = new ArrayList<>();
			AgendaVo agenda = dao.buscarPorCodigo(Long.valueOf(termo));
			if (agenda != null)
				encontradas.add(agenda);
			return encontradas;
		}

		return dao.listarPorNome(termo);
	}

	public AgendaVo buscar(Long codigo) {
		AgendaVo agenda = codigo == null ? null : dao.buscarPorCodigo(codigo);

		if (agenda == null)
			throw new BusinessException("Agenda não encontrada.");

		return agenda;
	}

	/** Inclui quando o código é nulo; altera quando o código vem preenchido. */
	public void salvar(AgendaVo agenda) {
		validar(agenda);

		if (agenda.getCodigo() == null) {
			dao.inserir(agenda);
			return;
		}

		validarTrocaDePeriodo(agenda);

		if (dao.alterar(agenda) == 0)
			throw new BusinessException("Agenda não encontrada.");
	}

	/** RN-AG-04: agenda com compromissos não pode ser excluída. */
	public void excluir(Long codigo) {
		if (codigo == null)
			throw new BusinessException("Agenda não encontrada.");

		int compromissos = compromissos().contarDaAgenda(codigo);

		if (compromissos > 0)
			throw new BusinessException("Não é possível excluir: a agenda tem "
					+ compromissos + (compromissos == 1 ? " compromisso." : " compromissos."));

		if (dao.excluir(codigo) == 0)
			throw new BusinessException("Agenda não encontrada.");
	}

	/** RN-AG-02: nome e período obrigatórios. RN-AG-03: o período é 1, 2 ou 3. */
	void validar(AgendaVo agenda) {
		String nome = agenda.getNome() == null ? "" : agenda.getNome().trim();

		if (nome.isEmpty())
			throw new BusinessException("Informe o nome da agenda.");

		if (nome.length() > TAMANHO_MAXIMO_DO_NOME)
			throw new BusinessException("O nome pode ter no máximo " + TAMANHO_MAXIMO_DO_NOME + " caracteres.");

		if (agenda.getPeriodo() == null)
			throw new BusinessException("Selecione o período disponível.");

		agenda.setNome(nome);
	}

	/**
	 * RN-AG-05: o período só pode mudar se todos os compromissos da agenda
	 * continuarem dentro do novo período.
	 */
	private void validarTrocaDePeriodo(AgendaVo agenda) {
		Periodo novoPeriodo = agenda.getPeriodo();

		for (LocalTime horario : compromissos().horariosDaAgenda(agenda.getCodigo()))
			if (!novoPeriodo.aceita(horario))
				throw new BusinessException("Não é possível mudar o período para " + novoPeriodo.getDescricao()
						+ ": a agenda tem compromisso às " + horario + ".");
	}

	/**
	 * Outro módulo é consultado pela Business dele, nunca pelo Dao.
	 * Criada aqui, e não em atributo, porque a CompromissoBusiness também
	 * usa a AgendaBusiness: dois atributos se criando um ao outro não terminam.
	 */
	private CompromissoBusiness compromissos() {
		return new CompromissoBusiness();
	}
}
