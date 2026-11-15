package br.com.kairos.infra.ia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import br.com.kairos.vo.CompromissoVo;
import br.com.kairos.vo.ResultadoVo;

/**
 * RN-IA-02: monta o que sai do sistema para a IA, pseudonimizado. Vão a agenda, a data,
 * o que o médico anotou e o histórico. Nunca vai o nome do trabalhador; e o texto livre
 * (achados, parecer) passa por uma limpeza que troca nome, CPF e e-mail digitados por marcadores.
 */
public final class MontadorDeContexto {

	public static final String TRABALHADOR = "[trabalhador]";
	private static final Pattern CPF = Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");
	/** O domínio termina em letras ou números: o ponto final da frase fica de fora. */
	private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[\\p{L}0-9-]+");
	private static final int PARTE_MINIMA_DO_NOME = 3;

	private MontadorDeContexto() {
		// só métodos estáticos
	}

	/**
	 * @param atual o que está no formulário agora (pode não estar salvo); campo vazio usa o resultado gravado
	 * @param salvo o resultado gravado, ou um rascunho vazio
	 */
	public static String montar(CompromissoVo compromisso, ResultadoVo salvo, ResultadoVo atual, List<ResultadoVo> historico) {
		Limpador limpar = new Limpador(compromisso.getNomeFuncionario());

		Map<String, Object> compromissoAtual = new LinkedHashMap<>();
		compromissoAtual.put("agenda", compromisso.getNomeAgenda());
		compromissoAtual.put("data", compromisso.getData() == null ? null : compromisso.getData().toString());
		compromissoAtual.put("indicador", preenchido(atual.getIndicador(), salvo.getIndicador()));
		compromissoAtual.put("achados", limpar.aplicar(preenchido(atual.getAchados(), salvo.getAchados())));
		compromissoAtual.put("parecerEmAndamento", limpar.aplicar(preenchido(atual.getParecer(), salvo.getParecer())));

		List<Map<String, Object>> anteriores = new ArrayList<>();
		for (ResultadoVo h : historico) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("agenda", h.getNomeAgenda());
			item.put("data", h.getDataCompromisso() == null ? null : h.getDataCompromisso().toString());
			item.put("situacao", h.getSituacao() == null ? "SEM_RESULTADO" : h.getSituacao());
			item.put("indicador", h.isLiberado() ? h.getIndicador() : null);
			item.put("parecer", h.isLiberado() ? limpar.aplicar(h.getParecer()) : null);
			anteriores.add(item);
		}

		Map<String, Object> contexto = new LinkedHashMap<>();
		contexto.put("compromissoAtual", compromissoAtual);
		contexto.put("historico", anteriores);
		return HttpJson.serializar(contexto);
	}

	/** Troca, no texto livre, o nome do trabalhador (inteiro e cada parte), CPFs e e-mails por marcadores. */
	static final class Limpador {
		private final List<Pattern> nomes = new ArrayList<>();

		Limpador(String nomeDoTrabalhador) {
			if (nomeDoTrabalhador == null || nomeDoTrabalhador.trim().isEmpty())
				return;

			List<String> partes = new ArrayList<>();
			partes.add(nomeDoTrabalhador.trim());
			for (String parte : nomeDoTrabalhador.trim().split("\\s+"))
				if (parte.length() >= PARTE_MINIMA_DO_NOME)
					partes.add(parte);

			for (String parte : partes)
				nomes.add(Pattern.compile("(?<!\\p{L})" + Pattern.quote(parte) + "(?!\\p{L})",
						Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
		}

		String aplicar(String texto) {
			if (texto == null)
				return null;

			String limpo = CPF.matcher(texto).replaceAll("[cpf]");
			limpo = EMAIL.matcher(limpo).replaceAll("[e-mail]");
			for (Pattern nome : nomes)
				limpo = nome.matcher(limpo).replaceAll(TRABALHADOR);
			return limpo;
		}
	}

	private static String preenchido(String atual, String salvo) {
		return atual != null && !atual.trim().isEmpty() ? atual.trim() : salvo;
	}
}
