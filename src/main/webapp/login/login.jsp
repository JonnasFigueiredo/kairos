<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%-- Tela de login. Não usa o topo.jspf: quem ainda não entrou não vê o menu --%>
<!DOCTYPE html>
<html lang="pt-BR">
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="icon" type="image/png" href="<s:url value='/img/kairos-icone.png'/>">
		<title><s:text name="app.nome"/> - <s:text name="login.titulo"/></title>
		<link rel="stylesheet" href="<s:url value='/webjars/bootstrap/5.1.3/css/bootstrap.min.css'/>">
		<link rel="stylesheet" href="<s:url value='/css/kairos.css'/>">
	</head>
	<body class="k-login-corpo">
		<main class="k-login">
			<section class="k-login-apresentacao" aria-labelledby="apresentacao-titulo">
				<div class="k-login-marca">
					<img class="k-logo" src="<s:url value='/img/kairos-icone.png'/>" alt="" aria-hidden="true">
					<s:text name="app.nome"/>
				</div>
				<div class="k-login-mensagem">
					<p class="k-login-chamada"><s:text name="login.chamada"/></p>
					<h2 id="apresentacao-titulo"><s:text name="login.apresentacao.titulo"/></h2>
					<p><s:text name="login.apresentacao.texto"/></p>
				</div>
				<div class="k-login-ilustracao" aria-hidden="true"><span>&#10022;</span><span></span><span></span></div>
			</section>

			<section class="k-login-cartao" aria-labelledby="login-titulo">
				<h1 id="login-titulo" class="k-titulo"><s:text name="login.titulo"/></h1>
				<p class="k-login-descricao"><s:text name="login.descricao"/></p>
				<%@ include file="/WEB-INF/layout/mensagens.jspf"%>

				<s:form action="entrarLogin" method="post">
					<div class="mb-3">
						<label class="form-label" for="usuario"><s:text name="login.usuario"/></label>
						<s:textfield id="usuario" name="usuario" cssClass="form-control" autocomplete="username" autofocus="autofocus"/>
					</div>
					<div class="mb-4">
						<label class="form-label" for="senha"><s:text name="login.senha"/></label>
						<%-- s:password nunca devolve o valor digitado para a página --%>
						<s:password id="senha" name="senha" cssClass="form-control" autocomplete="current-password"/>
					</div>
					<button type="submit" class="btn k-btn w-100"><s:text name="login.entrar"/></button>
				</s:form>
			</section>
		</main>
	</body>
</html>
