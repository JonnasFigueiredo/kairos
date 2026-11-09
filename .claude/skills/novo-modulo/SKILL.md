---
name: novo-modulo
description: Cria um cadastro novo no Kairos (tabela, Vo, Dao, Business, Action, struts.xml, textos e JSPs) seguindo o padrão do projeto. Use ao implementar funcionário, agenda, compromisso ou qualquer CRUD novo.
---

# Criar módulo no Kairos

Uma classe por vez.

1. **Regras.** Ler a seção do módulo em `docs/requisitos.md`. Cada `RN-*` vira uma validação na classe Business.
2. **Tabela.** Acrescentar em `db/` o `CREATE TABLE` com chave primária `auto_increment`, `NOT NULL` nos obrigatórios e chave estrangeira onde houver vínculo. Sem `ON DELETE CASCADE`: exclusão em cadeia é regra e fica na Business.
3. **Vo**, em `vo/<Entidade>Vo.java`: campos, getters e setters. Sem lógica.
4. **Dao**, em `dao/<Entidade>Dao.java`, estendendo `Dao`:
   - Métodos: `inserir`, `alterar`, `excluir`, `buscarPorCodigo`, `listarTodos` e as buscas da tela.
   - Só `PreparedStatement` com `?`. Recursos em `try-with-resources`.
   - `SQLException` vira `TechnicalException`, com a causa.
   - Método usado dentro de transação recebe a `Connection` por parâmetro.
5. **Business**, em `business/<Entidade>Business.java`:
   - Um método por caso de uso (`salvar`, `excluir`, `buscar`, `listar`).
   - Valida os obrigatórios e as `RN-*`. Lança `BusinessException` com a mensagem da tela.
   - Comentário com o código da regra em cada validação: `// RN-AG-04`.
   - Precisa de outro módulo: chamar a Business dele, nunca o Dao dele.
6. **Action**, em `action/<Entidade>Action.java`, estendendo `infra.Action`:
   - Métodos `todos`, `filtrar`, `novo`, `editar`, `salvar`, `excluir`.
   - Captura `BusinessException` e chama `addActionError` para a mensagem aparecer na tela.
   - Sem regra e sem SQL.
7. **struts.xml.** Um `<action name="*<Entidades>">` com `method="{1}"` e os resultados `success` (consulta), `input` (formulário) e `redirect`.
8. **Textos.** Rótulos novos em `ApplicationResources.properties`.
9. **JSPs**, em `src/main/webapp/<modulo>/`:
   - `cad<Entidade>_b.jsp`: busca, tabela, botões de novo, editar e excluir com confirmação.
   - `cad<Entidade>_i.jsp`: o mesmo formulário para incluir e alterar.
   - Usar as tags `<s:...>` e o layout comum. Sem scriptlet. Lista de opções com `<s:select>`.
   - Mostrar erros com `<s:actionerror/>`.
10. **Menu.** Acrescentar o item no layout comum.
11. **Teste.** Regra que não depende do banco (cálculo, comparação de horário) ganha teste JUnit.
12. **Validar.** `./mvnw -q test`, subir com `./mvnw jetty:run` e percorrer na tela: incluir, consultar, alterar, excluir e cada regra com um caso que passa e um que falha.
13. **Fechar.** Rodar os agentes `revisor-de-camadas` e `verificador-de-requisitos`. Atualizar `docs/requisitos.md` se alguma regra mudou.
