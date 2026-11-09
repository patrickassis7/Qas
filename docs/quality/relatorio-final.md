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

## Escopo e situação dos requisitos

Esta avaliação consolida evidências documentais e execuções identificadas nas revisões abaixo. Não é uma nova execução de testes nem um aceite global do produto. A [RTM no head do PR #2](https://github.com/patrickassis7/Qas/blob/3705037666bb39ad0346de7e26fe4955084d13d9/docs/RTM.md) é a referência detalhada de testes e requisitos; o quadro resume a situação conhecida em 2026-11-09.

| Requisito | Situação da evidência | Revisão/fonte principal |
|---|---|---|
| RF-01 Autenticação e autorização por perfil | Coberto na suíte do PR #2: login, rotas protegidas, negação por perfil e tentativas de escrita administrativa. | [Story 1.7 e RTM, PR #2](https://github.com/patrickassis7/Qas/blob/3705037666bb39ad0346de7e26fe4955084d13d9/docs/RTM.md) |
| RF-02 Cadastro e consulta de recursos | Validação e integração cobertas; no PR #2 também foram verificadas tentativas de escrita por perfis sem permissão. | RTM, PR #2 |
| RF-03 Pesquisa de recursos | Filtros e paginação cobertos; limites de página e tamanho inválidos são tratados como 400 padronizado na revisão PR #2. | RTM e Story 1.7, PR #2 |
| RF-04 Criar, alterar e cancelar reservas | Coberto por testes de integração e fluxo ponta a ponta na revisão PR #2. | RTM, PR #2 |
| RF-05 Detecção de sobreposição | Coberta por teste de intervalo e integração; a reserva repetida da carga também produziu conflitos esperados. | RTM e evidência JMeter, PR #2 |
| RF-06 Proteção contra dupla reserva | Teste concorrente automatizado e execução JMeter local: uma reserva aceita e sete conflitos esperados. A carga não define capacidade de produção. | [Evidência JMeter, PR #2](https://github.com/patrickassis7/Qas/blob/3705037666bb39ad0346de7e26fe4955084d13d9/docs/quality/evidencias/jmeter-commit15-2026-11-09.md) |
| RF-07 Aprovação de recurso restrito | Coberto por integração e fluxo ponta a ponta, incluindo autorização do responsável/administrador. | RTM, PR #2 |
| RF-08 Bloqueio por manutenção | Coberto por testes de integração; os resultados de carga não substituem esses testes funcionais. | RTM, PR #2 |
| RF-09 Retirada e devolução | Coberto por integração e fluxo ponta a ponta. | RTM, PR #2 |
| RF-10 Histórico auditável | Persistência e consulta administrativa cobertas por testes de integração; evidência não equivale a auditoria independente de segurança. | RTM, PR #2 |
| RF-11 Notificação simulada | Adaptador HTTP e falhas simuladas cobertos com WireMock na suíte Maven. | RTM, PR #2 |
| RF-12 Relatório de utilização | Cálculos cobertos por testes de integração; o endpoint também é protegido por perfil. | RTM, PR #2 |
| RF-13 Interface responsiva e mensagens compreensíveis | Implementação frontend está no PR #1 e o CI desse head passou. A matriz visual dos seis tamanhos ainda não está comprovada; requisito parcialmente evidenciado. | [PR #1](https://github.com/patrickassis7/Qas/pull/1); matriz visual pendente |
| RF-14 Documentação de API ou fluxos | README, OpenAPI, RTM e roteiro de demonstração existem; revisão de conteúdo não representa validação operacional de todos os fluxos. | Documentos na branch atual e RTM do PR #2 |

Os resultados de RF-01 a RF-12 acima são evidências associadas à revisão do PR #2, não à `develop` nem a esta branch documental. RF-13 permanece parcial; RF-14 está documentado, mas não é declarado integralmente validado por esta avaliação.

## Estratégia de testes observada

| Camada | Estratégia/evidência | Limite da conclusão |
|---|---|---|
| Unitária | JUnit 5 para intervalos, transições, validações de recursos e configurações/autenticação. | Uma classe aprovada prova apenas os casos que ela executa. |
| Integração | PostgreSQL real via Testcontainers, migrations Flyway, persistência, manutenção, auditoria, aprovação, notificações via WireMock e relatório. | Resultado atribuído ao `clean verify` do head PR #2. |
| API/caixa-preta | MockMvc/TestRestTemplate para status, contratos de erro, autorização, paginação e entradas inválidas. | Não substitui inspeção visual do navegador. |
| Ponta a ponta | `PublicApiWorkflowEndToEndTest` percorre login, pesquisa, reserva restrita, aprovação, retirada/devolução e consulta de auditoria. | É fluxo HTTP automatizado; não é ensaio de apresentação nem teste visual. |
| Concorrência/carga | `ReservationConcurrencyIntegrationTest` automatiza concorrência; JMeter 5.6.3 executou consultas e oito reservas simultâneas no ambiente local isolado. | Uma execução local caracteriza somente aquele ambiente e janela. |
| Interface visual | Frontend está no head do PR #1. | Não há evidência completa para 320, 375, 390, 768, 1024 e 1440 px. |

Fonte da estratégia e dos critérios: [plano de testes desta branch](plano-de-testes.md), [Story 1.7 no head do PR #2](https://github.com/patrickassis7/Qas/blob/3705037666bb39ad0346de7e26fe4955084d13d9/docs/stories/1.7-commit-15-carga-seguranca-robustez.md) e RTM citada acima.

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

O JMeter 5.6.3 foi executado manualmente em 2026-11-09, das 09:30:40.871 às 09:31:40.757 (UTC−03:00), contra backend local no perfil `local` e PostgreSQL 16.15 isolado. O resultado está registrado no head `3705037666bb39ad0346de7e26fe4955084d13d9` do PR #2. A [evidência versionada](https://github.com/patrickassis7/Qas/blob/3705037666bb39ad0346de7e26fe4955084d13d9/docs/quality/evidencias/jmeter-commit15-2026-11-09.md) registra JTL SHA-256 `C32B6B4F1D9EB2C84A923F7718EA37FBC76F7CCCBE1825A6B976D865B119EA2A` e agregados `statistics.json` SHA-256 `580C53AC4C7607299775FD74F21E0BCDB4F03C66029789C4EEDAC9973A26E149`.

- Total: **55.812 amostras HTTP**; todas receberam resposta.
- Pesquisa de recursos: 55.792 HTTP 200, zero erros; p50 4 ms, p90 6 ms, p95 7 ms, p99 9 ms, média 4,0937 ms e throughput de 933,9918 req/s.
- Login: 12 respostas HTTP 200.
- Reservas simultâneas para o mesmo recurso/período: **uma HTTP 201 e sete HTTP 409 esperadas**.
- HTTP 500: **zero**. Falhas de transporte: **zero**.
- Throughput total: 931,8929 req/s.
- Integridade no PostgreSQL isolado: uma reserva aprovada vinculada ao recurso e **zero reservas órfãs**.

Os 409 são conflitos previstos pelo cenário; o JMeter os conta como amostras não 2xx, não como falha inesperada do sistema. Esta foi uma execução local única, sem SLO acordado e sem garantia de capacidade de produção.

## Segurança, defeitos e riscos residuais

A story 1.7 e a RTM do PR #2 registram testes de autenticação, autorização por perfil, paginação inválida e contrato 400 uniforme, concorrência e tratamento de erro sem stack trace na suíte de 202 testes. A autorização foi exercitada nos endpoints reais de criação, alteração e desativação de recursos; SOLICITANTE e RESPONSAVEL receberam 403 sem efeitos persistidos. A carga não observou HTTP 500 nem inconsistência de vínculos. O [registro de defeitos no head do PR #2](https://github.com/patrickassis7/Qas/blob/3705037666bb39ad0346de7e26fe4955084d13d9/docs/quality/defects.md) registra DEF-008 como corrigido e retestado.

JaCoCo mede linhas e branches executados pela suíte; **não prova ausência de vulnerabilidades** nem substitui análise estática, revisão manual ou teste de penetração. O SonarCloud continua pendente e nenhum resultado de Quality Gate é afirmado.

## Pendências e limites

- **SonarCloud:** pendente; responsabilidade de outro integrante. Não há análise ou Quality Gate SonarCloud declarados neste relatório.
- **Responsividade RF-13:** frontend está no PR #1, mas não existe matriz completa de evidências visuais para 320, 375, 390, 768, 1024 e 1440 px. CI verde e testes de API não provam validação visual nesses tamanhos.
- **SLO de latência:** não acordado com o docente; percentis e vazão registrados são observações desta execução local, não metas aprovadas.
- **Integração:** aguardar a revisão/decisão dos PRs #1 e #2. Até então, os resultados associados às respectivas branches não devem ser apresentados como estado já incorporado em `develop`.
- **Reprodutibilidade da carga:** os JTL/HTML completos permanecem fora do Git; o repositório contém somente métricas agregadas e hashes sanitizados.
- **Riscos residuais:** a revisão de autorização e erros cobre os cenários listados na story/RTM, mas não é auditoria de segurança independente; a execução JMeter é única e local; a interface ainda não tem matriz visual completa; notificações são simuladas/configuráveis e sua entrega externa não foi demonstrada aqui. O registro de defeitos diferencia itens corrigidos dos riscos ainda abertos.

## Síntese — sem aprovação global

As evidências mostram suíte Maven aprovada com 202 testes e JaCoCo acima dos limites configurados na revisão do PR #2, CI `verify` verde nos heads consultados dos PRs #1 e #2, e uma execução JMeter com uma reserva aceita e sete conflitos esperados, sem HTTP 500 nem reserva órfã. Este relatório **não declara a qualidade final aprovada**: a integração dos PRs, SonarCloud, a matriz visual responsiva e o acordo de SLO permanecem pendentes.
