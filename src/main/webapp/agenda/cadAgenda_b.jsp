<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Consulta de agendas: pesquisa, tabela e ações --%>
<s:set var="tituloPagina" value="getText('agenda.titulo')"/>
<s:set var="menuAtivo" value="'agendas'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="k-painel mt-3">
	<div class="k-barra">
		<s:form action="listarAgendas" method="get" cssClass="k-busca">
			<label class="visually-hidden" for="busca"><s:text name="agenda.busca"/></label>
			<s:textfield id="busca" name="busca" cssClass="form-control" placeholder="%{getText('agenda.busca')}"/>
			<button type="submit" class="btn k-btn"><s:text name="comum.pesquisar"/></button>
			<s:if test="busca != null && !busca.isEmpty()">
				<a class="btn btn-outline-secondary" href="<s:url action='listarAgendas'/>"><s:text name="comum.limpar"/></a>
			</s:if>
		</s:form>
		<a class="btn k-btn" href="<s:url action='novoAgendas'/>"><s:text name="comum.novo"/></a>
	</div>

	<div class="table-responsive">
		<table class="table k-tabela mb-0">
			<thead>
				<tr>
					<th class="k-col-codigo"><s:text name="comum.codigo"/></th>
					<th><s:text name="comum.nome"/></th>
					<th><s:text name="agenda.periodo"/></th>
					<th class="text-end"><s:text name="comum.acoes"/></th>
				</tr>
			</thead>
			<tbody>
				<s:iterator value="agendas">
					<tr>
						<td><s:property value="codigo"/></td>
						<td><s:property value="nome"/></td>
						<td><s:property value="periodo.descricao"/></td>
						<td class="text-end text-nowrap">
							<s:url action="editarAgendas" var="urlEditar">
								<s:param name="agendaVo.codigo" value="codigo"/>
							</s:url>
							<a class="btn btn-sm btn-outline-secondary" href="<s:property value='#urlEditar'/>"><s:text name="comum.editar"/></a>
							<button type="button" class="btn btn-sm btn-outline-danger"
								data-bs-toggle="modal" data-bs-target="#confirmarExclusao"
								data-codigo="<s:property value='codigo'/>" data-nome="<s:property value='nome'/>">
								<s:text name="comum.excluir"/>
							</button>
						</td>
					</tr>
				</s:iterator>
				<s:if test="agendas.isEmpty()">
					<tr><td colspan="4" class="k-vazio"><s:text name="comum.nenhum.registro"/></td></tr>
				</s:if>
			</tbody>
		</table>
	</div>
</div>

<div class="modal fade" id="confirmarExclusao" tabindex="-1" aria-labelledby="tituloExclusao" aria-hidden="true">
	<div class="modal-dialog">
		<s:form action="excluirAgendas" method="post" cssClass="modal-content">
			<div class="modal-header">
				<h2 class="modal-title h5" id="tituloExclusao"><s:text name="comum.exclusao.titulo"/></h2>
				<button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="<s:text name='comum.fechar'/>"></button>
			</div>
			<div class="modal-body">
				<p><s:text name="agenda.exclusao.pergunta"/></p>
				<p class="fw-bold mb-0" data-exclusao-nome></p>
				<input type="hidden" name="agendaVo.codigo" data-exclusao-codigo>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal"><s:text name="comum.cancelar"/></button>
				<button type="submit" class="btn btn-danger"><s:text name="comum.exclusao.sim"/></button>
			</div>
		</s:form>
	</div>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
