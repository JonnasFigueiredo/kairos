# Kairos

As instruções do projeto ficam no AGENTS.md, compartilhado com as outras ferramentas de IA. A linha abaixo o importa.

@AGENTS.md

## Específico do Claude Code

Estas peças automatizam o que o AGENTS.md descreve. Valem só no Claude Code.

| Peça | Arquivo | O que faz |
| --- | --- | --- |
| Skill | `.claude/skills/novo-modulo/` | Segue o passo "Criar um cadastro novo" do AGENTS.md |
| Agente | `.claude/agents/revisor-de-camadas.md` | Aplica o "Checklist de revisão" do AGENTS.md, só lendo |
| Agente | `.claude/agents/verificador-de-requisitos.md` | Confere cada regra de `docs/requisitos.md` contra o código, só lendo |
| Hook | `.claude/hooks/compilar-apos-edicao.sh` | Compila com JDK 8 depois de cada edição de `.java` e devolve o erro na hora |

O hook só roda depois de ligado no `.claude/settings.json` da máquina, junto com as permissões de comando. Esse arquivo é criado em cada máquina e não está no repositório.

Mudou uma convenção, um comando ou uma regra? Atualize o AGENTS.md, não este arquivo.
