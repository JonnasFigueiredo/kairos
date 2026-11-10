package br.com.kairos.business;

import java.time.LocalDate;
import java.util.List;

import br.com.kairos.exception.BusinessException;
import br.com.kairos.vo.CompromissoVo;

/**
 * Regras do relatório de compromissos por período.
 * A tela e o Excel usam este mesmo método, então mostram sempre os mesmos dados (RN-RE-05).
 */
public class RelatorioBusiness {

	private final CompromissoBusiness compromissoBusiness = new CompromissoBusiness();

	/** RN-RE-01: compromissos com data dentro do período, incluindo as duas datas. */
	public List<CompromissoVo> compromissosDoPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
		validarPeriodo(dataInicial, dataFinal);
		return compromissoBusiness.listarDoPeriodo(dataInicial, dataFinal);
	}

	/** RN-RE-02: as duas datas são obrigatórias. RN-RE-03: a inicial não pode ser maior que a final. */
	void validarPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
		if (dataInicial == null)
			throw new BusinessException("Informe a data inicial.");

		if (dataFinal == null)
			throw new BusinessException("Informe a data final.");

		if (dataInicial.isAfter(dataFinal))
			throw new BusinessException("A data inicial não pode ser maior que a data final.");
	}
}
