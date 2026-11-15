# Requisitos

Fonte da verdade das regras. Cada regra tem um código, citado no código-fonte onde ela é aplicada, nos testes que a cobrem e nos commits.

Origem: **Prova** (enunciado do desafio) ou **Kairos** (decisão do projeto, onde o enunciado não define).

## Funcionário

| Código | Regra | Origem |
| --- | --- | --- |
| RN-FU-01 | Cadastro com inclusão, consulta, alteração e exclusão. | Prova, atividade 1 |
| RN-FU-02 | Campos obrigatórios: código e nome. O código é gerado pelo banco. | Prova, atividade 1 |
| RN-FU-03 | Excluir um funcionário exclui também os compromissos dele, na mesma transação. | Prova, atividade 4 |

## Agenda

| Código | Regra | Origem |
| --- | --- | --- |
| RN-AG-01 | Cadastro com inclusão, consulta, alteração e exclusão. | Prova, atividade 2 |
| RN-AG-02 | Campos obrigatórios: código, nome e período disponível. O código é gerado pelo banco. | Prova, atividade 2 |
| RN-AG-03 | Período disponível é escolhido em um `<select>` com as opções 1 Manhã, 2 Tarde, 3 Ambos. | Prova, atividade 2 |
| RN-AG-04 | Não é permitido excluir uma agenda que tem compromissos. | Prova, atividade 5 |
| RN-AG-05 | Não é permitido alterar o período de uma agenda se algum compromisso dela ficar fora do novo período. | Kairos |

## Compromisso

| Código | Regra | Origem |
| --- | --- | --- |
| RN-CO-01 | Cadastro com inclusão, consulta, alteração e exclusão. | Prova, atividade 3 |
| RN-CO-02 | Campos obrigatórios: funcionário, agenda, data e horário. | Prova, atividade 3 |
| RN-CO-03 | O funcionário e a agenda informados precisam existir. | Kairos |
| RN-CO-04 | O horário precisa estar dentro do período disponível da agenda. Vale na inclusão e na alteração. | Prova, atividade 6 |
| RN-CO-05 | Manhã: de 00:00 a 11:59. Tarde: de 12:00 a 23:59. Ambos: qualquer horário. | Kairos |

## Relatório

| Código | Regra | Origem |
| --- | --- | --- |
| RN-RE-01 | Lista os compromissos com data dentro do período informado, incluindo as duas datas. | Prova, atividade 7 |
| RN-RE-02 | Filtros obrigatórios: data inicial e data final. | Prova, atividade 7 |
| RN-RE-03 | A data inicial não pode ser maior que a final. | Kairos |
| RN-RE-04 | Colunas: código e nome do funcionário, código e nome da agenda, data e hora do compromisso. | Prova, atividade 7 |
| RN-RE-05 | Dois formatos, com os mesmos dados: HTML em tela e Excel (XLSX). | Prova, atividade 7 |

## Acesso

| Código | Regra | Origem |
| --- | --- | --- |
| RN-AC-01 | Toda tela exige usuário autenticado. Sem sessão, o pedido é levado à tela de login. | Kairos |
| RN-AC-02 | Login com usuário e senha, os dois obrigatórios. Usuário inexistente e senha errada recebem a mesma mensagem. | Kairos |
| RN-AC-03 | A senha é guardada só como resultado do PBKDF2 com sal, nunca em texto puro. | Kairos |
| RN-AC-04 | Sair encerra a sessão. | Kairos |

## Permissões por módulo

Cada usuário tem uma matriz de acessos: por módulo (Funcionários, Agendas, Compromissos, Relatório, Resultados, Atendimento, Assistente de IA, Usuários e acessos), as ações ver, incluir, alterar e excluir que o módulo aceita. A lista de módulos e ações é o enum `Modulo`.

| Código | Regra | Origem |
| --- | --- | --- |
| RN-PE-01 | Quem pode incluir, alterar ou excluir num módulo também pode ver; o sistema completa ao salvar. Permissão que não existe na matriz é recusada. | Kairos |
| RN-PE-02 | Toda Action informa seu módulo no `struts.xml`; o `AutorizacaoInterceptor` confere a ação exigida pelo método (listar = ver, novo = incluir, editar = alterar, salvar = incluir ou alterar, excluir = excluir). Sem permissão: página de acesso negado com HTTP 403. O menu esconde o que a pessoa não pode usar; quem barra é o servidor. | Kairos |
| RN-PE-03 | Ninguém desativa a própria conta nem tira de si mesmo a permissão de alterar acessos. | Kairos |
| RN-PE-04 | Sempre fica ao menos um usuário ativo que possa alterar acessos. | Kairos |
| RN-PE-05 | Permissão alterada vale no próximo login da pessoa. | Kairos |
| RN-PE-06 | Conta não se exclui, só se desativa; conta inativa não entra (mesma mensagem de senha errada). | Kairos |
| RN-PE-07 | Login (minúsculas, números, ponto, traço e sublinhado, até 60, único e imutável), nome e, na inclusão, senha (ao menos 6) são obrigatórios. Na alteração, senha em branco mantém a atual. | Kairos |

## Médico

| Código | Regra | Origem |
| --- | --- | --- |
| RN-ME-01 | Uma conta vira conta de médico com CRM (só números, até 10) e UF do CRM; especialidade é opcional (até 100). | Kairos |
| RN-ME-02 | Um CRM por UF. | Kairos |
| RN-ME-03 | Os módulos Atendimento e Assistente de IA só podem ser liberados para conta de médico, e o cadastro de médico é conferido a cada pedido, não só ao salvar. | Kairos |
| RN-ME-04 | Tirar os dados de médico volta a conta a ser comum. | Kairos |
| RN-ME-05 | O resultado liberado é assinado com nome, CRM e UF do médico, gravados no momento: não mudam se o cadastro mudar depois. | Kairos |

## Atendimento e resultado do exame

O compromisso continua compromisso: o resultado é à parte, um por compromisso, lançado pelo médico.

| Código | Regra | Origem |
| --- | --- | --- |
| RN-AT-01 | Só médico lança resultado (módulo Atendimento e cadastro de médico, conferidos a cada pedido). O médico vem da sessão. | Kairos |
| RN-AT-02 | O rascunho pode ficar incompleto. Para liberar: indicador (Normal, Alterado, Estável ou Agravamento, os do eSocial S-2220) e parecer. Achados e parecer até 10.000 caracteres. | Kairos |
| RN-AT-03 | Resultado liberado não se edita: para mudar, retifica. A retificação exige motivo (até 500); a versão anterior fica guardada com o motivo, quem retificou e quando, e o resultado vira a próxima versão, assinado de novo. | Kairos |
| RN-AT-04 | Compromisso com resultado não pode ser alterado. | Kairos |
| RN-AT-05 | Excluir o compromisso ou o funcionário apaga o resultado, as versões e as análises de IA, na mesma transação. | Kairos |
| RN-AT-06 | O rascunho só aparece no atendimento. O resultado liberado aparece, só leitura, para quem tem o módulo Resultados. | Kairos |
| RN-AT-07 | A fila do médico mostra os compromissos de hoje e até 50 compromissos anteriores sem resultado liberado, do mais novo para o mais antigo. | Kairos |

## Assistente de IA do médico

Apoio ao parecer: na tela do atendimento, a IA lê o compromisso atual e o histórico do trabalhador e devolve uma sugestão. Só ajuda; quem decide é o médico.

| Código | Regra | Origem |
| --- | --- | --- |
| RN-IA-01 | Só médico usa a IA (módulo Assistente de IA e cadastro de médico, conferidos a cada pedido). | Kairos |
| RN-IA-02 | O que sai para a IA é pseudonimizado: vão agenda, data, o que está no formulário (mesmo sem salvar) e o histórico de resultados. Não vai o nome do trabalhador; nos textos livres, nome, CPF e e-mail digitados viram `[trabalhador]`, `[cpf]` e `[e-mail]`. | Kairos |
| RN-IA-03 | A resposta vem em seções fixas: resumo, comparação com anteriores, pontos de atenção, sugestão de parecer, dados que faltam e limitações. Nada é salvo sozinho: "Usar como rascunho do parecer" copia a sugestão para o campo, que continua editável. Se o provedor fugir do formato, o texto aparece como veio. | Kairos |
| RN-IA-04 | Limite de análises por médico por dia (`KAIROS_IA_LIMITE_DIARIO`, padrão 30; as que falharam também contam). | Kairos |
| RN-IA-05 | Toda análise fica registrada em `analise_ia`: médico, compromisso, provedor, contexto enviado, resposta ou erro, tokens, duração e hora. Se a IA não responder, a tela recebe HTTP 503 e uma mensagem para seguir sem ela. | Kairos |
| RN-IA-06 | Cada provedor tem a sua chave (`KAIROS_IA_CHAVE_CLAUDE`, `_OPENAI`, `_GEMINI`, `_DEEPSEEK`) e `KAIROS_IA_PROVEDOR` diz a ordem de tentativa (ex.: `claude,gemini,deepseek,openai`); se um falhar, tenta o próximo. Provedor da lista sem chave é pulado. Sem provedor configurado, o painel não aparece. Provedores: Claude (API Messages), OpenAI, Gemini, DeepSeek e Ollama (API compatível com OpenAI) e um falso para demonstração. Tudo em HTTP puro (`HttpURLConnection`) e JSON do plugin do Struts. | Kairos |

## Testes automatizados

Rodam sem banco, com `./mvnw test`.

| Regra | Classe de teste |
| --- | --- |
| RN-FU-02 | `FuncionarioBusinessTest` |
| RN-AG-02, RN-AG-03 | `AgendaBusinessTest`, `PeriodoTest` |
| RN-CO-02, RN-CO-04 | `CompromissoBusinessTest` |
| RN-CO-05 | `PeriodoTest`, `ConversoresTest` |
| RN-RE-02, RN-RE-03 | `RelatorioBusinessTest` |
| RN-RE-04 (Excel) | `PlanilhaDeCompromissosTest` |
| RN-AC-01 | `AutenticacaoInterceptorTest` |
| RN-AC-02 | `AutenticacaoBusinessTest` |
| RN-AC-03 | `SenhaTest` |
| RN-PE-01, RN-PE-02, RN-ME-03 | `AutorizacaoInterceptorTest` |
| RN-PE-01, RN-PE-03, RN-PE-07, RN-ME-01, RN-ME-03 | `UsuarioBusinessTest` |
| RN-AT-01, RN-AT-02, RN-AT-03, RN-ME-05 | `AtendimentoBusinessTest` |
| RN-IA-02 | `MontadorDeContextoTest` |
| RN-IA-03, RN-IA-06 | `AnaliseIaBusinessTest`, `AdaptadoresTest` |

## Além do enunciado

O login (RN-AC-01 a RN-AC-04), as permissões por módulo (RN-PE), o cadastro de médico (RN-ME), o atendimento com resultado do exame (RN-AT) e o assistente de IA (RN-IA) não são pedidos pelo enunciado. São um acréscimo sobre o mesmo esqueleto: as mesmas camadas, o mesmo Struts, o mesmo JDBC.

A consulta de compromissos mostra também um calendário do mês, com os mesmos registros da tabela. É só outra forma de ver a consulta (RN-CO-01): não cria regra nova. A montagem da grade tem teste em `CalendarioMensalTest`.

## Fora do escopo

O enunciado não pede e o projeto não faz, por enquanto: conflito de horário entre compromissos, web service SOAP, troca de senha pelo próprio usuário, bloqueio por tentativas de login.
