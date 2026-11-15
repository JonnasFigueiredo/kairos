package br.com.kairos.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import br.com.kairos.exception.BusinessException;
import br.com.kairos.vo.MedicoVo;
import br.com.kairos.vo.ResultadoVo;
import br.com.kairos.vo.UsuarioVo;

/**
 * Regras do atendimento que não dependem do banco (RN-AT, RN-ME-05).
 */
public class AtendimentoBusinessTest {

	private final AtendimentoBusiness business = new AtendimentoBusiness();

	@Test
	public void rnAt01_soMedicoUsaOAtendimento() {
		UsuarioVo comum = new UsuarioVo();
		comum.setCodigo(1L);

		try {
			business.exigirMedico(comum);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals(AtendimentoBusiness.SO_MEDICO, e.getMessage());
		}

		business.exigirMedico(medica());
	}

	@Test
	public void rnAt02_indicadorEObrigatorioSoParaLiberar() {
		assertNull(business.nomeDoIndicador(null, false));
		assertNull(business.nomeDoIndicador("", false));
		assertEquals("ALTERADO", business.nomeDoIndicador("ALTERADO", true));

		try {
			business.nomeDoIndicador("", true);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals("Selecione o indicador do resultado.", e.getMessage());
		}
	}

	@Test
	public void rnAt02_indicadorDesconhecidoERecusadoMesmoNoRascunho() {
		try {
			business.nomeDoIndicador("OTIMO", false);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals("Selecione o indicador do resultado.", e.getMessage());
		}
	}

	@Test
	public void rnAt02_textosAteDezMilCaracteres() {
		ResultadoVo resultado = new ResultadoVo();
		resultado.setCodigoCompromisso(1L);
		resultado.setParecer(repetir('a', AtendimentoBusiness.TAMANHO_MAXIMO_DO_TEXTO + 1));

		try {
			business.validarTextos(resultado);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals("O parecer pode ter no máximo 10000 caracteres.", e.getMessage());
		}
	}

	@Test
	public void rnAt02_textoVazioViraNulo() {
		ResultadoVo resultado = new ResultadoVo();
		resultado.setCodigoCompromisso(1L);
		resultado.setAchados("   ");
		resultado.setParecer("  ok  ");

		business.validarTextos(resultado);

		assertNull(resultado.getAchados());
		assertEquals("ok", resultado.getParecer());
	}

	@Test
	public void rnAt03_motivoDaRetificacaoEObrigatorio() {
		try {
			business.validarMotivo("  ");
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals("Informe o motivo da retificação.", e.getMessage());
		}

		try {
			business.validarMotivo(repetir('m', AtendimentoBusiness.TAMANHO_MAXIMO_DO_MOTIVO + 1));
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals("O motivo pode ter no máximo 500 caracteres.", e.getMessage());
		}

		assertEquals("Erro de digitação", business.validarMotivo(" Erro de digitação "));
	}

	@Test
	public void rnMe05_assinaturaTemNomeCrmEUf() {
		assertEquals("Dra. Helena Prado - CRM 123456/SP", AtendimentoBusiness.assinaturaDe(medica()));
	}

	private static UsuarioVo medica() {
		MedicoVo medico = new MedicoVo();
		medico.setCrm("123456");
		medico.setUfCrm("SP");

		UsuarioVo usuario = new UsuarioVo();
		usuario.setCodigo(2L);
		usuario.setNome("Dra. Helena Prado");
		usuario.setMedico(medico);
		return usuario;
	}

	private static String repetir(char letra, int vezes) {
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < vezes; i++)
			texto.append(letra);
		return texto.toString();
	}
}
