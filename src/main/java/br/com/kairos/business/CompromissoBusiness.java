package br.com.kairos.business;

import java.time.LocalTime;
import java.util.List;

import br.com.kairos.dao.CompromissoDao;

/**
 * Regras e consultas de compromissos.
 * Por enquanto só o que o cadastro de agendas precisa saber.
 */
public class CompromissoBusiness {

	private final CompromissoDao dao = new CompromissoDao();

	public int contarDaAgenda(Long codigoAgenda) {
		return dao.contarPorAgenda(codigoAgenda);
	}

	public List<LocalTime> horariosDaAgenda(Long codigoAgenda) {
		return dao.listarHorariosPorAgenda(codigoAgenda);
	}
}
