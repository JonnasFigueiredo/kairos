# Desenvolvimento com IA

Este projeto foi desenvolvido com assistentes de programação com IA. Este documento explica como as instruções para esses assistentes estão organizadas, para que qualquer pessoa que assuma o projeto continue com a ferramenta que preferir e obtenha o mesmo padrão de código.

## O problema que a organização resolve

Um assistente de IA não conhece o projeto. A cada sessão ele começa do zero e preenche as lacunas com suposições: escolhe uma biblioteca da moda, ignora uma regra de negócio, mistura camadas. Sem instruções escritas, o resultado depende de quem estava conduzindo a sessão e do que lembrou de dizer.

A solução é tratar as instruções para a IA como parte do código: escritas, versionadas, revisadas e mantidas junto com o projeto.

## Como está organizado

| Arquivo | Para quem | O que contém |
| --- | --- | --- |
| [AGENTS.md](../AGENTS.md) | Qualquer assistente de IA, e pessoas | Comandos, stack, estrutura, convenções, segurança, passo a passo de tarefas, checklist de revisão e armadilhas conhecidas |
| [docs/requisitos.md](requisitos.md) | Assistentes e pessoas | Regras de negócio numeradas (`RN-*`), com a origem de cada uma e o teste que a cobre |
| [CLAUDE.md](../CLAUDE.md) | Claude Code | Importa o AGENTS.md e lista as automações próprias da ferramenta |
| `.claude/` | Claude Code | Skill de cadastro novo, agentes de revisão e hook de compilação |
| `.githooks/pre-commit` | Git, para todos | Roda os testes antes de cada commit e recusa o commit se algum falhar |
| Este documento | Pessoas | Por que é assim e como manter |

O princípio é **uma fonte única com adaptadores finos**. Tudo o que vale para qualquer ferramenta fica no `AGENTS.md`. Cada ferramenta ganha, no máximo, um arquivo pequeno que aponta para ele e acrescenta o que só ela sabe fazer. Assim não há duas versões da mesma regra para ficarem diferentes.

A pasta `.claude/` mantém esse nome porque é onde o Claude Code procura suas automações. Ela é um adaptador, como `.github/` ou `.vscode/`: quem usa outra ferramenta pode ignorá-la sem perder nenhuma regra.

## Verificação que não depende da ferramenta

As instruções dizem o que fazer; a verificação garante que foi feito. A verificação essencial fica no git, não no assistente: o hook `.githooks/pre-commit` roda os testes antes de cada commit, seja qual for a ferramenta que escreveu o código, inclusive nenhuma. Ele é ativado uma vez por clone:

```bash
git config core.hooksPath .githooks
```

## Usando com outras ferramentas

O `AGENTS.md` é um formato aberto, adotado por várias ferramentas.

| Ferramenta | Como lê as instruções |
| --- | --- |
| Codex, Cursor, GitHub Copilot | Leem o `AGENTS.md` da raiz diretamente |
| Claude Code | Lê o `CLAUDE.md`, que importa o `AGENTS.md` com a linha `@AGENTS.md` |
| Gemini CLI | Configure o arquivo de contexto no `.gemini/settings.json`: `{"context": {"fileName": "AGENTS.md"}}` |
| Aider | Inclua o arquivo no `.aider.conf.yml`: `read: AGENTS.md` |
| Outras | Crie o arquivo que a ferramenta espera com uma linha apontando para o `AGENTS.md` |

Modelos como DeepSeek, GPT ou Gemini não leem arquivos sozinhos: eles rodam dentro de uma ferramenta (Cursor, Cline, Aider e outras), e é a ferramenta que carrega o `AGENTS.md`. Trocar de modelo não muda nada aqui; trocar de ferramenta, no máximo, pede uma linha de configuração.

O suporte de cada ferramenta muda com as versões. Confira a documentação dela antes de criar um arquivo novo.

## O que torna as instruções úteis

1. **Comandos exatos.** "Rode `./mvnw -q test`" funciona; "rode os testes" deixa a IA adivinhar.
2. **Limites explícitos.** A stack fixa e o que nunca fazer (trocar biblioteca, tocar no `.env`) evitam os erros mais caros.
3. **Regra antes do código.** As regras numeradas em `docs/requisitos.md` dão à IA o que implementar e dão a quem revisa o que conferir. O código da regra aparece no código, no teste e no commit, o que deixa tudo rastreável.
4. **Procedimentos para tarefas repetidas.** O passo a passo de cadastro novo faz todo módulo sair igual, seja qual for a ferramenta ou a pessoa.
5. **Checklist de revisão.** A IA revisa o próprio trabalho com os mesmos critérios de uma pessoa.
6. **Armadilhas conhecidas.** Cada erro que a IA cometeu e que custou tempo vira uma linha no `AGENTS.md`. É a parte que mais cresce e a que mais economiza nas sessões seguintes.
7. **Curto.** O arquivo é lido em toda sessão. O que é detalhe fica em `docs/` e é lido quando a tarefa pede.

## Ciclo de uma tarefa

1. A tarefa parte de uma regra em `docs/requisitos.md`. Regra nova é escrita antes do código.
2. A IA lê o `AGENTS.md` e o código ao redor, e implementa uma camada por vez.
3. Os testes rodam a cada mudança. No Claude Code, um hook compila depois de cada edição e devolve o erro na hora.
4. A IA revisa com o checklist do `AGENTS.md`. No Claude Code, agentes só de leitura fazem essa revisão com contexto limpo; em outra ferramenta, basta pedir a revisão pelo checklist.
5. A pessoa responsável lê o diff, testa na tela e faz o commit, citando as regras atendidas. O hook do git roda os testes de novo antes de aceitar o commit.

## Como manter

- Mudou uma convenção, um comando ou uma regra: atualize o `AGENTS.md` ou o `docs/requisitos.md` no mesmo commit.
- A IA errou de um jeito que vai se repetir: acrescente a armadilha no `AGENTS.md`.
- Trate mudanças nesses arquivos como mudança de código: revisão e commit próprio.
- Nada de segredo, dado pessoal ou configuração de máquina nesses arquivos. Eles vão para o repositório.

## Limites

- Os assistentes não têm acesso ao `.env` nem a credenciais.
- Os agentes de revisão só leem; não editam arquivos.
- Decisões de regra, de arquitetura e de escopo são da pessoa responsável pelo projeto, não da IA.
