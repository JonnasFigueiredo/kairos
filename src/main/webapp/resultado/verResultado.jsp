<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Leitura de um resultado liberado (RN-AT-06), com as versões anteriores --%>
<s:set var="tituloPagina" value="getText('resultado.ver.titulo')"/>
<s:set var="menuAtivo" value="'resultados'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="k-painel k-formulario k-formulario-largo mt-3">
	<div class="row g-2 mb-3">
		<div class="col-12 col-sm-5">
			<p class="k-card-rotulo"><s:text name="atendimento.funcionario"/></p>
			<p class="mb-0 fw-semibold"><s:property value="resultadoVo.codigoFuncionario"/> - <s:property value="resultadoVo.nomeFuncionario"/></p>
		</div>
		<div class="col-6 col-sm-4">
			<p class="k-card-rotulo"><s:text name="atendimento.agenda"/></p>
			<p class="mb-0 fw-semibold"><s:property value="resultadoVo.nomeAgenda"/></p>
		</div>
		<div class="col-6 col-sm-3">
			<p class="k-card-rotulo"><s:text name="atendimento.data"/></p>
			<p class="mb-0 fw-semibold"><s:property value="resultadoVo.dataCompromissoFormatada"/> <s:property value="resultadoVo.horarioCompromissoFormatado"/></p>
		</div>
	</div>

	<dl class="k-detalhe">
		<dt><s:text name="atendimento.indicador"/></dt>
		<dd><s:property value="resultadoVo.indicadorDescricao"/></dd>
		<dt><s:text name="atendimento.achados"/></dt>
		<dd class="k-texto-longo"><s:property value="resultadoVo.achados"/></dd>
		<dt><s:text name="atendimento.parecer"/></dt>
		<dd class="k-texto-longo"><s:property value="resultadoVo.parecer"/></dd>
		<dt><s:text name="atendimento.assinatura"/></dt>
		<dd><s:property value="resultadoVo.assinatura"/> &middot; <s:text name="atendimento.liberado.em"/> <s:property value="resultadoVo.liberadoEmFormatado"/> &middot; <s:text name="atendimento.versao"/> <s:property value="resultadoVo.versao"/></dd>
	</dl>

	<s:if test="!resultadoVo.versoes.isEmpty()">
		<h2 class="h6 mt-4 mb-2"><s:text name="atendimento.versoes"/></h2>
		<s:iterator value="resultadoVo.versoes">
			<div class="k-versao">
				<p class="mb-1"><strong><s:text name="atendimento.versao"/> <s:property value="versao"/></strong> &middot; <s:property value="indicadorDescricao"/> &middot; <s:property value="assinatura"/> &middot; <s:property value="liberadoEmFormatado"/></p>
				<p class="mb-1 k-texto-longo"><s:property value="parecer"/></p>
				<p class="mb-0 small k-suave"><s:text name="atendimento.retificado.por"/> <s:property value="retificadoPor"/> em <s:property value="retificadoEmFormatado"/>: <s:property value="motivo"/></p>
			</div>
		</s:iterator>
	</s:if>

	<a class="btn btn-outline-secondary mt-3" href="<s:url action='listarResultados'/>"><s:text name="resultado.voltar"/></a>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
