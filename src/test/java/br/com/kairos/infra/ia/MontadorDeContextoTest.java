package br.com.kairos.infra.ia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import br.com.kairos.vo.CompromissoVo;
import br.com.kairos.vo.ResultadoVo;

/**
 * RN-IA-02: o que sai para a IA é pseudonimizado.
 */
public class MontadorDeContextoTest {

	@Test
	public void rnIa02_nomeDoTrabalhadorViraMarcador() {
		MontadorDeContexto.Limpador limpar = new MontadorDeContexto.Limpador("Maria da Silva");

		assertEquals("[trabalhador] relatou dor; [trabalhador] nega febre.",
				limpar.aplicar("Maria da Silva relatou dor; maria nega febre."));
	}

	@Test
	public void rnIa02_partesCurtasDoNomeNaoSaoTrocadas() {
		MontadorDeContexto.Limpador limpar = new MontadorDeContexto.Limpador("Maria da Silva");

		// "da" tem menos de 3 letras: a palavra "da" do texto fica como está
		assertEquals("dor da coluna", limpar.aplicar("dor da coluna"));
	}

	@Test
	public void rnIa02_cpfEEmailViramMarcadores() {
		MontadorDeContexto.Limpador limpar = new MontadorDeContexto.Limpador(null);

		assertEquals("CPF [cpf], contato [e-mail].",
				limpar.aplicar("CPF 123.456.789-00, contato joao@exemplo.com."));
	}

	@Test
	public void rnIa02_contextoNaoLevaONomeELevaOQueEstaNoFormulario() {
		CompromissoVo compromisso = new CompromissoVo();
		compromisso.setCodigo(10L);
		compromisso.setNomeFuncionario("Joana Pereira");
		compromisso.setNomeAgenda("Exames clínicos");
		compromisso.setData(LocalDate.of(2026, 11, 16));

		ResultadoVo salvo = new ResultadoVo();
		salvo.setParecer("Parecer gravado de Joana Pereira");

		ResultadoVo atual = new ResultadoVo();
		atual.setIndicador("ALTERADO");
		atual.setAchados("Joana relata cefaleia");

		String contexto = MontadorDeContexto.montar(compromisso, salvo, atual, Collections.<ResultadoVo>emptyList());

		assertFalse(contexto.contains("Joana"));
		assertFalse(contexto.contains("Pereira"));
		assertTrue(contexto.contains("\"indicador\":\"ALTERADO\""));
		assertTrue(contexto.contains("[trabalhador] relata cefaleia"));
		// Campo vazio no formulário usa o que está gravado
		assertTrue(contexto.contains("Parecer gravado de [trabalhador]"));
		assertTrue(contexto.contains("\"agenda\":\"Exames cl"));
	}
}
