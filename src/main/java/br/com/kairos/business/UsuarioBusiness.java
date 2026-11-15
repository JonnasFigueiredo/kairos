package br.com.kairos.business;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import br.com.kairos.dao.MedicoDao;
import br.com.kairos.dao.PermissaoDao;
import br.com.kairos.dao.UsuarioDao;
import br.com.kairos.exception.BusinessException;
import br.com.kairos.infra.Acao;
import br.com.kairos.infra.Modulo;
import br.com.kairos.infra.Senha;
import br.com.kairos.infra.Transacao;
import br.com.kairos.vo.MedicoVo;
import br.com.kairos.vo.UsuarioVo;

/**
 * Regras da tela Usuários e acessos (RN-PE e RN-ME).
 */
public class UsuarioBusiness {

	static final int TAMANHO_MAXIMO_DO_NOME = 255;
	static final int TAMANHO_MAXIMO_DO_LOGIN = 60;
	static final int TAMANHO_MINIMO_DA_SENHA = 6;
	static final int TAMANHO_MAXIMO_DO_CRM = 10;
	static final int TAMANHO_MAXIMO_DA_ESPECIALIDADE = 100;

	public static final List<String> UFS = Arrays.asList("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
			"MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

	private final UsuarioDao dao = new UsuarioDao();
	private final PermissaoDao permissaoDao = new PermissaoDao();
	private final MedicoDao medicoDao = new MedicoDao();

	public List<UsuarioVo> listar() {
		List<UsuarioVo> usuarios = dao.listarTodos();
		for (UsuarioVo usuario : usuarios) {
			usuario.setSenha(null);
			usuario.setMedico(medicoDao.buscarPorUsuario(usuario.getCodigo()));
		}
		return usuarios;
	}

	/** Usuário completo, com permissões e dados de médico, para o formulário. */
	public UsuarioVo buscar(Long codigo) {
		UsuarioVo usuario = codigo == null ? null : dao.buscarPorCodigo(codigo);

		if (usuario == null)
			throw new BusinessException("Usuário não encontrado.");

		usuario.setSenha(null);
		usuario.setPermissoes(permissaoDao.listarPorUsuario(codigo));
		usuario.setMedico(medicoDao.buscarPorUsuario(codigo));
		return usuario;
	}

	/**
	 * Inclui quando o código é nulo; altera quando vem preenchido.
	 *
	 * @param usuario    dados do formulário; em "senha" vem a senha digitada (vazia = manter)
	 * @param ehMedico   a caixa "É médico" da tela
	 * @param logado     quem está gravando (RN-PE-03)
	 */
	public void salvar(UsuarioVo usuario, boolean ehMedico, UsuarioVo logado) {
		boolean inclusao = usuario.getCodigo() == null;

		validarDados(usuario, inclusao);
		Set<String> permissoes = validarPermissoes(usuario.getPermissoes(), ehMedico);
		MedicoVo medico = ehMedico ? validarMedico(usuario.getMedico(), usuario.getCodigo()) : null;
		validarAutoprotecao(usuario, permissoes, logado);

		if (inclusao && dao.buscarPorLogin(usuario.getLogin()) != null)
			throw new BusinessException("Já existe usuário com esse login.");

		String senhaDigitada = usuario.getSenha();
		usuario.setSenha(senhaDigitada == null || senhaDigitada.isEmpty() ? null : Senha.gerar(senhaDigitada));
		usuario.setPermissoes(permissoes);
		usuario.setMedico(medico);

		Transacao.executar(conexao -> {
			if (inclusao) {
				dao.inserir(conexao, usuario);
			} else if (dao.alterar(conexao, usuario) == 0) {
				throw new BusinessException("Usuário não encontrado.");
			}

			// RN-PE-04: sempre fica ao menos um usuário ativo que possa alterar acessos
			boolean esteContinuaAdministrando = usuario.isAtivo() && permissoes.contains(Modulo.ACESSOS.chave(Acao.ALTERAR));
			if (!esteContinuaAdministrando
					&& dao.contarAtivosComPermissao(conexao, Modulo.ACESSOS.name(), Acao.ALTERAR.name(), usuario.getCodigo()) == 0)
				throw new BusinessException("Precisa ficar ao menos um usuário ativo que possa alterar acessos.");

			permissaoDao.substituir(conexao, usuario.getCodigo(), permissoes);

			if (medico == null)
				medicoDao.excluir(conexao, usuario.getCodigo());
			else
				medicoDao.gravar(conexao, usuario.getCodigo(), medico);
		});

		usuario.setSenha(null);
	}

	/** RN-PE-07: login, nome e, na inclusão, senha são obrigatórios. */
	void validarDados(UsuarioVo usuario, boolean inclusao) {
		String login = usuario.getLogin() == null ? "" : usuario.getLogin().trim().toLowerCase();
		String nome = usuario.getNome() == null ? "" : usuario.getNome().trim();
		String senha = usuario.getSenha() == null ? "" : usuario.getSenha();

		if (login.isEmpty())
			throw new BusinessException("Informe o login.");
		if (login.length() > TAMANHO_MAXIMO_DO_LOGIN || !login.matches("[a-z0-9._-]+"))
			throw new BusinessException("O login aceita letras minúsculas, números, ponto, traço e sublinhado, até "
					+ TAMANHO_MAXIMO_DO_LOGIN + " caracteres.");

		if (nome.isEmpty())
			throw new BusinessException("Informe o nome.");
		if (nome.length() > TAMANHO_MAXIMO_DO_NOME)
			throw new BusinessException("O nome pode ter no máximo " + TAMANHO_MAXIMO_DO_NOME + " caracteres.");

		if (inclusao && senha.isEmpty())
			throw new BusinessException("Informe a senha.");
		if (!senha.isEmpty() && senha.length() < TAMANHO_MINIMO_DA_SENHA)
			throw new BusinessException("A senha precisa ter ao menos " + TAMANHO_MINIMO_DA_SENHA + " caracteres.");

		usuario.setLogin(login);
		usuario.setNome(nome);
	}

	/**
	 * RN-PE-01: só permissões que existem na matriz; quem inclui, altera ou exclui
	 * também vê (o sistema completa). RN-ME-03: módulo só de médico exige conta de médico.
	 */
	Set<String> validarPermissoes(Set<String> marcadas, boolean ehMedico) {
		Set<String> completas = new LinkedHashSet<>();
		if (marcadas == null)
			return completas;

		for (String chave : marcadas) {
			String[] partes = chave.split(":", 2);
			Modulo modulo = partes.length == 2 ? Modulo.porNome(partes[0]) : null;
			Acao acao = partes.length == 2 ? Acao.porNome(partes[1]) : null;

			if (modulo == null || acao == null || !modulo.aceita(acao))
				throw new BusinessException("Permissão inválida: " + chave);

			if (modulo.isSoMedico() && !ehMedico)
				throw new BusinessException("Atendimento e assistente de IA só podem ser liberados para conta de médico.");

			completas.add(modulo.chave(Acao.VER));
			completas.add(chave);
		}
		return completas;
	}

	/** RN-ME-01 e RN-ME-02: CRM só números até 10, UF válida, especialidade opcional, CRM único na UF. */
	MedicoVo validarMedico(MedicoVo medico, Long codigoUsuario) {
		if (medico == null)
			medico = new MedicoVo();

		String crm = medico.getCrm() == null ? "" : medico.getCrm().trim();
		String uf = medico.getUfCrm() == null ? "" : medico.getUfCrm().trim().toUpperCase();
		String especialidade = medico.getEspecialidade() == null ? "" : medico.getEspecialidade().trim();

		if (crm.isEmpty() || crm.length() > TAMANHO_MAXIMO_DO_CRM || !crm.matches("\\d+"))
			throw new BusinessException("CRM deve ter só números, até " + TAMANHO_MAXIMO_DO_CRM + ".");
		if (!UFS.contains(uf))
			throw new BusinessException("Informe a UF do CRM.");
		if (especialidade.length() > TAMANHO_MAXIMO_DA_ESPECIALIDADE)
			throw new BusinessException("A especialidade pode ter no máximo " + TAMANHO_MAXIMO_DA_ESPECIALIDADE + " caracteres.");

		if (medicoDao.existeCrm(crm, uf, codigoUsuario))
			throw new BusinessException("Já existe médico com esse CRM nessa UF.");

		medico.setCrm(crm);
		medico.setUfCrm(uf);
		medico.setEspecialidade(especialidade.isEmpty() ? null : especialidade);
		return medico;
	}

	/** RN-PE-03: ninguém desativa a própria conta nem tira de si mesmo a permissão de alterar acessos. */
	void validarAutoprotecao(UsuarioVo usuario, Set<String> permissoes, UsuarioVo logado) {
		if (logado == null || usuario.getCodigo() == null || !usuario.getCodigo().equals(logado.getCodigo()))
			return;

		if (!usuario.isAtivo())
			throw new BusinessException("Você não pode desativar a própria conta.");

		if (!permissoes.contains(Modulo.ACESSOS.chave(Acao.ALTERAR)))
			throw new BusinessException("Você não pode tirar de si mesmo a permissão de alterar acessos.");
	}
}
