---
name: novo-modulo
description: Cria um cadastro novo no Kairos (tabela, Vo, Dao, Business, Action, permissão, struts.xml, textos, JSPs e menu) seguindo o padrão do projeto. Use ao implementar qualquer CRUD novo.
---

# Criar módulo no Kairos

O passo a passo está na seção "Criar um cadastro novo" do `AGENTS.md`, que é a fonte única e vale para qualquer ferramenta. Siga-o na ordem, uma classe por vez, compilando a cada passo.

Ao terminar:

1. Rode `./mvnw -q test` e percorra o fluxo na tela: incluir, consultar, alterar, excluir e cada regra com um caso que passa e um que falha.
2. Peça os agentes `revisor-de-camadas` e `verificador-de-requisitos` e corrija os achados.
3. Se alguma regra mudou, atualize `docs/requisitos.md`.
