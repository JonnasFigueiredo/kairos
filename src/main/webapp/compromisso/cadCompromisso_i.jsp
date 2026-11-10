<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Formulário de compromisso: o mesmo para incluir (sem código) e alterar (com código) --%>
<s:if test="compromissoVo.codigo == null">
	<s:set var="tituloPagina" value="getText('compromisso.novo')"/>
</s:if>
<s:else>
	<s:set var="tituloPagina" value="getText('compromisso.editar')"/>
</s:else>
<s:set var="menuAtivo" value="'compromissos'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="k-painel k-formulario mt-3">
	<s:form action="salvarCompromissos" method="post">
		<s:hidden name="compromissoVo.codigo"/>

		<div class="mb-3">
			<label class="form-label" for="funcionario"><s:text name="compromisso.funcionario"/></label>
			<%-- O value de cada opção é o código do funcionário; o texto é "código - nome" --%>
			<s:select id="funcionario" name="compromissoVo.codigoFuncionario" cssClass="form-select"
				list="funcionarios" listKey="codigo" listValue="codigo + ' - ' + nome"
				headerKey="" headerValue="%{getText('comum.selecione')}"/>
		</div>

		<div class="mb-3">
			<label class="form-label" for="agenda"><s:text name="compromisso.agenda"/></label>
			<%-- O período aparece no texto da opção para o usuário já saber quando a agenda atende --%>
			<s:select id="agenda" name="compromissoVo.codigoAgenda" cssClass="form-select"
				list="agendas" listKey="codigo" listValue="codigo + ' - ' + nome + ' (' + periodo.descricao + ')'"
				headerKey="" headerValue="%{getText('comum.selecione')}"/>
		</div>

		<div class="row g-3 mb-4">
			<div class="col-12 col-sm-6">
				<label class="form-label" for="data"><s:text name="compromisso.data"/></label>
				<s:textfield id="data" name="compromissoVo.data" type="date" cssClass="form-control"/>
			</div>
			<div class="col-12 col-sm-6">
				<label class="form-label" for="horario"><s:text name="compromisso.horario"/></label>
				<s:textfield id="horario" name="compromissoVo.horario" type="time" cssClass="form-control"/>
			</div>
		</div>

		<div class="d-flex gap-2">
			<button type="submit" class="btn k-btn"><s:text name="comum.salvar"/></button>
			<a class="btn btn-outline-secondary" href="<s:url action='listarCompromissos'/>"><s:text name="comum.cancelar"/></a>
		</div>
	</s:form>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
