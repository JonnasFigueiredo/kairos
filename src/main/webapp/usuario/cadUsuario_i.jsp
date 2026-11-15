<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Formulário de usuário: conta, matriz de acessos (RN-PE-01) e dados de médico (RN-ME-01) --%>
<s:if test="usuarioVo.codigo == null">
	<s:set var="tituloPagina" value="getText('usuario.novo')"/>
</s:if>
<s:else>
	<s:set var="tituloPagina" value="getText('usuario.editar')"/>
</s:else>
<s:set var="menuAtivo" value="'usuarios'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="k-painel k-formulario k-formulario-largo mt-3">
	<s:form action="salvarUsuarios" method="post" autocomplete="off">
		<s:hidden name="usuarioVo.codigo"/>

		<h2 class="h5 mb-3"><s:text name="usuario.secao.conta"/></h2>
		<div class="row g-3 mb-3">
			<div class="col-12 col-sm-4">
				<label class="form-label" for="login"><s:text name="usuario.login"/></label>
				<s:if test="usuarioVo.codigo == null">
					<s:textfield id="login" name="usuarioVo.login" cssClass="form-control" maxlength="60" autofocus="autofocus"/>
				</s:if>
				<s:else>
					<%-- O login não muda depois de criado; vai no campo escondido para a Business não o perder --%>
					<input id="login" class="form-control" value="<s:property value='usuarioVo.login'/>" disabled>
					<s:hidden name="usuarioVo.login"/>
				</s:else>
			</div>
			<div class="col-12 col-sm-8">
				<label class="form-label" for="nome"><s:text name="comum.nome"/></label>
				<s:textfield id="nome" name="usuarioVo.nome" cssClass="form-control" maxlength="255"/>
			</div>
			<div class="col-12 col-sm-6">
				<label class="form-label" for="senha"><s:text name="usuario.senha"/></label>
				<%-- s:password nunca devolve o valor digitado para a página --%>
				<s:password id="senha" name="usuarioVo.senha" cssClass="form-control" autocomplete="new-password"/>
				<s:if test="usuarioVo.codigo != null">
					<div class="form-text"><s:text name="usuario.senha.manter"/></div>
				</s:if>
			</div>
			<div class="col-12 col-sm-6 d-flex align-items-end">
				<div class="form-check form-switch mb-2">
					<s:checkbox id="ativo" name="usuarioVo.ativo" cssClass="form-check-input"/>
					<label class="form-check-label" for="ativo"><s:text name="usuario.ativo.conta"/></label>
				</div>
			</div>
		</div>

		<h2 class="h5 mb-3"><s:text name="usuario.secao.medico"/></h2>
		<div class="form-check form-switch mb-3">
			<s:checkbox id="ehMedico" name="ehMedico" cssClass="form-check-input"/>
			<label class="form-check-label" for="ehMedico"><s:text name="usuario.eh.medico"/></label>
		</div>
		<div class="row g-3 mb-3" data-medico-campos>
			<div class="col-12 col-sm-4">
				<label class="form-label" for="crm"><s:text name="usuario.crm"/></label>
				<s:textfield id="crm" name="usuarioVo.medico.crm" cssClass="form-control" maxlength="10" inputmode="numeric"/>
			</div>
			<div class="col-12 col-sm-3">
				<label class="form-label" for="ufCrm"><s:text name="usuario.uf.crm"/></label>
				<s:select id="ufCrm" name="usuarioVo.medico.ufCrm" cssClass="form-select" list="ufs"
					headerKey="" headerValue="%{getText('comum.selecione')}"/>
			</div>
			<div class="col-12 col-sm-5">
				<label class="form-label" for="especialidade"><s:text name="usuario.especialidade"/></label>
				<s:textfield id="especialidade" name="usuarioVo.medico.especialidade" cssClass="form-control" maxlength="100"/>
			</div>
		</div>

		<h2 class="h5 mb-2"><s:text name="usuario.secao.acessos"/></h2>
		<p class="form-text mb-3"><s:text name="usuario.acessos.ajuda"/></p>
		<div class="table-responsive mb-4">
			<table class="table k-tabela k-matriz mb-0">
				<thead>
					<tr>
						<th><s:text name="usuario.modulo"/></th>
						<s:iterator value="acoes" var="acao">
							<th class="text-center"><s:property value="#acao"/></th>
						</s:iterator>
					</tr>
				</thead>
				<tbody>
					<s:iterator value="modulos" var="modulo">
						<tr <s:if test="#modulo.soMedico">data-so-medico</s:if>>
							<td>
								<s:property value="#modulo.descricao"/>
								<s:if test="#modulo.soMedico"><small class="k-suave d-block"><s:text name="usuario.so.medico"/></small></s:if>
							</td>
							<s:iterator value="acoes" var="acao">
								<td class="text-center">
									<s:if test="#modulo.aceita(#acao)">
										<s:set var="chave" value="#modulo.chave(#acao)"/>
										<input type="checkbox" class="form-check-input" name="usuarioVo.permissoes"
											value="<s:property value='#chave'/>"
											aria-label="<s:property value='#modulo.descricao'/> - <s:property value='#acao'/>"
											<s:if test="usuarioVo.permissoes.contains(#chave)">checked</s:if>>
									</s:if>
								</td>
							</s:iterator>
						</tr>
					</s:iterator>
				</tbody>
			</table>
		</div>

		<div class="d-flex gap-2">
			<button type="submit" class="btn k-btn"><s:text name="comum.salvar"/></button>
			<a class="btn btn-outline-secondary" href="<s:url action='listarUsuarios'/>"><s:text name="comum.cancelar"/></a>
		</div>
	</s:form>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
