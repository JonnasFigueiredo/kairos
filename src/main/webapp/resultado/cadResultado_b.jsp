<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Resultados liberados (RN-AT-06): leitura para quem tem o módulo Resultados --%>
<s:set var="tituloPagina" value="getText('resultado.titulo')"/>
<s:set var="menuAtivo" value="'resultados'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="k-painel mt-3">
	<div class="k-barra">
		<s:form action="listarResultados" method="get" cssClass="k-busca">
			<label class="visually-hidden" for="busca"><s:text name="resultado.busca"/></label>
			<s:textfield id="busca" name="busca" cssClass="form-control" placeholder="%{getText('resultado.busca')}"/>
			<button type="submit" class="btn k-btn"><s:text name="comum.pesquisar"/></button>
			<s:if test="busca != null && !busca.isEmpty()">
				<a class="btn btn-outline-secondary" href="<s:url action='listarResultados'/>"><s:text name="comum.limpar"/></a>
			</s:if>
		</s:form>
	</div>

	<div class="table-responsive">
		<table class="table k-tabela mb-0">
			<thead>
				<tr>
					<th><s:text name="atendimento.data"/></th>
					<th><s:text name="atendimento.funcionario"/></th>
					<th><s:text name="atendimento.agenda"/></th>
					<th><s:text name="atendimento.indicador"/></th>
					<th><s:text name="atendimento.assinatura"/></th>
					<th class="text-end"><s:text name="comum.acoes"/></th>
				</tr>
			</thead>
			<tbody>
				<s:iterator value="resultados">
					<tr>
						<td><s:property value="dataCompromissoFormatada"/> <s:property value="horarioCompromissoFormatado"/></td>
						<td><s:property value="codigoFuncionario"/> - <s:property value="nomeFuncionario"/></td>
						<td><s:property value="nomeAgenda"/></td>
						<td><s:property value="indicadorDescricao"/></td>
						<td><s:property value="assinatura"/></td>
						<td class="text-end text-nowrap">
							<s:url action="verResultados" var="urlVer">
								<s:param name="resultadoVo.codigo" value="codigo"/>
							</s:url>
							<a class="btn btn-sm btn-outline-secondary" href="<s:property value='#urlVer'/>"><s:text name="resultado.ver"/></a>
						</td>
					</tr>
				</s:iterator>
				<s:if test="resultados.isEmpty()">
					<tr><td colspan="6" class="k-vazio"><s:text name="comum.nenhum.registro"/></td></tr>
				</s:if>
			</tbody>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
