// Modal de exclusão: quando abre, copia o código e o nome do botão clicado
// (atributos data-codigo e data-nome) para dentro do formulário do modal.
document.addEventListener('show.bs.modal', function (evento) {
	var botao = evento.relatedTarget;
	if (!botao || !botao.hasAttribute('data-codigo')) return;

	var modal = evento.target;
	var campoCodigo = modal.querySelector('[data-exclusao-codigo]');
	var textoNome = modal.querySelector('[data-exclusao-nome]');

	if (campoCodigo) campoCodigo.value = botao.getAttribute('data-codigo');
	// textContent trata o nome como texto puro, nunca como HTML
	if (textoNome) textoNome.textContent = botao.getAttribute('data-nome');
});

// Botão de imprimir do relatório: abre a impressão do navegador.
document.addEventListener('click', function (evento) {
	if (evento.target.closest('[data-imprimir]')) window.print();
});

// Painel do assistente de IA (RN-IA) na tela do atendimento: envia por POST o compromisso e o que
// está digitado no formulário, recebe JSON e desenha as seções. Tudo via textContent: a resposta
// da IA é texto, nunca HTML.
(function () {
	var painel = document.getElementById('painelIa');
	if (!painel) return;

	var botao = painel.querySelector('[data-ia-analisar]');
	var erro = painel.querySelector('[data-ia-erro]');
	var aviso = painel.querySelector('[data-ia-aviso]');
	var resposta = painel.querySelector('[data-ia-resposta]');
	var textoDoBotao = botao.textContent;

	function campoDoFormulario(nome) {
		// Rascunho (formResultado) ou retificação: o primeiro campo visível com esse nome
		var campos = document.querySelectorAll('[name="resultadoVo.' + nome + '"]');
		for (var i = 0; i < campos.length; i++) {
			if (campos[i].offsetParent !== null) return campos[i];
		}
		return campos[0] || null;
	}

	function valor(nome) {
		var campo = campoDoFormulario(nome);
		return campo ? campo.value : '';
	}

	function mostrarErro(mensagem) {
		erro.textContent = mensagem;
		erro.classList.remove('d-none');
		resposta.classList.add('d-none');
	}

	function preencherLista(nome, itens) {
		var lista = painel.querySelector('[data-ia-lista="' + nome + '"]');
		lista.textContent = '';
		(itens || []).forEach(function (item) {
			var li = document.createElement('li');
			li.textContent = item;
			lista.appendChild(li);
		});
	}

	function desenhar(analise) {
		['resumo', 'comparacaoComAnteriores', 'sugestaoDeParecer', 'limitacoes', 'provedor'].forEach(function (nome) {
			painel.querySelector('[data-ia-campo="' + nome + '"]').textContent = analise[nome] || '';
		});
		preencherLista('pontosDeAtencao', analise.pontosDeAtencao);
		preencherLista('dadosFaltantes', analise.dadosFaltantes);
		aviso.classList.toggle('d-none', analise.estruturada !== false);
		erro.classList.add('d-none');
		resposta.classList.remove('d-none');
	}

	botao.addEventListener('click', function () {
		botao.disabled = true;
		botao.textContent = painel.getAttribute('data-texto-analisando');
		erro.classList.add('d-none');

		var dados = new URLSearchParams();
		dados.append('codigoCompromisso', painel.getAttribute('data-compromisso'));
		dados.append('resultadoVo.indicador', valor('indicador'));
		dados.append('resultadoVo.achados', valor('achados'));
		dados.append('resultadoVo.parecer', valor('parecer'));

		fetch(painel.getAttribute('data-url'), {
			method: 'POST',
			credentials: 'same-origin',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8', 'Accept': 'application/json' },
			body: dados.toString()
		})
		.then(function (r) { return r.json().then(function (json) { return { ok: r.ok, json: json }; }); })
		.then(function (r) {
			if (!r.ok || r.json.erro) {
				mostrarErro(r.json.erro || painel.getAttribute('data-texto-erro'));
				return;
			}
			desenhar(r.json);
		})
		.catch(function () { mostrarErro(painel.getAttribute('data-texto-erro')); })
		.then(function () {
			botao.disabled = false;
			botao.textContent = textoDoBotao;
		});
	});

	// RN-IA-03: copia a sugestão para o parecer; o campo continua editável e nada é salvo sozinho
	painel.querySelector('[data-ia-usar-sugestao]').addEventListener('click', function () {
		var parecer = campoDoFormulario('parecer');
		var sugestao = painel.querySelector('[data-ia-campo="sugestaoDeParecer"]').textContent;
		if (!parecer || !sugestao) return;
		parecer.value = sugestao;
		parecer.focus();
	});
})();

// Formulário de usuário: a caixa "É médico" mostra os campos do CRM e libera as linhas
// da matriz que são só de médico (RN-ME-03). É conforto: quem valida é o servidor.
(function () {
	var caixa = document.getElementById('ehMedico');
	if (!caixa) return;

	var campos = document.querySelector('[data-medico-campos]');
	var linhas = document.querySelectorAll('[data-so-medico]');

	function atualizar() {
		var medico = caixa.checked;
		if (campos) campos.classList.toggle('k-oculto', !medico);
		linhas.forEach(function (linha) {
			linha.classList.toggle('k-desligado', !medico);
			if (!medico) {
				linha.querySelectorAll('input[type="checkbox"]').forEach(function (c) { c.checked = false; });
			}
		});
	}

	caixa.addEventListener('change', atualizar);
	atualizar();
})();
