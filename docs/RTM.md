# Matriz de Rastreabilidade

Esta matriz complementa o arquivo raiz `RTM.md` para o Commit 14; não o substitui. Os caminhos na coluna Evidência identificam testes ou documentação existentes, mas não significam aprovação de execução. Por solicitação do usuário, nenhum teste foi executado nesta finalização. Não há declaração de cobertura integral.

| Requisito | Risco | Teste e evidência localizável | Status nesta revisão |
|---|---|---|---|
| RF-01 Autenticação e autorização por perfil | Usuário não autenticado ou perfil incorreto acessar operações protegidas | `src/test/java/br/com/organizacaorecursos/integration/AuthIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/PublicApiWorkflowEndToEndTest.java` | Implementado; execução final pendente |
| RF-02 Cadastro e consulta de recursos | Dados inválidos persistidos ou consulta negada a perfis que devem ler | `src/test/java/br/com/organizacaorecursos/resource/ResourceValidatorTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java` | Implementado; execução final pendente |
| RF-03 Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Filtro incorreto retorna recursos incompatíveis ou indisponíveis | `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Implementado; execução final pendente |
| RF-04 Criar, alterar e cancelar reservas | Reserva alterada/cancelada incorretamente ou conflito ignorado | `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Implementado; execução final pendente |
| RF-05 Detecção de sobreposição | Intervalos sobrepostos ou recurso professor aceitos indevidamente | `src/test/java/br/com/organizacaorecursos/reservation/IntervalOverlapTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Implementado; execução final pendente |
| RF-06 Proteção contra dupla reserva | Concorrência permite mais de uma reserva aceita | `src/test/java/br/com/organizacaorecursos/integration/ReservationConcurrencyIntegrationTest.java` | Implementado; execução final pendente |
| RF-07 Aprovação de recurso restrito | Reserva restrita fica sem decisão ou é aprovada por perfil indevido | `src/test/java/br/com/organizacaorecursos/integration/ApprovalIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/PublicApiWorkflowEndToEndTest.java` | Implementado; execução final pendente |
| RF-08 Bloqueio por manutenção | Recurso em manutenção fica reservável ou intervalos adjacentes são bloqueados | `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ReservationIntegrationTest.java` | Teste direcionado observado antes dos últimos ajustes; reteste pendente |
| RF-09 Retirada e devolução | Retirada/devolução sem estado ou responsável válidos | `src/test/java/br/com/organizacaorecursos/integration/ApprovalIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/PublicApiWorkflowEndToEndTest.java` | Implementado; execução final pendente |
| RF-10 Histórico auditável | Mudança sem evento ou exposição de credenciais | `src/test/java/br/com/organizacaorecursos/integration/IdentityAuditIntegrationTest.java`; `src/test/java/br/com/organizacaorecursos/integration/ResourceMaintenanceIntegrationTest.java` | Implementado; execução final pendente |
| RF-11 Notificação simulada | Falha externa reverte a reserva ou notificação não é enviada | `src/test/java/br/com/organizacaorecursos/integration/ApprovalIntegrationTest.java` | Implementado com WireMock; execução final pendente |
| RF-12 Relatório de utilização | Horas, percentuais ou conflitos evitados calculados incorretamente | `src/test/java/br/com/organizacaorecursos/integration/UtilizationReportIntegrationTest.java` | Implementado; execução final pendente |
| RF-13 Interface responsiva e erros compreensíveis | Interface inacessível em telas menores ou erros incompreensíveis | Interface visual não encontrada; mensagens API em `src/main/java/br/com/organizacaorecursos/shared/api/GlobalExceptionHandler.java` | Pendente de interface/evidência visual; API não prova responsividade |
| RF-14 Documentação de API/fluxos | Contratos implementados divergem da documentação | `README.md`; `docs/quality/plano-de-testes.md`; OpenAPI em `/v3/api-docs` | Documentado; revisão/execução final pendente |

## Regras e qualidade relacionadas

| Regra/requisito | Relação | Evidência | Status nesta revisão |
|---|---|---|---|
| RN-01 | RF-04, RF-08: fim posterior ao início | `ResourceValidatorTest`, `ReservationIntegrationTest`, `ResourceMaintenanceIntegrationTest` | Execução final pendente |
| RN-02 | RF-05: intervalos semiabertos e sobreposição por recurso | `IntervalOverlapTest`, `ReservationIntegrationTest` | Execução final pendente |
| RN-03 | RF-06: uma reserva aceita em corrida concorrente | `ReservationConcurrencyIntegrationTest` | Execução final pendente |
| RN-04 | RF-08: manutenção impede reserva | `ResourceMaintenanceIntegrationTest`, `ReservationIntegrationTest` | Reteste pendente |
| RN-05 | RF-04, RF-07, RF-09: transições de estado | `ReservationStatusTransitionTest`, `ApprovalIntegrationTest` | Execução final pendente |
| RN-06 | RF-07: só responsável ou administrador decide | `ApprovalIntegrationTest` | Execução final pendente |
| RN-07 | RF-04: reserva iniciada não é excluída | `ReservationIntegrationTest` | Execução final pendente |
| RN-08 | RF-10: mudanças de estado geram auditoria | `IdentityAuditIntegrationTest`, `ApprovalIntegrationTest` | Execução final pendente |
| RNF-01 | Cobertura mínima JaCoCo | `pom.xml`, `.github/workflows/ci.yml` | Gate configurado; execução e limites não verificados nesta revisão |
| RNF-05 | Segurança e tratamento seguro de erros | `AuthIntegrationTest`, `IdentityAuditIntegrationTest`, `GlobalExceptionHandler` | Execução final pendente |
| RNF-06 | Testes com PostgreSQL/WireMock | `PostgresIntegrationTestSupport`, `ApprovalIntegrationTest` | Configurado; execução final pendente |
| RNF-07 | Evidência de TDD/BDD | Histórico de desenvolvimento | Pendente de evidência revisável |
| RNF-08 | Rastreabilidade | Esta matriz | Parcial; revisor humano pendente |

## Gate e revisão

- Gate JaCoCo configurado para 80% de linhas e 70% de branches; sem execução atual, não há resultado de cobertura a registrar.
- SonarCloud depende de configuração externa e não foi executado nesta revisão.
- RF-13 permanece pendente enquanto não houver interface verificável.
- Revisão humana da RTM: pendente. Revisor, data e resultado não foram inferidos.