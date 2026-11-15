package br.com.kairos.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Análise do assistente de IA em seções (RN-IA-03). Vai para a tela como JSON.
 * {@code estruturada} falso = o provedor não devolveu o JSON pedido; o texto inteiro vai em resumo.
 * {@code erro} preenchido = a análise não aconteceu; a mensagem é para o usuário.
 */
public class AnaliseVo {

	private String resumo = "";
	private String comparacaoComAnteriores = "";
	private List<String> pontosDeAtencao = new ArrayList<>();
	private String sugestaoDeParecer = "";
	private List<String> dadosFaltantes = new ArrayList<>();
	private String limitacoes = "";
	private String provedor;
	private boolean estruturada = true;
	private String erro;

	public static AnaliseVo comErro(String mensagem) {
		AnaliseVo analise = new AnaliseVo();
		analise.setErro(mensagem);
		analise.setEstruturada(false);
		return analise;
	}

	public String getResumo() {
		return resumo;
	}

	public void setResumo(String resumo) {
		this.resumo = resumo;
	}

	public String getComparacaoComAnteriores() {
		return comparacaoComAnteriores;
	}

	public void setComparacaoComAnteriores(String comparacaoComAnteriores) {
		this.comparacaoComAnteriores = comparacaoComAnteriores;
	}

	public List<String> getPontosDeAtencao() {
		return pontosDeAtencao;
	}

	public void setPontosDeAtencao(List<String> pontosDeAtencao) {
		this.pontosDeAtencao = pontosDeAtencao;
	}

	public String getSugestaoDeParecer() {
		return sugestaoDeParecer;
	}

	public void setSugestaoDeParecer(String sugestaoDeParecer) {
		this.sugestaoDeParecer = sugestaoDeParecer;
	}

	public List<String> getDadosFaltantes() {
		return dadosFaltantes;
	}

	public void setDadosFaltantes(List<String> dadosFaltantes) {
		this.dadosFaltantes = dadosFaltantes;
	}

	public String getLimitacoes() {
		return limitacoes;
	}

	public void setLimitacoes(String limitacoes) {
		this.limitacoes = limitacoes;
	}

	public String getProvedor() {
		return provedor;
	}

	public void setProvedor(String provedor) {
		this.provedor = provedor;
	}

	public boolean isEstruturada() {
		return estruturada;
	}

	public void setEstruturada(boolean estruturada) {
		this.estruturada = estruturada;
	}

	public String getErro() {
		return erro;
	}

	public void setErro(String erro) {
		this.erro = erro;
	}
}
