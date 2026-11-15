package br.com.kairos.infra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import br.com.kairos.vo.MedicoVo;
import br.com.kairos.vo.UsuarioVo;

/**
 * RN-PE-02: a permissão do usuário no módulo é conferida antes da Action.
 * RN-ME-03: módulo só de médico exige cadastro de médico.
 */
public class AutorizacaoInterceptorTest {

	@Test
	public void rnPe02_metodoDeLeituraExigeVer() {
		assertEquals(EnumSet.of(Acao.VER), Acao.exigidasPor("listar"));
		assertEquals(EnumSet.of(Acao.VER), Acao.exigidasPor("fila"));
		assertEquals(EnumSet.of(Acao.VER), Acao.exigidasPor("analisar"));
		assertEquals(EnumSet.of(Acao.VER), Acao.exigidasPor("execute"));
	}

	@Test
	public void rnPe02_metodosDeGravacaoExigemAAcaoCerta() {
		assertEquals(EnumSet.of(Acao.INCLUIR), Acao.exigidasPor("novo"));
		assertEquals(EnumSet.of(Acao.INCLUIR), Acao.exigidasPor("liberar"));
		assertEquals(EnumSet.of(Acao.ALTERAR), Acao.exigidasPor("editar"));
		assertEquals(EnumSet.of(Acao.ALTERAR), Acao.exigidasPor("retificar"));
		assertEquals(EnumSet.of(Acao.INCLUIR, Acao.ALTERAR), Acao.exigidasPor("salvar"));
		assertEquals(EnumSet.of(Acao.EXCLUIR), Acao.exigidasPor("excluir"));
	}

	@Test
	public void rnPe02_comPermissaoPassa() {
		AutorizacaoInterceptor interceptor = comModulo("FUNCIONARIOS");
		UsuarioVo usuario = comPermissoes("FUNCIONARIOS:VER", "FUNCIONARIOS:EXCLUIR");

		assertTrue(interceptor.autorizado(usuario, "listar"));
		assertTrue(interceptor.autorizado(usuario, "excluir"));
	}

	@Test
	public void rnPe02_semPermissaoEBarrado() {
		AutorizacaoInterceptor interceptor = comModulo("FUNCIONARIOS");
		UsuarioVo usuario = comPermissoes("FUNCIONARIOS:VER");

		assertFalse(interceptor.autorizado(usuario, "excluir"));
		assertFalse(interceptor.autorizado(usuario, "novo"));
	}

	@Test
	public void rnPe02_salvarPassaComIncluirOuAlterar() {
		AutorizacaoInterceptor interceptor = comModulo("AGENDAS");

		assertTrue(interceptor.autorizado(comPermissoes("AGENDAS:VER", "AGENDAS:INCLUIR"), "salvar"));
		assertTrue(interceptor.autorizado(comPermissoes("AGENDAS:VER", "AGENDAS:ALTERAR"), "salvar"));
		assertFalse(interceptor.autorizado(comPermissoes("AGENDAS:VER"), "salvar"));
	}

	@Test
	public void rnMe03_moduloSoDeMedicoExigeCadastroDeMedico() {
		AutorizacaoInterceptor interceptor = comModulo("ATENDIMENTO");
		UsuarioVo semCadastro = comPermissoes("ATENDIMENTO:VER");

		assertFalse(interceptor.autorizado(semCadastro, "fila"));

		UsuarioVo medico = comPermissoes("ATENDIMENTO:VER");
		medico.setMedico(new MedicoVo());
		assertTrue(interceptor.autorizado(medico, "fila"));
	}

	@Test
	public void rnPe01_chaveDeTextoInvalidaNaoLiberaNada() {
		UsuarioVo usuario = comPermissoes("FUNCIONARIOS:VER");

		assertFalse(usuario.pode("FUNCIONARIOS"));
		assertFalse(usuario.pode("INEXISTENTE:VER"));
		assertFalse(usuario.pode("FUNCIONARIOS:VOAR"));
		assertTrue(usuario.pode("FUNCIONARIOS:VER"));
	}

	@Test
	public void rnPe01_moduloSabeQuaisAcoesAceita() {
		assertTrue(Modulo.FUNCIONARIOS.aceita(Acao.EXCLUIR));
		assertFalse(Modulo.RELATORIO.aceita(Acao.INCLUIR));
		assertFalse(Modulo.ACESSOS.aceita(Acao.EXCLUIR));
		assertEquals("ACESSOS:ALTERAR", Modulo.ACESSOS.chave(Acao.ALTERAR));
		assertTrue(Modulo.ASSISTENTE_IA.isSoMedico());
		assertFalse(Modulo.RESULTADOS.isSoMedico());
	}

	private static AutorizacaoInterceptor comModulo(String modulo) {
		AutorizacaoInterceptor interceptor = new AutorizacaoInterceptor();
		interceptor.setModulo(modulo);
		return interceptor;
	}

	private static UsuarioVo comPermissoes(String... chaves) {
		Set<String> permissoes = new HashSet<>();
		for (String chave : chaves)
			permissoes.add(chave);

		UsuarioVo usuario = new UsuarioVo();
		usuario.setPermissoes(permissoes);
		return usuario;
	}
}
