<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<s:set var="tituloPagina" value="getText('erro.titulo')"/>
<s:set var="menuAtivo" value="''"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>

<div class="alert alert-danger" role="alert">
	<s:text name="erro.tecnico"/>
</div>
<a class="btn k-btn" href="<s:url action='inicio'/>"><s:text name="erro.tentar.novamente"/></a>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
