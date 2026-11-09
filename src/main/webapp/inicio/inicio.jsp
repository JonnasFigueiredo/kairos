<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<s:set var="tituloPagina" value="getText('inicio.titulo')"/>
<s:set var="menuAtivo" value="'inicio'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>

<p class="k-subtitulo"><s:text name="app.descricao"/></p>

<div class="row g-3">
	<div class="col-12 col-md-4">
		<div class="k-card">
			<p class="k-card-rotulo"><s:text name="menu.funcionarios"/></p>
			<p class="k-card-valor"><s:property value="resumo.totalFuncionarios"/></p>
		</div>
	</div>
	<div class="col-12 col-md-4">
		<div class="k-card">
			<p class="k-card-rotulo"><s:text name="menu.agendas"/></p>
			<p class="k-card-valor"><s:property value="resumo.totalAgendas"/></p>
		</div>
	</div>
	<div class="col-12 col-md-4">
		<div class="k-card">
			<p class="k-card-rotulo"><s:text name="menu.compromissos"/></p>
			<p class="k-card-valor"><s:property value="resumo.totalCompromissos"/></p>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
