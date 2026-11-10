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

## Além do enunciado

A consulta de compromissos mostra também um calendário do mês, com os mesmos registros da tabela. É só outra forma de ver a consulta (RN-CO-01): não cria regra nova. A montagem da grade tem teste em `CalendarioMensalTest`.

## Fora do escopo

O enunciado não pede e o projeto não faz, por enquanto: login, conflito de horário entre compromissos, web service SOAP.
