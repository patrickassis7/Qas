# Relatório final de qualidade

**Data de corte:** 2026-11-09

**Escopo:** evidências disponíveis para Organização de Recursos e estado de integração dos PRs #1 e #2.

## Estado de integração

Este relatório foi criado em `feat/docs-arquitetura-final`, a partir de `origin/develop` em `bb3241ccd6b28a0073a2ee0b44f4fb58a135a0aa`. As evidências mais recentes de frontend e robustez/carga pertencem a branches de feature ainda não integradas:

| Entrega | Branch e commit avaliados | Integração em `develop` |
|---|---|---|
| Frontend RF-13 — PR #1 | `feat/16-frontend-rf13`, `f37e95854e6964d86ec5606aa9ffc1a6fdc1a0e8` | PR aberto como Draft; frontend e alterações associadas não estão em `develop`. |
| Segurança, robustez e carga — PR #2 | `feat/15-load-security-robustness`, `3705037666bb39ad0346de7e26fe4955084d13d9` | PR aberto como Draft; correções, testes e evidências desta branch não estão em `develop`. |

As aprovações de CI abaixo se referem somente aos commits indicados. Não representam uma execução do workflow para esta branch de documentação nem um merge dos PRs.

## Testes automatizados e cobertura

Na revisão local registrada pela story 1.7 no head do PR #2, `.\mvnw.cmd --batch-mode --no-transfer-progress clean verify` terminou com **BUILD SUCCESS**: 202 testes, zero falhas, zero erros e zero ignorados. PostgreSQL/Testcontainers foi executado. Evidência: [story 1.7 no commit do PR #2](https://github.com/patrickassis7/Qas/blob/3705037666bb39ad0346de7e26fe4955084d13d9/docs/stories/1.7-commit-15-carga-seguranca-robustez.md).

JaCoCo registrou:

- Linhas: **1.074/1.147 — 93,64%**.
- Branches: **220/280 — 78,57%**.
- Os dois indicadores superaram os gates configurados de 80% de linhas e 70% de branches naquela revisão.

Esses números descrevem a revisão/branch do PR #2. `docs/quality/plano-de-testes.md` preserva os resultados 193 testes e 93,52%/77,27% como histórico do Commit 14 e agora aponta para esta revisão posterior; não use a referência antiga como resultado mais recente.

## Integração contínua

O GitHub Actions executou o workflow **CI** em eventos `pull_request`; o job `verify` terminou em sucesso para os dois heads consultados:

| PR | Commit avaliado | Resultado | Evidência remota |
|---|---|---|---|
| #1 — frontend | `f37e95854e6964d86ec5606aa9ffc1a6fdc1a0e8` | `CI` e `verify`: concluídos com sucesso em 2026-11-09 | [execução CI #42566300669](https://github.com/patrickassis7/Qas/actions/runs/42566300669) |
| #2 — segurança/carga | `3705037666bb39ad0346de7e26fe4955084d13d9` | `CI` e `verify`: concluídos com sucesso em 2026-11-09 | [execução CI #42575378609](https://github.com/patrickassis7/Qas/actions/runs/42575378609) |

Não foi feita execução de CI nesta branch de documentação durante a elaboração deste relatório.

## Carga e concorrência

O JMeter 5.6.3 foi executado manualmente em 2026-11-09 contra backend local no perfil `local` e PostgreSQL 16.15 isolado. O resultado foi registrado no head do PR #2; os agregados e hashes estão na [evidência JMeter versionada pelo PR #2](https://github.com/patrickassis7/Qas/blob/3705037666bb39ad0346de7e26fe4955084d13d9/docs/quality/evidencias/jmeter-commit15-2026-11-09.md).

- Total: **55.812 amostras HTTP**; todas receberam resposta.
- Pesquisa de recursos: 55.792 HTTP 200, zero erros; p50 4 ms, p95 7 ms, p99 9 ms e throughput de 933,99 req/s.
- Login: 12 respostas HTTP 200.
- Reservas simultâneas para o mesmo recurso/período: **uma HTTP 201 e sete HTTP 409 esperadas**.
- HTTP 500: **zero**. Falhas de transporte: **zero**.
- Integridade no PostgreSQL isolado: uma reserva aprovada vinculada ao recurso e **zero reservas órfãs**.

Os 409 são conflitos previstos pelo cenário; o JMeter os conta como amostras não 2xx, não como falha inesperada do sistema. Esta foi uma execução local única, sem SLO acordado e sem garantia de capacidade de produção.

## Segurança e defeitos

A story 1.7 e a RTM do PR #2 registram testes de autenticação, autorização por perfil, paginação inválida, concorrência e tratamento de erro na suíte de 202 testes. A execução de carga não observou HTTP 500 nem inconsistência de vínculos. Isso não substitui revisão de segurança independente ou análise estática remota.

## Pendências e limites

- **SonarCloud:** pendente; responsabilidade de outro integrante. Não há análise ou Quality Gate SonarCloud declarados neste relatório.
- **Responsividade RF-13:** frontend está no PR #1, mas não existe matriz completa de evidências visuais para 320, 375, 390, 768, 1024 e 1440 px. CI verde e testes de API não provam validação visual nesses tamanhos.
- **Integração:** aguardar a revisão/decisão dos PRs #1 e #2. Até então, os resultados associados às respectivas branches não devem ser apresentados como estado já incorporado em `develop`.
- **Reprodutibilidade da carga:** os JTL/HTML completos permanecem fora do Git; o repositório contém somente métricas agregadas e hashes sanitizados.

## Conclusão

As evidências disponíveis mostram suíte Maven aprovada com 202 testes e JaCoCo acima dos limites configurados na revisão do PR #2, CI `verify` verde nos heads consultados dos PRs #1 e #2, e uma execução JMeter com um aceite e sete conflitos esperados, sem HTTP 500 nem reserva órfã. A integração dos PRs, SonarCloud e a validação visual responsiva completa permanecem pendentes.
