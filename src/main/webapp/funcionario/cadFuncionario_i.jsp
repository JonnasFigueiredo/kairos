<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Formulário de funcionário: o mesmo para incluir (sem código) e alterar (com código) --%>
<s:if test="funcionarioVo.codigo == null">
	<s:set var="tituloPagina" value="getText('funcionario.novo')"/>
</s:if>
<s:else>
	<s:set var="tituloPagina" value="getText('funcionario.editar')"/>
</s:else>
<s:set var="menuAtivo" value="'funcionarios'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="k-painel k-formulario mt-3">
	<s:form action="salvarFuncionarios" method="post">
		<%-- O código vai escondido: é ele que diz à Business se é inclusão ou alteração --%>
		<s:hidden name="funcionarioVo.codigo"/>

		<div class="mb-3">
			<label class="form-label" for="codigo"><s:text name="comum.codigo"/></label>
			<s:if test="funcionarioVo.codigo == null">
				<input id="codigo" class="form-control" value="<s:text name='comum.codigo.automatico'/>" disabled>
			</s:if>
			<s:else>
				<input id="codigo" class="form-control" value="<s:property value='funcionarioVo.codigo'/>" disabled>
			</s:else>
		</div>

		<div class="mb-4">
			<label class="form-label" for="nome"><s:text name="comum.nome"/></label>
			<s:textfield id="nome" name="funcionarioVo.nome" cssClass="form-control" maxlength="255" autofocus="autofocus"/>
		</div>

		<div class="d-flex gap-2">
			<button type="submit" class="btn k-btn"><s:text name="comum.salvar"/></button>
			<a class="btn btn-outline-secondary" href="<s:url action='listarFuncionarios'/>"><s:text name="comum.cancelar"/></a>
		</div>
	</s:form>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
