<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Usuários e acessos: lista das contas. Conta não se exclui, só se desativa (RN-PE-06) --%>
<s:set var="tituloPagina" value="getText('usuario.titulo')"/>
<s:set var="menuAtivo" value="'usuarios'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="k-painel mt-3">
	<div class="k-barra">
		<p class="k-subtitulo mb-0"><s:text name="usuario.descricao"/></p>
		<s:if test="#session.usuario.pode('ACESSOS:INCLUIR')">
			<a class="btn k-btn" href="<s:url action='novoUsuarios'/>"><s:text name="comum.novo"/></a>
		</s:if>
	</div>

	<div class="table-responsive">
		<table class="table k-tabela mb-0">
			<thead>
				<tr>
					<th class="k-col-codigo"><s:text name="comum.codigo"/></th>
					<th><s:text name="usuario.login"/></th>
					<th><s:text name="comum.nome"/></th>
					<th><s:text name="usuario.medico"/></th>
					<th><s:text name="usuario.situacao"/></th>
					<th class="text-end"><s:text name="comum.acoes"/></th>
				</tr>
			</thead>
			<tbody>
				<s:iterator value="usuarios">
					<tr>
						<td><s:property value="codigo"/></td>
						<td><s:property value="login"/></td>
						<td><s:property value="nome"/></td>
						<td><s:if test="medico"><s:property value="medico.crmFormatado"/></s:if><s:else>-</s:else></td>
						<td>
							<s:if test="ativo"><span class="badge k-badge-ativo"><s:text name="usuario.ativo"/></span></s:if>
							<s:else><span class="badge k-badge-inativo"><s:text name="usuario.inativo"/></span></s:else>
						</td>
						<td class="text-end text-nowrap">
							<s:if test="#session.usuario.pode('ACESSOS:ALTERAR')">
								<s:url action="editarUsuarios" var="urlEditar">
									<s:param name="usuarioVo.codigo" value="codigo"/>
								</s:url>
								<a class="btn btn-sm btn-outline-secondary" href="<s:property value='#urlEditar'/>"><s:text name="comum.editar"/></a>
							</s:if>
						</td>
					</tr>
				</s:iterator>
				<s:if test="usuarios.isEmpty()">
					<tr><td colspan="6" class="k-vazio"><s:text name="comum.nenhum.registro"/></td></tr>
				</s:if>
			</tbody>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
