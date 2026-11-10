<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Relatório de compromissos por período: filtros, resultado em tela e exportação para Excel --%>
<s:set var="tituloPagina" value="getText('relatorio.titulo')"/>
<s:set var="menuAtivo" value="'relatorio'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<%-- GET: o período fica na URL, então o relatório pode ser recarregado sem reenviar formulário --%>
<div class="k-painel k-nao-imprimir mt-3">
	<s:form action="gerarRelatorio" method="get" cssClass="k-filtros">
		<div>
			<label class="form-label" for="dataInicial"><s:text name="relatorio.data.inicial"/></label>
			<s:textfield id="dataInicial" name="dataInicial" type="date" cssClass="form-control"/>
		</div>
		<div>
			<label class="form-label" for="dataFinal"><s:text name="relatorio.data.final"/></label>
			<s:textfield id="dataFinal" name="dataFinal" type="date" cssClass="form-control"/>
		</div>
		<div class="k-filtros-botoes">
			<button type="submit" class="btn k-btn"><s:text name="relatorio.gerar"/></button>
			<%-- Mesmo formulário, outro destino: formaction troca a URL só para este botão --%>
			<button type="submit" class="btn btn-outline-secondary" formaction="<s:url action='excelRelatorio'/>">
				<s:text name="relatorio.excel"/>
			</button>
		</div>
	</s:form>
</div>

<%-- Só aparece depois de uma consulta válida --%>
<s:if test="gerado">
	<div class="k-painel mt-3">
		<div class="k-barra">
			<p class="mb-0">
				<s:text name="relatorio.periodo"/>
				<strong><s:property value="dataInicialFormatada"/></strong>
				<s:text name="relatorio.ate"/>
				<strong><s:property value="dataFinalFormatada"/></strong>
				(<s:property value="compromissos.size()"/> <s:text name="relatorio.registros"/>)
			</p>
			<button type="button" class="btn btn-outline-secondary k-nao-imprimir" data-imprimir>
				<s:text name="relatorio.imprimir"/>
			</button>
		</div>

		<div class="table-responsive">
			<table class="table k-tabela mb-0">
				<thead>
					<tr>
						<th><s:text name="relatorio.codigo.funcionario"/></th>
						<th><s:text name="relatorio.nome.funcionario"/></th>
						<th><s:text name="relatorio.codigo.agenda"/></th>
						<th><s:text name="relatorio.nome.agenda"/></th>
						<th><s:text name="compromisso.data"/></th>
						<th><s:text name="relatorio.hora"/></th>
					</tr>
				</thead>
				<tbody>
					<s:iterator value="compromissos">
						<tr>
							<td><s:property value="codigoFuncionario"/></td>
							<td><s:property value="nomeFuncionario"/></td>
							<td><s:property value="codigoAgenda"/></td>
							<td><s:property value="nomeAgenda"/></td>
							<td><s:property value="dataFormatada"/></td>
							<td><s:property value="horarioFormatado"/></td>
						</tr>
					</s:iterator>
					<s:if test="compromissos.isEmpty()">
						<tr><td colspan="6" class="k-vazio"><s:text name="relatorio.vazio"/></td></tr>
					</s:if>
				</tbody>
			</table>
		</div>
	</div>
</s:if>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
