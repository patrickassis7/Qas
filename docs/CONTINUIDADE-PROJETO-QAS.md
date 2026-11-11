# Continuidade do projeto Organização de Recursos (Qas)

**Verificado em:** 9 de novembro de 2026

**Repositório:** [patrickassis7/Qas](https://github.com/patrickassis7/Qas)
**Remoto:** `origin` — `https://github.com/patrickassis7/Qas.git`

Este documento registra o estado observado nesta verificação para facilitar a transferência do projeto. Os hashes e estados remotos abaixo são um retrato da consulta feita nesta data; a próxima pessoa deve executar `git fetch origin` e `git status` antes de trabalhar.

## Estado Git verificado

- Checkout local: `feat/rf13-responsive-overflow`.
- HEAD local no início desta entrega documental: `94f1b6ea606170ece1f17084e1f07c78e5d217fa` (`docs(quality): registra validacao visual do RF-13`).
- A branch contém dois commits locais posteriores a `origin/develop`: `ded9437` e `94f1b6e`. Ambos são ancestrais de HEAD e não aparecem em nenhuma branch remota consultada.
- `origin/develop`: `8e5fb34b7faa9838626285fea29c7bcec7c42aa8`, incluindo os merges dos PRs #2 (segurança/carga) e #1 (frontend).
- `origin/feat/rf13-responsive-overflow` não existia na consulta remota. Portanto, os dois commits RF-13 ainda precisam ser publicados depois que o gate AIOX puder ser satisfeito legitimamente.
- `origin/feat/docs-arquitetura-final` existe em `8072615036d9f18d1ba22fc8e6a821c25cb07f35`. Ela contém 16 commits além de `origin/develop`, incluindo diagramas, ADRs, roteiro, relatório de qualidade, RTM e README. Essa branch foi preservada e não foi integrada nesta entrega.
- `git fetch origin` concluiu sem erro. Não foi feito merge, rebase, reset ou push nesta verificação.

## Commits RF-13 pendentes de publicação

| Commit | Mensagem | Conteúdo |
|---|---|---|
| `ded9437` | `fix(frontend): corrige overflow do login em telas estreitas` | Ajuste de CSS para evitar overflow no login em viewport estreita. |
| `94f1b6e` | `docs(quality): registra validacao visual do RF-13` | Atualização das evidências de validação responsiva. |

Não recriar esses commits nem criar commits vazios. Antes de tentar publicá-los, confirmar novamente a branch, os hashes, a existência do branch remoto e o estado do gate.

## Validação executada nesta transferência

Na revisão de código `94f1b6ea606170ece1f17084e1f07c78e5d217fa`, antes deste commit documental:

- `.\mvnw.cmd --batch-mode --no-transfer-progress clean verify`: **BUILD SUCCESS**; 202 testes, 0 falhas, 0 erros e 0 ignorados. Os relatórios Surefire foram contados em `target/surefire-reports`.
- Testcontainers iniciou PostgreSQL 16.15 e o Flyway validou/aplicou as seis migrations.
- JaCoCo gerado pelo Maven: **1.074/1.147 linhas cobertas (93,64%)** e **220/280 branches cobertos (78,57%)**.
- `node --check src/main/resources/static/app.js`: passou.
- Os cinco comandos npm definidos nos checks AIOX foram tentados (`npm run lint`, `npm run typecheck`, `npm test`, `npm run build` e `npm run validate:port-denylist`); todos terminaram com exit code 1 por ausência de `package.json`/scripts npm. São gates **não satisfeitos**, não validações aprovadas nem evidência de defeito Java.
- A CLI `coderabbit` não foi encontrada no `PATH` nesta sessão. A revisão exigida por essa ferramenta não foi executada.
- A story `docs/stories/1.6-commit-16-frontend-rf13.md` está com status `Review`; o checklist pre-push consultado exige status `Done` ou `Ready for Review`, então esse item também não está satisfeito.
- Nenhum Maven foi executado depois da criação deste documento, pois o único arquivo novo desta entrega é documentação. Os resultados acima correspondem ao SHA de código `94f1b6e`, explicitamente indicado.

A story RF-13 registra validação manual reportada pelo usuário no Chrome DevTools, nos tamanhos 320, 375, 390, 768, 1024 e 1440 px, incluindo a verificação de ausência de overflow. Não há capturas de tela arquivadas. Essa evidência é manual e reportada pelo usuário, não uma execução automatizada reproduzida nesta sessão. O smoke HTTP documentado na story também não foi repetido nesta transferência.

## Bloqueio de publicação: gate AIOX

A publicação dos dois commits RF-13 **não foi realizada**. Os artefatos AIOX vigentes tornam o gate de qualidade bloqueante antes do push:

- `.aiox-core/constitution.md` (Artigo V, versão 1.1.0) exige `npm run lint`, `npm run typecheck`, `npm test`, `npm run build` e ausência de achados CRITICAL do CodeRabbit; classifica o gate como BLOCK.
- `.aiox-core/development/tasks/github-devops-pre-push-quality-gate.md` também marca os checks aplicáveis, o scanner `validate:port-denylist` e CodeRabbit como obrigatórios e proíbe bypass.
- `.aiox-core/product/checklists/pre-push-checklist.md` exige reportar os itens e bloquear a publicação quando gates críticos não passam.
- Este projeto é Maven. Não há `package.json` nem script `lint`; o comando efetivamente tentado falhou. A proposta de perfil Maven está em revisão e **NÃO ESTÁ APROVADA nem implementada**.
- `coderabbit` não está instalado/disponível no ambiente desta sessão. Não se deve tratar ausência, erro, timeout ou revisão de outro SHA como aprovação.
- A inexistência de um hook Git `pre-push` não revoga a política AIOX. Não usar `--no-verify`, variáveis de skip, scripts fictícios ou qualquer outro bypass.

Próximo passo de governança: obter os pareceres e o consenso requeridos de `@architect` e `@po` sobre a proposta, cumprir qualquer autorização adicional da equipe exigida pela Constitution, criar/refinar a story de implementação e só então alterar a Constitution/gates conforme o processo aprovado. Depois da implementação, executar os testes de aceitação e todos os checks obrigatórios no SHA exato que será publicado. Até lá, manter `ded9437` e `94f1b6e` preservados localmente.

## Documentos preservados e estado de aprovação

Os documentos abaixo estavam presentes, mas não rastreados, no início da transferência. Foram inspecionados e não apresentaram correspondências para padrões de atribuição de segredo, chave privada, e-mail ou identificador pessoal usados nessa verificação. Essa checagem por padrões não substitui um scanner de segredos abrangente.

- `01-especificacao-e-documentacao.md` — especificação e requisitos do projeto.
- `02-roteiro-e-plano-de-commits.md` — plano acadêmico de entregas e commits.
- `docs/quality/proposta-adequacao-gate-aiox-maven.md` — proposta de adaptação AIOX/Maven. **Estado explícito: candidata para revisão; NÃO APROVADA e NÃO IMPLEMENTADA.** Pareceres favoráveis condicionados anteriores não constituem consenso formal.

Esses três documentos são material do projeto e serão incluídos no commit de handoff com caminhos explícitos. `.vscode/` é configuração local e permanecerá fora do commit. Nenhum `.env`, credencial, token, saída de build ou relatório temporário será adicionado.

## Documentação relacionada em outras revisões

A branch documental remota contém materiais que não estão presentes no checkout RF-13 baseado em `develop`. Consultar sem fazer merge automático:

- [RTM oficial](https://github.com/patrickassis7/Qas/blob/feat/docs-arquitetura-final/docs/RTM.md)
- [README documental](https://github.com/patrickassis7/Qas/blob/feat/docs-arquitetura-final/README.md)
- [ADRs](https://github.com/patrickassis7/Qas/tree/feat/docs-arquitetura-final/docs/adr)
- [Diagramas de arquitetura](https://github.com/patrickassis7/Qas/tree/feat/docs-arquitetura-final/docs/architecture)
- [Roteiro de demonstração](https://github.com/patrickassis7/Qas/blob/feat/docs-arquitetura-final/docs/apresentacao/roteiro-demo.md)
- [Relatório final de qualidade](https://github.com/patrickassis7/Qas/blob/feat/docs-arquitetura-final/docs/quality/relatorio-final.md)
- [Plano e evidências de carga](https://github.com/patrickassis7/Qas/tree/feat/docs-arquitetura-final/docs/quality)

Na branch `develop` consultada, estão disponíveis a [story RF-13](stories/1.6-commit-16-frontend-rf13.md), o [plano de carga](quality/plano-de-carga.md) e as [evidências agregadas de carga](quality/evidencias/). A branch documental precisa ser revisada e integrada por PR, resolvendo diferenças de README/RTM e preservando os commits de ambas as linhas de trabalho.

## Pendências para a próxima pessoa

1. Resolver formalmente a incompatibilidade Maven/AIOX. Não editar Constitution, gates, templates ou agentes antes da aprovação exigida; manter a proposta marcada como não aprovada.
2. Quando o gate estiver legitimamente satisfeito, publicar `ded9437` e `94f1b6e` em `origin/feat/rf13-responsive-overflow` sem force push e abrir/atualizar PR para `develop`.
3. Revisar a branch `feat/docs-arquitetura-final` e concluir sua integração por PR. Ela já existe remotamente com 16 commits exclusivos; não sobrescrever README/RTM nem assumir que esses materiais estão em `develop`.
4. Confirmar no GitHub os checks do SHA final; a execução local de Maven não comprova CI remoto.
5. SonarCloud continua delegado a outro integrante; não há validação SonarCloud registrada aqui.
6. Validar os documentos após a integração e atualizar a RTM/relatório com a revisão efetivamente integrada, sem atribuir a `develop` evidências de branches não integradas.

## Limites desta transferência

Esta entrega não altera código, configuração AIOX, banco, volumes Docker, SonarCloud ou branches remotas. O commit documental local organiza o handoff e preserva os documentos de requisitos e a proposta. A publicação remota permanece bloqueada até a resolução aprovada do gate; nenhum push foi confirmado nesta etapa.
