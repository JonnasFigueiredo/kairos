# Desenvolvimento com IA

Este projeto foi construído com um assistente de IA (Claude Code) como par de programação. Este documento mostra como o uso foi organizado para que o resultado seja previsível, revisável e de autoria de quem assina.

## Princípios

1. **A regra vem antes do código.** `docs/requisitos.md` lista cada regra com um código `RN-*`. A IA implementa a partir dele, não de memória.
2. **Contexto fixo e curto.** `CLAUDE.md` carrega em toda sessão: como rodar, stack, estrutura e convenções. O que é detalhe fica em `docs/` e é lido sob demanda.
3. **Stack travada.** A IA não troca versão nem adiciona biblioteca sem autorização do autor.
4. **Verificação automática.** Um hook compila o projeto a cada arquivo Java editado. Erro volta para a IA na hora.
5. **Revisão independente.** Agentes só de leitura revisam o que foi escrito, com um contexto limpo, sem o viés de quem escreveu.
6. **O autor decide.** Decisões de regra, de arquitetura e de escopo são humanas e ficam registradas. O autor lê, executa e commita cada etapa.

## Peças

| Peça | Arquivo | Para que serve |
| --- | --- | --- |
| Memória do projeto | `CLAUDE.md` | Instruções lidas em toda sessão |
| Requisitos | `docs/requisitos.md` | Regras `RN-*` com origem |
| Skill | `.claude/skills/novo-modulo/` | Passo a passo de um cadastro novo nas quatro camadas |
| Agente | `.claude/agents/revisor-de-camadas.md` | Revisa o respeito às camadas, o uso de JDBC e o tratamento de erro |
| Agente | `.claude/agents/verificador-de-requisitos.md` | Confere cada `RN-*` contra o código e aponta o que falta |
| Hook | `.claude/hooks/compilar-apos-edicao.sh` | Compila com Java 8 depois de cada edição em `.java` |
| Permissões | `.claude/settings.json` | Libera os comandos de rotina e bloqueia a leitura do `.env` |

## Skill, agente e hook: a diferença

- **Skill** é um procedimento. A IA carrega quando a tarefa pede e segue os passos. Garante que todo cadastro saia no mesmo padrão.
- **Agente** é um revisor com contexto próprio e ferramentas restritas (só leitura). Recebe uma pergunta, devolve um relatório.
- **Hook** é um comando que o próprio ambiente executa, sem depender da IA lembrar. Serve para o que tem de acontecer sempre.

## Ciclo de uma etapa

1. Ler as regras `RN-*` da etapa em `docs/requisitos.md`.
2. Seguir a skill `novo-modulo`, uma classe por vez.
3. O hook compila a cada edição.
4. Rodar e testar o fluxo na tela.
5. Pedir os dois agentes de revisão e corrigir os achados.
6. Autor revisa o diff e commita, citando as `RN-*` atendidas.

## Limites

- A IA não tem acesso ao `.env` nem a credenciais.
- Agentes de revisão não editam arquivos.
- O enunciado da prova não está no repositório.
