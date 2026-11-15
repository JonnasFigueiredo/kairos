package br.com.kairos.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.kairos.infra.ia.ProvedoresIa;
import br.com.kairos.vo.AnaliseVo;

/**
 * RN-IA-03: a resposta vem em seções; se o provedor fugir do formato, o texto aparece como veio.
 */
public class AnaliseIaBusinessTest {

	private final AnaliseIaBusiness business = new AnaliseIaBusiness(new ProvedoresIa(null, null), 30);

	@Test
	public void rnIa03_jsonNasSecoesEInterpretado() {
		String json = "{\"resumo\":\"Exame sem alterações.\",\"comparacaoComAnteriores\":\"Sem anteriores.\","
				+ "\"pontosDeAtencao\":[\"PA limítrofe\"],\"sugestaoDeParecer\":\"Sem restrições.\","
				+ "\"dadosFaltantes\":[],\"limitacoes\":\"Só dados informados.\"}";

		AnaliseVo analise = business.interpretar(json, "Falso");

		assertTrue(analise.isEstruturada());
		assertEquals("Exame sem alterações.", analise.getResumo());
		assertEquals(1, analise.getPontosDeAtencao().size());
		assertEquals("PA limítrofe", analise.getPontosDeAtencao().get(0));
		assertEquals("Sem restrições.", analise.getSugestaoDeParecer());
		assertTrue(analise.getDadosFaltantes().isEmpty());
		assertEquals("Falso", analise.getProvedor());
	}

	@Test
	public void rnIa03_cercaDeCodigoEmVoltaDoJsonEIgnorada() {
		AnaliseVo analise = business.interpretar("```json\n{\"resumo\":\"ok\"}\n```", "x");

		assertTrue(analise.isEstruturada());
		assertEquals("ok", analise.getResumo());
		assertEquals("", analise.getLimitacoes());
	}

	@Test
	public void rnIa03_textoForaDoFormatoVaiInteiroNoResumo() {
		AnaliseVo analise = business.interpretar("O exame parece normal.", "x");

		assertFalse(analise.isEstruturada());
		assertEquals("O exame parece normal.", analise.getResumo());
	}

	@Test
	public void rnIa06_semProvedorFicaIndisponivel() {
		assertFalse(business.disponivel());
	}
}
