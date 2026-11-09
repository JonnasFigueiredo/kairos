# Kairos

Gestão de funcionários, agendas e compromissos, com relatório por período em tela e em Excel. Desafio técnico do Programa de Formação Dev SOC, criado do zero com as tecnologias do projeto base.

## Rodar

```bash
docker compose up -d        # sobe o MySQL (Podman: podman compose up -d)
./mvnw jetty:run            # app em http://localhost:8080/kairos (Windows: mvnw.cmd)
./mvnw -q test              # testes das regras, sem banco
docker compose down         # para; os dados ficam no volume
docker compose down -v      # para e apaga o banco
```

- Exige JDK 8 no `JAVA_HOME`. Maven vem pelo wrapper; não instalar à parte.
- Configuração do banco em `.env` (copiar de `.env.example`). `.env` nunca vai para o git.
- Sem `.env`, o compose usa valores padrão de desenvolvimento (banco, usuário e senha `kairos`).
- Os scripts de `db/` rodam só com o volume vazio. Mudou a estrutura: `docker compose down -v` e subir de novo.
- Porta 8080 ocupada: `./mvnw jetty:run -Dkairos.porta=8082`.
- `.properties`: o Java 8 lê em ISO-8859-1. Acento vai como escape Unicode; depois de editar, converter com `native2ascii -encoding UTF-8` (ferramenta do JDK 8).
- Mudou arquivo em `src/main/resources` com o Jetty no ar: `./mvnw -q process-resources` em outro terminal, e o Jetty recarrega.

## Stack (fixa: é a do projeto base da prova)

| Camada | Versão |
| --- | --- |
| Java | 8 |
| Web | Struts 2.5.28.3, JSP com tags do Struts |
| Tela | Bootstrap 5.1.3 (webjar), sem framework JavaScript |
| Banco | MySQL 8.4 em container, JDBC puro (`mysql-connector-j`) |
| Servidor | Jetty 9.4.7 (`jetty-maven-plugin`) |
| Excel | Apache POI |
| Log | log4j2 2.17.1 |
| Build | Maven (war) + wrapper |

Não trocar versão nem acrescentar biblioteca sem perguntar ao autor. Nada de Spring, JPA, Lombok ou sintaxe acima do Java 8 (`var`, records, text blocks).

## Estrutura

Pacotes por camada em `src/main/java/br/com/kairos/`:

- `action/`: classes do Struts. Recebem os campos da tela e devolvem o nome do resultado.
- `business/`: regras de negócio. Lançam `BusinessException` com a mensagem que vai para a tela.
- `dao/`: SQL com JDBC. Erro de banco vira `TechnicalException`.
- `vo/`: objetos de dados, sem lógica.
- `infra/`: classes base e enums (`Action`, `Periodo`).
- `exception/`: `BusinessException`, `TechnicalException`.

Fora do Java:

- `src/main/resources/struts.xml`: mapeia URL para Action e resultado para JSP.
- `src/main/resources/*.properties`: textos das telas.
- `src/main/webapp/<modulo>/`: `cad<Entidade>_b.jsp` (consulta) e `cad<Entidade>_i.jsp` (inclusão e alteração), como no projeto base.
- `db/`: script de criação das tabelas.

## Convenções

- Fluxo sempre Action → Business → Dao. Action não tem regra nem SQL; Dao não tem regra; JSP não tem scriptlet.
- Um módulo chama outro só pela classe Business.
- SQL só com `PreparedStatement` e parâmetros `?`. Nunca concatenar valor de tela.
- `Connection`, `PreparedStatement` e `ResultSet` em `try-with-resources`.
- Nunca engolir exceção (`printStackTrace` e seguir). Erro técnico sobe como `TechnicalException`.
- Operação que grava em mais de uma tabela roda em uma transação.
- Quem valida é o servidor. Validação em JavaScript é só conforto.
- Texto de tela vai em `.properties`, não solto no JSP.
- Código, mensagens e textos em português.

## Commits

- Um commit por unidade lógica que compila e roda (uma camada de um módulo, uma regra, um ajuste). Nada de commit "wip" ou "ajustes".
- Assunto no padrão Conventional Commits, em português, no imperativo, até 72 caracteres, sem ponto final: `feat: adiciona cadastro de agendas`.
- Tipos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.
- Corpo só quando há um porquê a registrar. Regra de negócio atendida vai no corpo: `Atende RN-AG-04.`
- Antes de commitar: `./mvnw -q test` passando e `git status` sem arquivo inesperado.
- Não entram no repositório: `.env`, o enunciado da prova, rascunhos e configuração local de IDE ou do assistente.

## Onde está o resto

- Regras de negócio: `docs/requisitos.md`. Ler antes de mexer em um módulo; cada regra tem um código `RN-*`.
- Como a IA é usada neste projeto: `docs/desenvolvimento-com-ia.md`.
- Criar cadastro novo: skill `novo-modulo`.
- Revisão antes de commitar: agentes `revisor-de-camadas` e `verificador-de-requisitos`.
