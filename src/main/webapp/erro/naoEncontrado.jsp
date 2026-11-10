<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Endereço que não existe (erro 404, ver web.xml) --%>
<s:set var="tituloPagina" value="getText('erro.nao.encontrado.titulo')"/>
<s:set var="menuAtivo" value="''"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>

<div class="alert alert-danger" role="alert">
	<s:text name="erro.nao.encontrado"/>
</div>
<a class="btn k-btn" href="<s:url action='inicio'/>"><s:text name="erro.voltar.inicio"/></a>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
