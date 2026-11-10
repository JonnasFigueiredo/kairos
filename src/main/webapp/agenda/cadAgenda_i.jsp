<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Formulário de agenda: o mesmo para incluir (sem código) e alterar (com código) --%>
<s:if test="agendaVo.codigo == null">
	<s:set var="tituloPagina" value="getText('agenda.nova')"/>
</s:if>
<s:else>
	<s:set var="tituloPagina" value="getText('agenda.editar')"/>
</s:else>
<s:set var="menuAtivo" value="'agendas'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="k-painel k-formulario mt-3">
	<s:form action="salvarAgendas" method="post">
		<s:hidden name="agendaVo.codigo"/>

		<div class="mb-3">
			<label class="form-label" for="codigo"><s:text name="comum.codigo"/></label>
			<s:if test="agendaVo.codigo == null">
				<input id="codigo" class="form-control" value="<s:text name='comum.codigo.automatico'/>" disabled>
			</s:if>
			<s:else>
				<input id="codigo" class="form-control" value="<s:property value='agendaVo.codigo'/>" disabled>
			</s:else>
		</div>

		<div class="mb-3">
			<label class="form-label" for="nome"><s:text name="comum.nome"/></label>
			<s:textfield id="nome" name="agendaVo.nome" cssClass="form-control" maxlength="255" autofocus="autofocus"/>
		</div>

		<div class="mb-4">
			<label class="form-label" for="periodo"><s:text name="agenda.periodo"/></label>
			<%-- RN-AG-03: gera um <select>. "list" chama getPeriodos() na Action;
			     de cada item, listKey vira o value da opção e listValue o texto. --%>
			<s:select id="periodo" name="agendaVo.codigoPeriodo" cssClass="form-select"
				list="periodos" listKey="codigo" listValue="descricao"
				headerKey="" headerValue="%{getText('comum.selecione')}"/>
		</div>

		<div class="d-flex gap-2">
			<button type="submit" class="btn k-btn"><s:text name="comum.salvar"/></button>
			<a class="btn btn-outline-secondary" href="<s:url action='listarAgendas'/>"><s:text name="comum.cancelar"/></a>
		</div>
	</s:form>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
