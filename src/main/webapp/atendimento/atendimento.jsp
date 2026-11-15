<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Tela do atendimento (RN-AT): dados do compromisso, resultado (rascunho, liberação, retificação),
     histórico do funcionário e, quando configurado, o painel do assistente de IA (RN-IA) --%>
<s:set var="tituloPagina" value="getText('atendimento.tela.titulo')"/>
<s:set var="menuAtivo" value="'atendimento'"/>
<%@ include file="/WEB-INF/layout/topo.jspf"%>
<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

<div class="row g-3 mt-1">
	<div class="col-12 col-xl-7">
		<%-- Cabeçalho do compromisso --%>
		<div class="k-painel p-3 mb-3">
			<div class="row g-2">
				<div class="col-12 col-sm-5">
					<p class="k-card-rotulo"><s:text name="atendimento.funcionario"/></p>
					<p class="mb-0 fw-semibold"><s:property value="compromisso.codigoFuncionario"/> - <s:property value="compromisso.nomeFuncionario"/></p>
				</div>
				<div class="col-6 col-sm-4">
					<p class="k-card-rotulo"><s:text name="atendimento.agenda"/></p>
					<p class="mb-0 fw-semibold"><s:property value="compromisso.nomeAgenda"/></p>
				</div>
				<div class="col-6 col-sm-3">
					<p class="k-card-rotulo"><s:text name="atendimento.data"/></p>
					<p class="mb-0 fw-semibold"><s:property value="compromisso.dataFormatada"/> <s:property value="compromisso.horarioFormatado"/></p>
				</div>
			</div>
		</div>

		<div class="k-painel p-3">
			<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
				<h2 class="h5 mb-0"><s:text name="atendimento.resultado"/></h2>
				<s:if test="resultadoVo.liberado">
					<span class="badge k-badge-ativo"><s:text name="atendimento.liberado"/> &middot; <s:text name="atendimento.versao"/> <s:property value="resultadoVo.versao"/></span>
				</s:if>
				<s:elseif test="resultadoVo.codigo != null">
					<span class="badge k-badge-rascunho"><s:text name="atendimento.rascunho"/></span>
				</s:elseif>
			</div>

			<s:if test="resultadoVo.liberado">
				<%-- Liberado: só leitura, com assinatura (RN-ME-05). Mudar é retificar (RN-AT-03) --%>
				<dl class="k-detalhe">
					<dt><s:text name="atendimento.indicador"/></dt>
					<dd><s:property value="resultadoVo.indicadorDescricao"/></dd>
					<dt><s:text name="atendimento.achados"/></dt>
					<dd class="k-texto-longo"><s:property value="resultadoVo.achados"/></dd>
					<dt><s:text name="atendimento.parecer"/></dt>
					<dd class="k-texto-longo"><s:property value="resultadoVo.parecer"/></dd>
					<dt><s:text name="atendimento.assinatura"/></dt>
					<dd><s:property value="resultadoVo.assinatura"/> &middot; <s:text name="atendimento.liberado.em"/> <s:property value="resultadoVo.liberadoEmFormatado"/></dd>
				</dl>

				<s:if test="#session.usuario.pode('ATENDIMENTO:ALTERAR')">
					<button type="button" class="btn btn-outline-secondary" data-bs-toggle="collapse" data-bs-target="#retificacao" aria-expanded="false" aria-controls="retificacao">
						<s:text name="atendimento.retificar"/>
					</button>

					<div class="collapse mt-3" id="retificacao">
						<s:form action="retificarAtendimento" method="post" cssClass="k-painel p-3">
							<s:hidden name="codigoCompromisso"/>
							<h3 class="h6"><s:text name="atendimento.retificar.titulo"/></h3>
							<p class="k-suave small"><s:text name="atendimento.retificar.ajuda"/></p>
							<div class="mb-3">
								<label class="form-label" for="motivo"><s:text name="atendimento.motivo"/></label>
								<s:textarea id="motivo" name="motivo" cssClass="form-control" rows="2" maxlength="500"/>
							</div>
							<div class="mb-3">
								<label class="form-label" for="indicadorRet"><s:text name="atendimento.indicador"/></label>
								<s:select id="indicadorRet" name="resultadoVo.indicador" cssClass="form-select"
									list="indicadores" listKey="name()" listValue="descricao"
									headerKey="" headerValue="%{getText('comum.selecione')}"/>
							</div>
							<div class="mb-3">
								<label class="form-label" for="achadosRet"><s:text name="atendimento.achados"/></label>
								<s:textarea id="achadosRet" name="resultadoVo.achados" cssClass="form-control" rows="4" maxlength="10000"/>
							</div>
							<div class="mb-3">
								<label class="form-label" for="parecerRet"><s:text name="atendimento.parecer"/></label>
								<s:textarea id="parecerRet" name="resultadoVo.parecer" cssClass="form-control" rows="5" maxlength="10000"/>
							</div>
							<button type="submit" class="btn k-btn"><s:text name="atendimento.retificar.sim"/></button>
						</s:form>
					</div>
				</s:if>
			</s:if>
			<s:else>
				<%-- Rascunho ou novo: formulário (RN-AT-02). Dois botões, dois destinos (formaction) --%>
				<s:form action="salvarAtendimento" method="post" id="formResultado">
					<s:hidden name="codigoCompromisso"/>
					<div class="mb-3">
						<label class="form-label" for="indicador"><s:text name="atendimento.indicador"/></label>
						<s:select id="indicador" name="resultadoVo.indicador" cssClass="form-select"
							list="indicadores" listKey="name()" listValue="descricao"
							headerKey="" headerValue="%{getText('comum.selecione')}"/>
					</div>
					<div class="mb-3">
						<label class="form-label" for="achados"><s:text name="atendimento.achados"/></label>
						<s:textarea id="achados" name="resultadoVo.achados" cssClass="form-control" rows="5" maxlength="10000"/>
					</div>
					<div class="mb-4">
						<label class="form-label" for="parecer"><s:text name="atendimento.parecer"/></label>
						<s:textarea id="parecer" name="resultadoVo.parecer" cssClass="form-control" rows="6" maxlength="10000"/>
					</div>
					<div class="d-flex flex-wrap gap-2">
						<s:if test="#session.usuario.pode('ATENDIMENTO:INCLUIR')">
							<button type="submit" class="btn btn-outline-secondary"><s:text name="atendimento.salvar.rascunho"/></button>
							<button type="button" class="btn k-btn" data-bs-toggle="modal" data-bs-target="#confirmarLiberacao"><s:text name="atendimento.liberar"/></button>
						</s:if>
						<a class="btn btn-outline-secondary ms-auto" href="<s:url action='filaAtendimento'/>"><s:text name="atendimento.voltar.fila"/></a>
					</div>
				</s:form>

				<%-- Confirmação da liberação: envia o mesmo formulário para outro destino --%>
				<div class="modal fade" id="confirmarLiberacao" tabindex="-1" aria-labelledby="tituloLiberacao" aria-hidden="true">
					<div class="modal-dialog">
						<div class="modal-content">
							<div class="modal-header">
								<h2 class="modal-title h5" id="tituloLiberacao"><s:text name="atendimento.liberar"/></h2>
								<button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="<s:text name='comum.fechar'/>"></button>
							</div>
							<div class="modal-body">
								<p class="mb-0"><s:text name="atendimento.liberar.pergunta"/></p>
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal"><s:text name="comum.cancelar"/></button>
								<button type="submit" class="btn k-btn" form="formResultado" formaction="<s:url action='liberarAtendimento'/>"><s:text name="atendimento.liberar.sim"/></button>
							</div>
						</div>
					</div>
				</div>
			</s:else>
		</div>

		<s:if test="!resultadoVo.versoes.isEmpty()">
			<div class="k-painel p-3 mt-3">
				<h2 class="h5 mb-3"><s:text name="atendimento.versoes"/></h2>
				<s:iterator value="resultadoVo.versoes">
					<div class="k-versao">
						<p class="mb-1"><strong><s:text name="atendimento.versao"/> <s:property value="versao"/></strong> &middot; <s:property value="indicadorDescricao"/> &middot; <s:property value="assinatura"/> &middot; <s:property value="liberadoEmFormatado"/></p>
						<p class="mb-1 k-texto-longo"><s:property value="parecer"/></p>
						<p class="mb-0 small k-suave"><s:text name="atendimento.retificado.por"/> <s:property value="retificadoPor"/> em <s:property value="retificadoEmFormatado"/>: <s:property value="motivo"/></p>
					</div>
				</s:iterator>
			</div>
		</s:if>
	</div>

	<div class="col-12 col-xl-5">
		<%-- Painel do assistente de IA (RN-IA): só aparece para quem pode usar e quando há provedor configurado --%>
		<s:if test="#session.usuario.pode('ASSISTENTE_IA:VER') && iaDisponivel">
			<%@ include file="/WEB-INF/layout/painelIa.jspf"%>
		</s:if>

		<div class="k-painel p-3">
			<h2 class="h5 mb-3"><s:text name="atendimento.historico"/></h2>
			<s:if test="historico.isEmpty()">
				<p class="k-suave mb-0"><s:text name="atendimento.historico.vazio"/></p>
			</s:if>
			<s:iterator value="historico">
				<div class="k-historico-item">
					<p class="mb-1"><strong><s:property value="dataCompromissoFormatada"/></strong> &middot; <s:property value="nomeAgenda"/>
						<s:if test="situacao == 'LIBERADO'"><span class="badge k-badge-ativo ms-1"><s:property value="indicadorDescricao"/></span></s:if>
						<s:elseif test="situacao == 'RASCUNHO'"><span class="badge k-badge-rascunho ms-1"><s:text name="atendimento.rascunho"/></span></s:elseif>
						<s:else><span class="badge k-badge-inativo ms-1"><s:text name="atendimento.sem.resultado"/></span></s:else>
					</p>
					<s:if test="situacao == 'LIBERADO'">
						<p class="mb-0 small k-texto-longo"><s:property value="parecer"/></p>
					</s:if>
				</div>
			</s:iterator>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/layout/rodape.jspf"%>
