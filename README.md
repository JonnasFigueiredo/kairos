# Kairos

Gestão de funcionários, agendas e compromissos, com relatório por período em tela e em Excel.

Projeto do desafio técnico do Programa de Formação. Foi criado do zero, com a estrutura em camadas e as tecnologias do projeto base (Java 8, Struts 2, JSP, Bootstrap, Jetty, Maven).

## O que o sistema faz

| # | Atividade do desafio | Onde está |
| --- | --- | --- |
| 1 | CRUD de funcionários | Menu Funcionários |
| 2 | CRUD de agendas, com o período disponível em um `<select>` (Manhã, Tarde, Ambos) | Menu Agendas |
| 3 | CRUD de compromissos | Menu Compromissos |
| 4 | Excluir um funcionário exclui os compromissos dele | `FuncionarioBusiness.excluir` |
| 5 | Agenda com compromissos não pode ser excluída | `AgendaBusiness.excluir` |
| 6 | Compromisso fora do período da agenda é recusado | `CompromissoBusiness.validarHorario` |
| 7 | Relatório de compromissos por período, em HTML e em Excel | Menu Relatório |

Cada regra tem um código (`RN-FU-03`, `RN-CO-04`...) em [docs/requisitos.md](docs/requisitos.md), citado no código onde ela é aplicada.

## Como rodar

Pré-requisitos: **JDK 8** e **Docker** ou **Podman**. O Maven vem junto com o projeto (wrapper).

```bash
# 1. Banco de dados (MySQL 8.4 em container)
docker compose up -d            # com Podman: podman compose up -d

# 2. Aplicação
./mvnw jetty:run                # no Windows: mvnw.cmd jetty:run
```

Abra http://localhost:8080/kairos

- Na primeira subida o MySQL cria as tabelas e alguns dados de exemplo. Pode levar de um a quatro minutos; `docker compose ps` mostra `healthy` quando terminou.
- O `JAVA_HOME` precisa apontar para um JDK 8.
- Porta 8080 ocupada: `./mvnw jetty:run -Dkairos.porta=8082`.
- Banco, usuário e senha têm valores padrão de desenvolvimento. Para trocar, copie `.env.example` para `.env`.

Para parar: `Ctrl+C` na aplicação e `docker compose down` no banco. Os dados ficam no volume; `docker compose down -v` apaga tudo e recria do zero na próxima subida.

## Testes

```bash
./mvnw test
```

49 testes de unidade, sem banco. Cobrem as regras que não dependem de dados gravados: validações, a fronteira entre manhã e tarde (11:59 e 12:00), os filtros do relatório e o conteúdo da planilha. A relação entre cada regra e a classe de teste está em [docs/requisitos.md](docs/requisitos.md#testes-automatizados).

## Tecnologias

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 8 |
| Web | Struts 2.5.28.3, JSP com as tags do Struts |
| Tela | Bootstrap 5.1.3 |
| Banco | MySQL 8.4 em container, acesso por JDBC |
| Servidor | Jetty 9.4.7 (plugin do Maven) |
| Excel | Apache POI 5.2.5 |
| Testes | JUnit 4 |
| Build | Maven |

## Estrutura

Toda requisição segue o mesmo caminho:

```
JSP → Action → Business → Dao → MySQL
```

```
src/main/java/br/com/kairos/
  action/      recebe os campos da tela e devolve o nome do resultado
  business/    regras de negócio; lança BusinessException
  dao/         SQL com JDBC; lança TechnicalException
  vo/          objetos de dados
  infra/       classes de apoio: conexão, transação, enum de período, conversores, planilha
  exception/   BusinessException e TechnicalException
src/main/resources/
  struts.xml                       mapeia URL → Action → JSP
  ApplicationResources.properties  textos das telas
src/main/webapp/                   JSPs, CSS e JavaScript
db/                                criação das tabelas e dados de exemplo
docs/                              requisitos e processo de desenvolvimento
```

## Decisões

Pontos em que o enunciado não define o comportamento, ou em que este projeto difere do projeto base.

- **MySQL em container no lugar do H2 em memória.** Os dados sobrevivem ao reinício da aplicação, e as regras são exercitadas contra um banco de verdade, com chaves estrangeiras. O custo é precisar de Docker ou Podman para rodar.
- **Fronteira entre manhã e tarde.** Manhã vai de 00:00 a 11:59; tarde, de 12:00 a 23:59. A decisão está em um lugar só, o enum `Periodo`.
- **Mudança de período da agenda.** O período só pode mudar se os compromissos existentes continuarem dentro dele. Sem isso, a atividade 6 poderia ser contornada: criar o compromisso e depois trocar o período da agenda.
- **Exclusão em cadeia na camada de negócio.** A exclusão do funcionário e dos compromissos dele roda em uma transação, na classe Business, em vez de `ON DELETE CASCADE` no banco. A regra fica visível no código; as chaves estrangeiras continuam como última barreira.
- **Erro técnico não some.** Falha de banco vira `TechnicalException`, vai para o log e o usuário vê uma página de aviso. Nada de `printStackTrace` seguido de lista vazia.
- **Um formulário para incluir e alterar.** O código vai em campo escondido; vazio é inclusão, preenchido é alteração.
- **Excluir e gravar só por POST.** Um link não altera dados.
- **Saída sempre com escape.** Os JSPs usam `<s:property>`, e as consultas usam `PreparedStatement` com parâmetros.
- **Data e hora como valores no Excel.** O usuário consegue ordenar e filtrar a planilha.

## Limites conhecidos

- A validação e a gravação usam conexões separadas. Dois usuários ao mesmo tempo podem, em tese, deixar um compromisso fora do período da agenda. A correção é validar e gravar na mesma transação, com bloqueio da linha da agenda.
- Não há login nem proteção contra envio forjado de formulário (CSRF).
- Não há pool de conexões nem teste automatizado com banco.

## Documentação

- [docs/requisitos.md](docs/requisitos.md): as regras de negócio, a origem de cada uma e os testes que as cobrem.
- [docs/desenvolvimento-com-ia.md](docs/desenvolvimento-com-ia.md): como um assistente de IA foi usado no desenvolvimento, com requisitos numerados, roteiros, verificação automática e revisão independente.
