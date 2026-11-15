package br.com.kairos.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import br.com.kairos.exception.BusinessException;
import br.com.kairos.vo.MedicoVo;
import br.com.kairos.vo.UsuarioVo;

/**
 * Regras de usuários e acessos que não dependem do banco (RN-PE, RN-ME).
 */
public class UsuarioBusinessTest {

	private final UsuarioBusiness business = new UsuarioBusiness();

	@Test
	public void rnPe07_loginObrigatorio() {
		assertRecusado(comDados("", "Maria", "123456"), true, "Informe o login.");
	}

	@Test
	public void rnPe07_loginSoAceitaMinusculasNumerosEPontuacaoSimples() {
		assertRecusado(comDados("Maria Silva", "Maria", "123456"), true,
				"O login aceita letras minúsculas, números, ponto, traço e sublinhado, até 60 caracteres.");
	}

	@Test
	public void rnPe07_loginEGuardadoEmMinusculas() {
		UsuarioVo usuario = comDados("  Maria.Silva ", "Maria", "123456");
		business.validarDados(usuario, true);
		assertEquals("maria.silva", usuario.getLogin());
	}

	@Test
	public void rnPe07_senhaObrigatoriaSoNaInclusao() {
		assertRecusado(comDados("maria", "Maria", ""), true, "Informe a senha.");
		business.validarDados(comDados("maria", "Maria", ""), false);
	}

	@Test
	public void rnPe07_senhaCurtaERecusada() {
		assertRecusado(comDados("maria", "Maria", "12345"), false, "A senha precisa ter ao menos 6 caracteres.");
	}

	@Test
	public void rnPe01_permissaoInexistenteERecusada() {
		try {
			business.validarPermissoes(conjunto("FUNCIONARIOS:VOAR"), false);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals("Permissão inválida: FUNCIONARIOS:VOAR", e.getMessage());
		}
	}

	@Test
	public void rnPe01_quemAlteraTambemVe() {
		Set<String> completas = business.validarPermissoes(conjunto("AGENDAS:ALTERAR"), false);

		assertTrue(completas.contains("AGENDAS:VER"));
		assertTrue(completas.contains("AGENDAS:ALTERAR"));
		assertEquals(2, completas.size());
	}

	@Test
	public void rnMe03_moduloSoDeMedicoExigeContaDeMedico() {
		try {
			business.validarPermissoes(conjunto("ATENDIMENTO:VER"), false);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals("Atendimento e assistente de IA só podem ser liberados para conta de médico.", e.getMessage());
		}

		assertTrue(business.validarPermissoes(conjunto("ATENDIMENTO:VER"), true).contains("ATENDIMENTO:VER"));
	}

	@Test
	public void rnMe01_crmSoNumerosAteDez() {
		assertMedicoRecusado("12A45", "SP", "CRM deve ter só números, até 10.");
		assertMedicoRecusado("12345678901", "SP", "CRM deve ter só números, até 10.");
		assertMedicoRecusado("", "SP", "CRM deve ter só números, até 10.");
	}

	@Test
	public void rnMe01_ufPrecisaSerUmaDas27() {
		assertMedicoRecusado("12345", "XX", "Informe a UF do CRM.");
		assertMedicoRecusado("12345", "", "Informe a UF do CRM.");
	}

	@Test
	public void rnPe03_naoDesativaAPropriaConta() {
		UsuarioVo logado = new UsuarioVo();
		logado.setCodigo(7L);

		UsuarioVo editado = new UsuarioVo();
		editado.setCodigo(7L);
		editado.setAtivo(false);

		try {
			business.validarAutoprotecao(editado, conjunto("ACESSOS:ALTERAR"), logado);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals("Você não pode desativar a própria conta.", e.getMessage());
		}
	}

	@Test
	public void rnPe03_naoTiraDeSiMesmoAlterarAcessos() {
		UsuarioVo logado = new UsuarioVo();
		logado.setCodigo(7L);

		UsuarioVo editado = new UsuarioVo();
		editado.setCodigo(7L);

		try {
			business.validarAutoprotecao(editado, conjunto("ACESSOS:VER"), logado);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals("Você não pode tirar de si mesmo a permissão de alterar acessos.", e.getMessage());
		}
	}

	@Test
	public void rnPe03_outraContaPodeSerDesativada() {
		UsuarioVo logado = new UsuarioVo();
		logado.setCodigo(7L);

		UsuarioVo editado = new UsuarioVo();
		editado.setCodigo(8L);
		editado.setAtivo(false);

		business.validarAutoprotecao(editado, new HashSet<String>(), logado);
	}

	private void assertRecusado(UsuarioVo usuario, boolean inclusao, String mensagem) {
		try {
			business.validarDados(usuario, inclusao);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals(mensagem, e.getMessage());
		}
	}

	private void assertMedicoRecusado(String crm, String uf, String mensagem) {
		MedicoVo medico = new MedicoVo();
		medico.setCrm(crm);
		medico.setUfCrm(uf);
		try {
			business.validarMedico(medico, null);
			fail("devia recusar");
		} catch (BusinessException e) {
			assertEquals(mensagem, e.getMessage());
		}
	}

	private static UsuarioVo comDados(String login, String nome, String senha) {
		UsuarioVo usuario = new UsuarioVo();
		usuario.setLogin(login);
		usuario.setNome(nome);
		usuario.setSenha(senha);
		return usuario;
	}

	private static Set<String> conjunto(String... chaves) {
		return new LinkedHashSet<>(Arrays.asList(chaves));
	}
}
