# Matriz de Rastreabilidade

Esta matriz complementa o arquivo raiz `RTM.md` para o Commit 14; não o substitui. Em 2026-11-09, `./mvnw --batch-mode --no-transfer-progress clean verify` passou com 193 testes, zero falhas/erros/ignorados. JaCoCo mediu 93,52% de linhas (1053/1126) e 77,27% de branches (204/264). Isso comprova o gate de cobertura, mas não declara cobertura funcional de 100%; RF-13 visual e SonarCloud seguem pendentes. A revisão humana da RTM foi aprovada por `@patrickassis7` em 2026-11-09.

| Requisito | Risco | Teste e evidência localizável | Status nesta revisão |
|---|---|---|---|
| RF-01 Autenticação e autorização por perfil | Usuário não autenticado ou perfil incorreto acessar operações protegidas | `src/test/java/br/com/organizacaorecursos/integration/AuthIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/PublicApiWorkflowEndToEndTest.java` | Suíte aprovada em clean verify (193 testes); ver escopo dos testes vinculados |
| RF-02 Cadastro e consulta de recursos | Dados inválidos persistidos ou consulta negada a perfis que devem ler | `src/test/java/br/com/organizacaorecursos/resource/ResourceValidatorTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java` | Suíte aprovada; validador 9/9 e integração 2/2 |
| RF-03 Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Filtro incorreto retorna recursos incompatíveis ou indisponíveis; paginação excessiva pode degradar a API | `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Pesquisa e validação dos limites de página (1–100) aprovadas; `clean verify` da story 1.7 pendente |
| RF-04 Criar, alterar e cancelar reservas | Reserva alterada/cancelada incorretamente ou conflito ignorado | `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Suíte aprovada em clean verify |
| RF-05 Detecção de sobreposição | Intervalos sobrepostos ou recurso professor aceitos indevidamente | `src/test/java/br/com/organizacaorecursos/reservation/IntervalOverlapTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Suíte aprovada em clean verify |
| RF-06 Proteção contra dupla reserva | Concorrência permite mais de uma reserva aceita | `src/test/java/br/com/organizacaorecursos/integration/ReservationConcurrencyIntegrationTest.java` | Suíte aprovada em clean verify |
| RF-07 Aprovação de recurso restrito | Reserva restrita fica sem decisão ou é aprovada por perfil indevido | `src/test/java/br/com/organizacaorecursos/integration/ApprovalIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/PublicApiWorkflowEndToEndTest.java` | Suíte aprovada; E2E 1/1 |
| RF-08 Bloqueio por manutenção | Recurso em manutenção fica reservável ou intervalos adjacentes são bloqueados | `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Retestado; integração de manutenção 2/2 e suíte 193/193 |
| RF-09 Retirada e devolução | Retirada/devolução sem estado ou responsável válidos | `src/test/java/br/com/organizacaorecursos/integration/ApprovalIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/PublicApiWorkflowEndToEndTest.java` | Suíte aprovada; E2E 1/1 |
| RF-10 Histórico auditável | Mudança sem evento ou exposição de credenciais | `src/test/java/br/com/organizacaorecursos/integration/IdentityAuditIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java` | Suíte aprovada em clean verify |
| RF-11 Notificação simulada | Falha externa reverte a reserva ou notificação não é enviada | `src/test/java/br/com/organizacaorecursos/integration/ApprovalIntegrationTest.java` | Suíte aprovada com WireMock em clean verify |
| RF-12 Relatório de utilização | Horas, percentuais ou conflitos evitados calculados incorretamente | `src/test/java/br/com/organizacaorecursos/integration/UtilizationReportIntegrationTest.java` | Suíte aprovada em clean verify |
| RF-13 Interface responsiva e erros compreensíveis | Interface inacessível em telas menores ou erros incompreensíveis | Interface visual não encontrada; mensagens API em `src/main/java/br/com/organizacaorecursos/shared/api/GlobalExceptionHandler.java` | Pendente de interface/evidência visual; API não prova responsividade |
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
| RNF-01 | Cobertura mínima JaCoCo | `pom.xml`, `.github/workflows/ci.yml`, `target/site/jacoco/jacoco.csv` | 93,52% linhas e 77,27% branches; gate 80%/70% aprovado; falhas isoladas comprovadas |
| RNF-05 | Segurança e tratamento seguro de erros | `AuthIntegrationTest`, `IdentityAuditIntegrationTest`, `ReservationIntegrationTest`, `GlobalExceptionHandler` | Autorização existente preservada; erros de paginação inválida são 400 uniformes sem stack trace; headers `nosniff`/`DENY` verificados; clean verify da story 1.7 pendente |
| RNF-06 | Testes com PostgreSQL/WireMock | `PostgresIntegrationTestSupport`, `ApprovalIntegrationTest` | Suíte aprovada com Testcontainers/WireMock |
| RNF-07 | Evidência de TDD/BDD | Histórico de desenvolvimento | Pendente de evidência revisável |
| RNF-08 | Rastreabilidade | Esta matriz | Revisão humana aprovada por `@patrickassis7` em 2026-11-09; rastreabilidade funcional parcial conforme evidências por RF |

## Gate e revisão

- Gate JaCoCo configurado para 80% de linhas e 70% de branches; clean verify passou com 93,52%/77,27%.
- Bloqueio de linhas demonstrado com mínimo 94% (93,52% observado): `jacoco:check@check-coverage -Djacoco.minimum.lines=0.94 -Djacoco.minimum.branches=0.70` falhou.
- Bloqueio de branches demonstrado com mínimo 78% (77,27% observado): `jacoco:check@check-coverage -Djacoco.minimum.lines=0.80 -Djacoco.minimum.branches=0.78` falhou.
- Clean verify executou 193 testes: 193 passaram, 0 falhas, 0 erros, 0 ignorados.
- SonarCloud depende de configuração externa e não foi executado nesta revisão.
- RF-13 permanece pendente enquanto não houver interface verificável.
- Revisão humana da RTM: aprovada por `@patrickassis7` em 2026-11-09, confirmação explícita registrada nesta sessão.
