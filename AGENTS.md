# Instruções para assistentes de IA

Este arquivo é lido por assistentes de programação com IA (Codex, Cursor, GitHub Copilot, Claude Code e outros) antes de mexer no projeto. Vale também para pessoas: é o resumo do que precisa ser respeitado.

O Kairos é um sistema de gestão de funcionários, agendas e compromissos, com relatório em tela e em Excel, permissões por módulo, atendimento médico e um assistente de IA no atendimento. Visão geral e telas: [README.md](README.md). Regras de negócio: [docs/requisitos.md](docs/requisitos.md).

## Comandos

```bash
./subir.sh                  # banco (Docker ou Podman) + aplicação em http://localhost:8080/kairos
./subir.sh banco            # só o banco
./parar.sh                  # para o banco; "./parar.sh apagar" apaga os dados
./mvnw -q test              # testes de unidade, sem banco
./mvnw -q compile           # só compila
git config core.hooksPath .githooks   # uma vez por clone: testes antes de cada commit
```

No Windows, use `subir.cmd`, `parar.cmd` e `mvnw.cmd`.

- O projeto exige **JDK 8** no `JAVA_HOME`. Java mais novo não roda o Jetty da stack.
- O hook `.githooks/pre-commit` roda os testes e recusa o commit se algum falhar. Não contorne com `--no-verify`: corrija o teste ou o código.
- Usuários de desenvolvimento: `admin` e `medica`, senha `kairos`.
- Antes de dar uma tarefa por concluída: `./mvnw -q test` passando e o fluxo testado na tela.

## Stack

A stack é fixa: é a do projeto base do desafio.

| Camada | Versão |
| --- | --- |
| Java | 8 |
| Web | Struts 2.5.28.3 e JSP com as tags do Struts, mais o `struts2-json-plugin` da mesma versão |
| Tela | Bootstrap 5.1.3 (webjar) e JavaScript puro |
| Banco | MySQL 8.4 em container, JDBC puro (`mysql-connector-j`) |
| Servidor | Jetty 9.4.7 (`jetty-maven-plugin`) |
| Excel | Apache POI 5.2.5 |
| Testes | JUnit 4 |
| Build | Maven com wrapper |

- Não troque versão nem acrescente biblioteca sem pedir ao responsável pelo projeto.
- Nada de Spring, JPA, Lombok ou sintaxe acima do Java 8 (`var`, records, text blocks, `List.of`).
- `log4j-api` e `commons-io` têm versão fixada no `pom.xml` de propósito. Não remova.
- Chamadas HTTP aos provedores de IA usam `HttpURLConnection`, sem SDK.

## Estrutura

```
src/main/java/br/com/kairos/
  action/      Actions do Struts: recebem a tela e escolhem o resultado
  business/    regras de negócio; lançam BusinessException com a mensagem da tela
  dao/         SQL com JDBC; erro de banco vira TechnicalException
  vo/          objetos de dados, sem regra
  infra/       base das Actions, conexão, transação, interceptores, enums (Periodo, Modulo, Acao, Indicador)
  infra/ia/    assistente de IA: interface ModeloDeLinguagem e um adaptador por provedor
  exception/   BusinessException, IaIndisponivelException, TechnicalException
src/main/resources/
  struts.xml                       URL, Action, módulo de permissão e página de resultado
  ApplicationResources.properties  textos das telas
src/main/webapp/<modulo>/          cad<Entidade>_b.jsp (consulta) e cad<Entidade>_i.jsp (formulário)
db/                                criação das tabelas e dados de exemplo
db/atualizacoes/                   scripts para bancos já criados
```

## Convenções

**Camadas**
- O fluxo é sempre JSP, Action, Business, Dao. Action não tem regra nem SQL. Dao não tem regra. JSP não tem scriptlet.
- Um módulo chama outro pela classe Business. Exceção: dentro de uma `Transacao`, a Business usa o Dao do outro módulo, para usar a mesma conexão.
- Operação que grava em mais de uma tabela roda em `Transacao`.
- Exclusão em cadeia é regra de negócio: fica na Business, não em `ON DELETE CASCADE`.

**Banco**
- SQL só com `PreparedStatement` e parâmetros `?`. Nunca concatene valor vindo da tela.
- `Connection`, `PreparedStatement` e `ResultSet` em `try-with-resources`.
- Parâmetro de `LIKE` é montado com `Dao.contendo`, que escapa os curingas.

**Erros**
- Nunca engula exceção. Erro técnico sobe como `TechnicalException`, com a causa.
- Mensagem para o usuário é `BusinessException`. Mensagem técnica não chega à tela.

**Telas e Struts**
- Quem valida é o servidor. Validação em JavaScript é só conforto.
- Método que grava ou exclui só aceita POST (`requisicaoEhPost`).
- Toda Action de módulo declara `<param name="autorizacao.modulo">` no `struts.xml`. O `AutorizacaoInterceptor` deduz a ação exigida pelo nome do método (`Acao.exigidasPor`). Em `salvar`, a Action confere inclusão ou alteração com `podeGravar`.
- Menu e botões só aparecem para quem tem a permissão (`#session.usuario.pode('MODULO:ACAO')`), mas quem barra é o servidor.
- Lista de `<s:select>` vinda do banco é carregada em `prepare<Metodo>` (interface `Preparable`), nunca em getter chamado pelo JSP.
- Texto de tela vai no `.properties`, não solto no JSP.

**Regras de negócio**
- Cada regra tem um código em `docs/requisitos.md` (`RN-CO-04`, `RN-AT-03`...). Leia a seção do módulo antes de mexer nele.
- Cite o código da regra no ponto do código que a aplica, no teste que a cobre e no commit.
- Regra nova ou alterada: atualize `docs/requisitos.md` no mesmo trabalho.

**Idioma**
- Código, mensagens, textos e commits em português.

## Segurança e dados

- Nunca leia, crie ou altere `.env`, e nunca coloque chave de API, senha ou token no código, no banco ou no git. A configuração do assistente de IA do sistema fica no `.env` (modelo em `.env.example`).
- Senha de usuário é gravada e conferida só pela classe `Senha` (PBKDF2).
- O que sai para os provedores de IA passa pelo `MontadorDeContexto`, que tira os dados pessoais. Não envie dados de funcionário por outro caminho.
- Não apague dados nem bancos sem pedido explícito.

## Como trabalhar uma tarefa

1. Leia as regras do módulo em `docs/requisitos.md` e o código que já existe ao redor.
2. Mude uma camada por vez e compile a cada passo.
3. Escreva teste de unidade para regra que não depende do banco.
4. Rode `./mvnw -q test` e percorra o fluxo na tela, com um caso que passa e um que falha para cada regra.
5. Revise com o checklist abaixo antes de propor o commit.

## Criar um cadastro novo

1. **Regras:** leia ou escreva a seção do módulo em `docs/requisitos.md`.
2. **Tabela:** acrescente em `db/01_cria_tabelas.sql`, com chave primária `AUTO_INCREMENT`, `NOT NULL` nos obrigatórios e chave estrangeira nos vínculos. Para bancos já criados, um script em `db/atualizacoes/`.
3. **Vo:** campos, getters e setters, sem lógica.
4. **Dao:** estende `Dao`. Métodos `inserir`, `alterar`, `excluir`, `buscarPorCodigo`, `listarTodos` e as buscas da tela. Método usado em transação recebe a `Connection`.
5. **Business:** um método por caso de uso. Valida os obrigatórios e as regras, cada uma com o comentário do código (`// RN-XX-00`).
6. **Action:** estende `infra.Action`. Métodos `listar`, `novo`, `editar`, `salvar` e `excluir`. Captura `BusinessException` e usa `addActionError`.
7. **Permissão:** acrescente o módulo no enum `Modulo` e declare `autorizacao.modulo` na Action do `struts.xml`.
8. **struts.xml:** `<action name="*Entidades" method="{1}">` com os resultados `success`, `input` e `redirect` e a lista `allowed-methods`.
9. **Textos:** rótulos no `ApplicationResources.properties`.
10. **JSPs:** `cad<Entidade>_b.jsp` e `cad<Entidade>_i.jsp` com o layout comum (`topo.jspf`, `mensagens.jspf`, `rodape.jspf`).
11. **Menu e acesso direto:** item no `topo.jspf` condicionado à permissão, e a pasta do JSP no `security-constraint` do `web.xml`.
12. **Testes e tela:** teste das regras e o fluxo completo na tela.

## Checklist de revisão

- [ ] Nenhuma Action com regra, SQL ou acesso a Dao. Nenhuma Business com SQL ou classe do Struts. Nenhum Dao com regra.
- [ ] Nenhum SQL concatenado. Recursos JDBC em `try-with-resources`. Gravação em várias tabelas dentro de `Transacao`.
- [ ] Nenhum `catch` vazio ou só com `printStackTrace`. `SQLException` vira `TechnicalException`.
- [ ] Nada acima do Java 8 e nenhuma biblioteca nova.
- [ ] Cada regra `RN-*` tocada está aplicada nos dois caminhos que ela exige (inclusão e alteração, por exemplo) e tem teste quando não depende do banco.
- [ ] Action nova declara o módulo de permissão. Gravação só por POST.
- [ ] Texto novo no `.properties`, com acentos em escape Unicode.
- [ ] `./mvnw -q test` passando.

## Commits

- Um commit por unidade lógica que compila e roda.
- Conventional Commits em português, no imperativo, até 72 caracteres, sem ponto final: `feat: adiciona cadastro de agendas`. Tipos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.
- Corpo só quando há um porquê a registrar. Regra atendida vai no corpo: `Atende RN-AG-04.`
- Nunca entram no repositório: `.env`, credenciais, o enunciado do desafio, rascunhos e configuração local de IDE ou de assistente.

## Armadilhas conhecidas

Problemas que já aconteceram neste projeto. Cada novo tropeço vira uma linha aqui.

- **Acentos no `.properties`:** o Java 8 lê o arquivo em ISO-8859-1. Acento direto aparece quebrado na tela. Escreva em escape Unicode (`ç`) ou converta com `native2ascii -encoding UTF-8`, do JDK 8.
- **`getX()` e `isX()` na mesma classe:** um VO com `getMedico()` e `isMedico()` confunde o Struts, que deixa de preencher o campo vindo da tela. Dê nomes diferentes (`ehMedico()`).
- **`<s:iterator>` aninhado:** dentro do laço, um nome de propriedade é procurado primeiro no item atual. Se o item tiver uma propriedade de mesmo nome, ela vence. Guarde a lista externa com `<s:set>` antes do laço.
- **Atributo sem valor em tag do Struts:** `data-algo` sem `=""` numa tag `<s:...>` quebra a compilação do JSP ("equal symbol expected").
- **Scripts de `db/`:** rodam só quando o volume do banco está vazio. Mudança de tabela em banco existente pede um script em `db/atualizacoes/`.
- **Mensagens nas respostas HTTP:** o Struts escapa os acentos como entidades HTML (`n&atilde;o`). Leve isso em conta ao testar por `curl`.
- **Porta 3306:** Docker e Podman não podem ter o banco no ar ao mesmo tempo. Pare um antes de subir o outro.
- **Windows e caminho de programas:** com `NoDefaultCurrentDirectoryInExePath` ativo, um script não acha `mvnw.cmd` na pasta atual. Chame por caminho completo (`%~dp0mvnw.cmd`).
