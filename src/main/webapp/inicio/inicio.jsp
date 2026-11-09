<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="pt-BR">
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<title><s:text name="app.nome"/> - <s:text name="inicio.titulo"/></title>
		<link rel="stylesheet" href="<s:url value='/webjars/bootstrap/5.1.3/css/bootstrap.min.css'/>">
	</head>
	<body class="bg-dark text-light">
		<div class="container py-5">
			<h1 class="display-5"><s:text name="app.nome"/></h1>
			<p class="lead"><s:text name="app.descricao"/></p>

			<div class="row g-3 mt-3">
				<div class="col-12 col-md-6 col-lg-3">
					<div class="card bg-secondary text-light h-100">
						<div class="card-body">
							<h2 class="h5 card-title"><s:text name="menu.funcionarios"/></h2>
							<span class="badge bg-dark"><s:text name="inicio.em.breve"/></span>
						</div>
					</div>
				</div>
				<div class="col-12 col-md-6 col-lg-3">
					<div class="card bg-secondary text-light h-100">
						<div class="card-body">
							<h2 class="h5 card-title"><s:text name="menu.agendas"/></h2>
							<span class="badge bg-dark"><s:text name="inicio.em.breve"/></span>
						</div>
					</div>
				</div>
				<div class="col-12 col-md-6 col-lg-3">
					<div class="card bg-secondary text-light h-100">
						<div class="card-body">
							<h2 class="h5 card-title"><s:text name="menu.compromissos"/></h2>
							<span class="badge bg-dark"><s:text name="inicio.em.breve"/></span>
						</div>
					</div>
				</div>
				<div class="col-12 col-md-6 col-lg-3">
					<div class="card bg-secondary text-light h-100">
						<div class="card-body">
							<h2 class="h5 card-title"><s:text name="menu.relatorio"/></h2>
							<span class="badge bg-dark"><s:text name="inicio.em.breve"/></span>
						</div>
					</div>
				</div>
			</div>
		</div>

		<script src="<s:url value='/webjars/bootstrap/5.1.3/js/bootstrap.bundle.min.js'/>"></script>
	</body>
</html>
