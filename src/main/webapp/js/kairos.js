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
