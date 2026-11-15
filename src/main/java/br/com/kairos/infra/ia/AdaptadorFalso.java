package br.com.kairos.infra.ia;

/**
 * Provedor de mentira, para demonstração e testes sem chave nem internet
 * (KAIROS_IA_PROVEDOR=falso). Devolve uma análise fixa no formato esperado.
 */
class AdaptadorFalso implements ModeloDeLinguagem {

	static final String RESPOSTA = "{"
			+ "\"resumo\": \"Análise de demonstração: nenhum provedor de IA real está configurado neste servidor.\","
			+ "\"comparacaoComAnteriores\": \"Compare manualmente com o histórico ao lado.\","
			+ "\"pontosDeAtencao\": [\"Este texto não foi gerado por um modelo de linguagem.\"],"
			+ "\"sugestaoDeParecer\": \"Trabalhador avaliado em exame ocupacional. Sem alterações a registrar nos dados informados.\","
			+ "\"dadosFaltantes\": [\"Configure KAIROS_IA_PROVEDOR e a chave do provedor (KAIROS_IA_CHAVE_CLAUDE, _OPENAI, _GEMINI ou _DEEPSEEK) para uma análise real.\"],"
			+ "\"limitacoes\": \"Resposta fixa do provedor de demonstração.\""
			+ "}";

	@Override
	public String descricao() {
		return "Falso · demonstração";
	}

	@Override
	public RespostaModelo responder(PedidoModelo pedido) {
		return new RespostaModelo(RESPOSTA, descricao(), 0L, 0L);
	}
}
