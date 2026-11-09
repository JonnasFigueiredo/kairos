package br.com.kairos.business;

import br.com.kairos.dao.ResumoDao;
import br.com.kairos.vo.ResumoVo;

/**
 * Monta o resumo da página inicial. Não há regra de negócio aqui:
 * a classe existe para a Action nunca falar direto com o Dao.
 */
public class ResumoBusiness {

	private final ResumoDao dao = new ResumoDao();

	public ResumoVo carregar() {
		return dao.contarRegistros();
	}
}
