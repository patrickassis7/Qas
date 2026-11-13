# Matriz de Rastreabilidade

Esta matriz complementa o arquivo raiz `RTM.md`; não o substitui. Em 2026-11-09, `./mvnw.cmd --batch-mode --no-transfer-progress clean verify` passou com 202 testes, zero falhas/erros/ignorados. JaCoCo mediu 93,64% de linhas (1074/1147) e 78,57% de branches (220/280). Isso comprova o gate de cobertura, mas não declara cobertura funcional de 100%. A evidência visual manual reportada para RF-13 está descrita na story 1.6; SonarCloud segue pendente. A carga JMeter real está registrada em `docs/quality/plano-de-carga.md`. A revisão humana da RTM foi aprovada por `@patrickassis7` em 2026-11-09.

| Requisito | Risco | Teste e evidência localizável | Status nesta revisão |
|---|---|---|---|
| RF-01 Autenticação e autorização por perfil | Usuário não autenticado ou perfil incorreto acessar operações protegidas | `src/test/java/br/com/organizacaorecursos/integration/AuthIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/PublicApiWorkflowEndToEndTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java` | Suíte aprovada em clean verify (202 testes); solicitante e responsável recebem 403 padronizado ao tentar operações administrativas reais |
| RF-02 Cadastro e consulta de recursos | Dados inválidos persistidos ou consulta negada a perfis que devem ler | `src/test/java/br/com/organizacaorecursos/resource/ResourceValidatorTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java` | Suíte aprovada; validador 9/9 e integração 3/3, incluindo bloqueio de escrita por perfil |
| RF-03 Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Filtro incorreto retorna recursos incompatíveis ou indisponíveis; paginação excessiva pode degradar a API | `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Pesquisa e limites de página (1–100) aprovados; suíte 202/202 |
| RF-04 Criar, alterar e cancelar reservas | Reserva alterada/cancelada incorretamente ou conflito ignorado | `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Suíte aprovada em clean verify |
| RF-05 Detecção de sobreposição | Intervalos sobrepostos ou recurso professor aceitos indevidamente | `src/test/java/br/com/organizacaorecursos/reservation/IntervalOverlapTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Suíte aprovada em clean verify |
| RF-06 Proteção contra dupla reserva | Concorrência permite mais de uma reserva aceita | `src/test/java/br/com/organizacaorecursos/integration/ReservationConcurrencyIntegrationTest.java`; plano JMeter e resultados agregados em `docs/quality/plano-de-carga.md` | Teste concorrente automatizado passou; carga real observou 1 HTTP 201, 7 HTTP 409 esperados, zero 500 e zero reservas órfãs; execução local não representa SLO de produção |
| RF-07 Aprovação de recurso restrito | Reserva restrita fica sem decisão ou é aprovada por perfil indevido | `src/test/java/br/com/organizacaorecursos/integration/ApprovalIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/PublicApiWorkflowEndToEndTest.java` | Suíte aprovada; E2E 1/1 |
| RF-08 Bloqueio por manutenção | Recurso em manutenção fica reservável ou intervalos adjacentes são bloqueados | `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Retestado; integração de manutenção 3/3 e suíte 202/202 |
| RF-09 Retirada e devolução | Retirada/devolução sem estado ou responsável válidos | `src/test/java/br/com/organizacaorecursos/integration/ApprovalIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/PublicApiWorkflowEndToEndTest.java` | Suíte aprovada; E2E 1/1 |
| RF-10 Histórico auditável | Mudança sem evento ou exposição de credenciais | `src/test/java/br/com/organizacaorecursos/integration/IdentityAuditIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java` | Suíte aprovada em clean verify |
| RF-11 Notificação simulada | Falha externa reverte a reserva ou notificação não é enviada | `src/test/java/br/com/organizacaorecursos/integration/ApprovalIntegrationTest.java` | Suíte aprovada com WireMock em clean verify |
| RF-12 Relatório de utilização | Horas, percentuais ou conflitos evitados calculados incorretamente | `src/test/java/br/com/organizacaorecursos/integration/UtilizationReportIntegrationTest.java` | Suíte aprovada em clean verify |
| RF-13 Interface responsiva e erros compreensíveis | Interface inacessível em telas menores ou erros incompreensíveis | Validação visual manual reportada pelo usuário e registrada em [story 1.6](stories/1.6-commit-16-frontend-rf13.md); mensagens API em `src/main/java/br/com/organizacaorecursos/shared/api/GlobalExceptionHandler.java` | Evidência manual reportada: telas verificadas nas seis larguras; sem capturas preservadas. Testes automatizados e mensagens API são evidência complementar, não substituem a inspeção visual. |
| RF-14 Documentação de API/fluxos | Contratos implementados divergem da documentação | `README.md`; `docs/quality/plano-de-testes.md`; OpenAPI em `/v3/api-docs` | Documentado e suíte aprovada; revisão humana da RTM aprovada em 2026-11-09 |

## Regras e qualidade relacionadas

| Regra/requisito | Relação | Evidência | Status nesta revisão |
|---|---|---|---|
| RN-01 | RF-04, RF-08: fim posterior ao início | `ResourceValidatorTest`, `ReservationIntegrationTest`, `ResourceMaintenanceIntegrationTest` | Suíte aprovada em clean verify |
| RN-02 | RF-05: intervalos semiabertos e sobreposição por recurso | `IntervalOverlapTest`, `ReservationIntegrationTest` | Suíte aprovada em clean verify |
| RN-03 | RF-06: uma reserva aceita em corrida concorrente | `ReservationConcurrencyIntegrationTest` | Suíte aprovada em clean verify |
| RN-04 | RF-08: manutenção impede reserva | `ResourceMaintenanceIntegrationTest`, `ReservationIntegrationTest` | Retestado; suíte aprovada |
| RN-05 | RF-04, RF-07, RF-09: transições de estado | `ReservationStatusTransitionTest`, `ApprovalIntegrationTest` | Suíte aprovada em clean verify |
| RN-06 | RF-07: só responsável ou administrador decide | `ApprovalIntegrationTest` | Suíte aprovada em clean verify |
| RN-07 | RF-04: reserva iniciada não é excluída | `ReservationIntegrationTest` | Suíte aprovada em clean verify |
| RN-08 | RF-10: mudanças de estado geram auditoria | `IdentityAuditIntegrationTest`, `ApprovalIntegrationTest` | Suíte aprovada em clean verify |
| RNF-01 | Cobertura mínima JaCoCo | `pom.xml`, `.github/workflows/ci.yml`, `target/site/jacoco/jacoco.csv` | 93,64% linhas e 78,57% branches; gate 80%/70% aprovado; falhas isoladas comprovadas |
| RNF-05 | Segurança e tratamento seguro de erros | `AuthIntegrationTest`, `IdentityAuditIntegrationTest`, `ReservationIntegrationTest`, `ResourceMaintenanceIntegrationTest`, `GlobalExceptionHandler` | Autorização por perfil preservada e ampliada nos endpoints reais de recursos; erros de paginação inválida são 400 uniformes sem stack trace; headers `nosniff`/`DENY` verificados; suíte completa 202/202 |
| RNF-06 | Testes com PostgreSQL/WireMock | `PostgresIntegrationTestSupport`, `ApprovalIntegrationTest`; schema exclusivo por classe e rejeição de datasource remoto | Migração para PostgreSQL 16 nativo implementada e validada localmente (220/220 testes); execução do workflow GitHub e probe Supabase manual ainda pendentes |
| RNF-07 | Evidência de TDD/BDD | Histórico de desenvolvimento | Pendente de evidência revisável |
| RNF-08 | Rastreabilidade | Esta matriz | Revisão humana aprovada por `@patrickassis7` em 2026-11-09; rastreabilidade funcional parcial conforme evidências por RF |

## Gate e revisão

- Gate JaCoCo configurado para 80% de linhas e 70% de branches; clean verify passou com 93,64%/78,57%.
- Bloqueio de linhas demonstrado com mínimo 94% (93,52% observado): `jacoco:check@check-coverage -Djacoco.minimum.lines=0.94 -Djacoco.minimum.branches=0.70` falhou.
- Bloqueio de branches demonstrado com mínimo 78% (77,27% observado): `jacoco:check@check-coverage -Djacoco.minimum.lines=0.80 -Djacoco.minimum.branches=0.78` falhou.
- Clean verify em 2026-11-09 executou 202 testes: 202 passaram, 0 falhas, 0 erros, 0 ignorados.
- JMeter 5.6.3: carga local real concluída; 55.792 pesquisas HTTP 200, p50 4 ms, p95 7 ms, p99 9 ms e throughput 933,99 req/s. Colisão resultou em um 201, sete 409 esperados, zero 500 e nenhuma reserva órfã. Evidência agregada em `docs/quality/plano-de-carga.md`; resultados brutos permanecem fora do Git.
- SonarCloud depende de configuração externa e não foi executado nesta revisão.
- RF-13 tem validação visual manual reportada nesta revisão; a evidência e sua limitação (sem capturas preservadas) estão na story 1.6.
- Revisão humana da RTM: aprovada por `@patrickassis7` em 2026-11-09, confirmação explícita registrada nesta sessão.
