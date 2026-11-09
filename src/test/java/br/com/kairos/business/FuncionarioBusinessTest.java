package br.com.kairos.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import br.com.kairos.exception.BusinessException;
import br.com.kairos.vo.FuncionarioVo;

/**
 * Regras do funcionário que não dependem do banco.
 */
public class FuncionarioBusinessTest {

	private final FuncionarioBusiness business = new FuncionarioBusiness();

	@Test
	public void rnFu02_nomeNuloERecusado() {
		assertRecusado(null, "Informe o nome do funcionário.");
	}

	@Test
	public void rnFu02_nomeSoComEspacosERecusado() {
		assertRecusado("   ", "Informe o nome do funcionário.");
	}

	@Test
	public void nomeMaiorQueOLimiteERecusado() {
		StringBuilder nome = new StringBuilder();
		for (int i = 0; i <= FuncionarioBusiness.TAMANHO_MAXIMO_DO_NOME; i++)
			nome.append('a');

		assertRecusado(nome.toString(), "O nome pode ter no máximo 255 caracteres.");
	}

	@Test
	public void nomeValidoPerdeOsEspacosDasPontas() {
		FuncionarioVo funcionario = comNome("  Maria  ");

		business.validar(funcionario);

		assertEquals("Maria", funcionario.getNome());
	}

	private void assertRecusado(String nome, String mensagemEsperada) {
		try {
			business.validar(comNome(nome));
			fail("Deveria recusar o nome: [" + nome + "]");
		} catch (BusinessException e) {
			assertEquals(mensagemEsperada, e.getMessage());
		}
	}

	private FuncionarioVo comNome(String nome) {
		FuncionarioVo funcionario = new FuncionarioVo();
		funcionario.setNome(nome);
		return funcionario;
	}
}
