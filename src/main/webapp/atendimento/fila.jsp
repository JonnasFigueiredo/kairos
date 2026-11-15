<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Fila do médico (RN-AT-07): compromissos de hoje e os anteriores sem resultado liberado --%>
<s:set var="tituloPagina" value="getText('atendimento.titulo')"/>
<s:set var="menuAtivo" value="'atendimento'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="k-painel mt-3">
	<h2 class="h5 k-lista-titulo"><s:text name="atendimento.hoje"/></h2>
	<div class="table-responsive">
		<table class="table k-tabela mb-0">
			<thead>
				<tr>
					<th><s:text name="compromisso.horario"/></th>
					<th><s:text name="atendimento.funcionario"/></th>
					<th><s:text name="atendimento.agenda"/></th>
					<th><s:text name="atendimento.situacao"/></th>
					<th class="text-end"><s:text name="comum.acoes"/></th>
				</tr>
			</thead>
			<tbody>
				<s:iterator value="hoje">
					<tr>
						<td><s:property value="horarioFormatado"/></td>
						<td><s:property value="nomeFuncionario"/></td>
						<td><s:property value="nomeAgenda"/></td>
						<td>
							<s:if test="situacaoResultado == 'LIBERADO'"><span class="badge k-badge-ativo"><s:text name="atendimento.liberado"/></span></s:if>
							<s:elseif test="situacaoResultado == 'RASCUNHO'"><span class="badge k-badge-rascunho"><s:text name="atendimento.rascunho"/></span></s:elseif>
							<s:else><span class="badge k-badge-inativo"><s:text name="atendimento.sem.resultado"/></span></s:else>
						</td>
						<td class="text-end text-nowrap">
							<s:url action="abrirAtendimento" var="urlAbrir">
								<s:param name="codigoCompromisso" value="codigo"/>
							</s:url>
							<a class="btn btn-sm k-btn" href="<s:property value='#urlAbrir'/>">
								<s:if test="situacaoResultado == 'LIBERADO'"><s:text name="atendimento.abrir"/></s:if>
								<s:else><s:text name="atendimento.atender"/></s:else>
							</a>
						</td>
					</tr>
				</s:iterator>
				<s:if test="hoje.isEmpty()">
					<tr><td colspan="5" class="k-vazio"><s:text name="comum.nenhum.registro"/></td></tr>
				</s:if>
			</tbody>
		</table>
	</div>
</div>

<div class="k-painel mt-3">
	<h2 class="h5 k-lista-titulo"><s:text name="atendimento.pendentes"/></h2>
	<p class="k-suave px-3 mb-2 small"><s:text name="atendimento.pendentes.ajuda"/></p>
	<div class="table-responsive">
		<table class="table k-tabela mb-0">
			<thead>
				<tr>
					<th><s:text name="atendimento.data"/></th>
					<th><s:text name="compromisso.horario"/></th>
					<th><s:text name="atendimento.funcionario"/></th>
					<th><s:text name="atendimento.agenda"/></th>
					<th><s:text name="atendimento.situacao"/></th>
					<th class="text-end"><s:text name="comum.acoes"/></th>
				</tr>
			</thead>
			<tbody>
				<s:iterator value="pendentes">
					<tr>
						<td><s:property value="dataFormatada"/></td>
						<td><s:property value="horarioFormatado"/></td>
						<td><s:property value="nomeFuncionario"/></td>
						<td><s:property value="nomeAgenda"/></td>
						<td>
							<s:if test="situacaoResultado == 'RASCUNHO'"><span class="badge k-badge-rascunho"><s:text name="atendimento.rascunho"/></span></s:if>
							<s:else><span class="badge k-badge-inativo"><s:text name="atendimento.sem.resultado"/></span></s:else>
						</td>
						<td class="text-end text-nowrap">
							<s:url action="abrirAtendimento" var="urlAbrir">
								<s:param name="codigoCompromisso" value="codigo"/>
							</s:url>
							<a class="btn btn-sm k-btn" href="<s:property value='#urlAbrir'/>"><s:text name="atendimento.atender"/></a>
						</td>
					</tr>
				</s:iterator>
				<s:if test="pendentes.isEmpty()">
					<tr><td colspan="6" class="k-vazio"><s:text name="comum.nenhum.registro"/></td></tr>
				</s:if>
			</tbody>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
