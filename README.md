# Kairos

Gestão de funcionários, agendas e compromissos, com relatório por período em tela e em Excel.

Projeto do desafio técnico do Programa de Formação. Foi criado do zero, com a estrutura em camadas e as tecnologias do projeto base (Java 8, Struts 2, JSP, Bootstrap, Jetty, Maven).

## O que o sistema faz

| # | Atividade do desafio | Onde está |
| --- | --- | --- |
| 1 | CRUD de funcionários | Menu Funcionários |
| 2 | CRUD de agendas, com o período disponível em um `<select>` (Manhã, Tarde, Ambos) | Menu Agendas |
| 3 | CRUD de compromissos | Menu Compromissos (tabela e calendário do mês) |
| 4 | Excluir um funcionário exclui os compromissos dele | `FuncionarioBusiness.excluir` |
| 5 | Agenda com compromissos não pode ser excluída | `AgendaBusiness.excluir` |
| 6 | Compromisso fora do período da agenda é recusado | `CompromissoBusiness.validarHorario` |
| 7 | Relatório de compromissos por período, em HTML e em Excel | Menu Relatório |

Cada regra tem um código (`RN-FU-03`, `RN-CO-04`...) em [docs/requisitos.md](docs/requisitos.md), citado no código onde ela é aplicada.

### Além do desafio

Sobre o mesmo esqueleto (Struts, JSP, JDBC), o sistema também tem:

| Função | Onde está |
| --- | --- |
| Usuários e acessos: contas da equipe, matriz de permissões por módulo (ver, incluir, alterar, excluir) e cadastro de médico (CRM e UF) | Menu Usuários e acessos; `AutorizacaoInterceptor` barra no servidor |
| Atendimento do médico: fila do dia, resultado do exame em rascunho, liberação assinada e retificação com versões | Menu Atendimento (só conta de médico) |
| Resultados: leitura dos resultados liberados | Menu Resultados |
| Assistente de IA: análise do compromisso e do histórico, pseudonimizada, em seções, com sugestão de parecer | Painel na tela do atendimento; liga por `KAIROS_IA_PROVEDOR` |

Usuários de desenvolvimento: `admin` (administra tudo) e `medica` (atende e usa a IA), os dois com a senha `kairos`.

## Como rodar

Pré-requisitos: **JDK 8** e **Docker** ou **Podman**. O Maven vem junto com o projeto (wrapper).

```bash
# 1. Banco de dados (MySQL 8.4 em container)
docker compose up -d            # com Podman: podman compose up -d

# 2. Aplicação
./mvnw jetty:run                # no Windows: mvnw.cmd jetty:run
```

Abra http://localhost:8080/kairos e entre com o usuário de desenvolvimento: `admin`, senha `kairos`.

- Na primeira subida o MySQL cria as tabelas e alguns dados de exemplo. Pode levar de um a quatro minutos; `docker compose ps` mostra `healthy` quando terminou.
- O `JAVA_HOME` precisa apontar para um JDK 8.
- Porta 8080 ocupada: `./mvnw jetty:run -Dkairos.porta=8082`.
- Banco, usuário e senha têm valores padrão de desenvolvimento. Para trocar, copie `.env.example` para `.env`.
- Banco criado antes do atendimento e da IA (sem as tabelas `permissao`, `medico`, `resultado`, `analise_ia`): aplique `db/atualizacoes/2026-11-15_atendimento_e_ia.sql` com o banco no ar (o comando está no cabeçalho do arquivo) ou recrie com `docker compose down -v`.
- Assistente de IA: desligado por padrão. Para ligar, defina `KAIROS_IA_PROVEDOR` e a chave no `.env` (ver `.env.example`); `KAIROS_IA_PROVEDOR=falso` mostra o painel com uma resposta de demonstração, sem chave nem internet.

Para parar: `Ctrl+C` na aplicação e `docker compose down` no banco. Os dados ficam no volume; `docker compose down -v` apaga tudo e recria do zero na próxima subida.

## Testes

```bash
./mvnw test
```

116 testes de unidade, sem banco nem internet. Cobrem as regras que não dependem de dados gravados: validações, a fronteira entre manhã e tarde (11:59 e 12:00), os filtros do relatório, o conteúdo da planilha, a grade do calendário, o cálculo da senha, o bloqueio de quem não entrou, a matriz de permissões, as regras do resultado do exame, a pseudonimização do que vai para a IA e a troca entre provedores. A relação entre cada regra e a classe de teste está em [docs/requisitos.md](docs/requisitos.md#testes-automatizados).

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
  infra/       classes de apoio: conexão, transação, enums de período, indicador, módulo e ação, conversores,
               planilha, calendário, senha, interceptores de login e de permissão
  infra/ia/    assistente de IA: interface do modelo, adaptadores dos provedores, contexto pseudonimizado
  exception/   BusinessException, IaIndisponivelException e TechnicalException
src/main/resources/
  struts.xml                       mapeia URL → Action → JSP
  ApplicationResources.properties  textos das telas
src/main/webapp/                   JSPs, CSS e JavaScript
db/                                criação das tabelas e dados de exemplo
db/atualizacoes/                   script para atualizar um banco criado antes do atendimento
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
- **Excluir e gravar só por POST, vindo do próprio Kairos.** Um link não altera dados, e um formulário enviado de outro site é recusado pelos cabeçalhos `Origin` e `Referer`.
- **Login por interceptor.** O enunciado não pede login; foi acrescentado. Um interceptor do Struts fica na frente de todas as Actions e só deixa passar quem tem sessão, então uma Action nova já nasce protegida. A senha é guardada como PBKDF2 com sal, que vem no Java 8, sem biblioteca extra. O identificador da sessão é trocado ao entrar.
- **Saída sempre com escape.** Os JSPs usam `<s:property>`, e as consultas usam `PreparedStatement` com parâmetros.
- **Data e hora como valores no Excel.** O usuário consegue ordenar e filtrar a planilha.
- **Permissão por interceptor.** Cada Action declara o módulo no `struts.xml`, e um segundo interceptor deduz a ação pelo nome do método. O menu esconde o que a pessoa não pode usar, mas quem barra é o servidor.
- **Resultado à parte do compromisso.** O compromisso continua igual; o resultado do exame é outra tabela, um por compromisso, com versões guardadas a cada retificação.
- **IA sem SDK.** Os provedores são chamados com o `HttpURLConnection` do Java 8, e o JSON é o do plugin do Struts, na mesma versão do Struts. Uma interface (`ModeloDeLinguagem`) esconde o provedor, e trocar de provedor é configuração. A chave fica só no `.env`.

## Limites conhecidos

- A validação e a gravação usam conexões separadas. Dois usuários ao mesmo tempo podem, em tese, deixar um compromisso fora do período da agenda. A correção é validar e gravar na mesma transação, com bloqueio da linha da agenda.
- A proteção contra envio forjado de formulário (CSRF) confere a origem do pedido, sem token por formulário. O passo seguinte é o interceptor `token` do Struts.
- O próprio usuário não troca a senha (só quem administra acessos), e não há bloqueio por tentativas de login.
- A análise da IA só conhece o que o médico anota e os resultados anteriores: o cadastro não tem laudos, valores de exame, idade nem cargo.
- Não há pool de conexões nem teste automatizado com banco.

## Documentação

- [docs/requisitos.md](docs/requisitos.md): as regras de negócio, a origem de cada uma e os testes que as cobrem.
- [docs/desenvolvimento-com-ia.md](docs/desenvolvimento-com-ia.md): como um assistente de IA foi usado no desenvolvimento, com requisitos numerados, roteiros, verificação automática e revisão independente.
