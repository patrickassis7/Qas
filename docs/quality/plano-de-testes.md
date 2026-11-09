# Plano de Testes — Commit 14

## Objetivo e escopo

Demonstrar por evidência reproduzível os requisitos RF-01 a RF-14 e as regras RN-01 a RN-08, preservando os contratos do produto. O mapeamento requisito → risco → teste → evidência está em [`docs/RTM.md`](../RTM.md). Em 2026-11-09, `./mvnw --batch-mode --no-transfer-progress clean verify` passou com 193 testes, sem falhas, erros ou skips.

## Estratégia por camada

| Camada | Escopo | Ambiente/evidência |
|---|---|---|
| Unitário parametrizado | Limites de intervalo, transições, validação de recursos e configurações de autenticação | JUnit 5; `src/test/java/br/com/organizacaorecursos/reservation/`, `resource/` e `auth/` |
| Integração | Persistência, auditoria, concorrência, manutenção, aprovação, retirada/devolução e relatório | PostgreSQL real via Testcontainers; migrations Flyway; `src/test/java/br/com/organizacaorecursos/integration/` |
| Integração externa | Envio de aprovação/rejeição/cancelamento e resposta HTTP/timeout | WireMock; `ApprovalIntegrationTest` |
| API caixa-preta | Rotas HTTP, status, payload, autenticação/autorização, filtros e erros | MockMvc nos testes de integração; dados sintéticos e banco isolado por classe |
| Ponta a ponta | Login → pesquisar → reservar recurso restrito → aprovar → retirar/iniciar → devolver/concluir → consultar auditoria | `PublicApiWorkflowEndToEndTest`, `TestRestTemplate`, servidor HTTP aleatório e PostgreSQL Testcontainers |
| Interface visual | Responsividade e inspeção visual de mensagens | Pendente: não há interface estática verificável no escopo atual; não substituir por teste de API |

## Dados e isolamento

- Cada classe de integração usa PostgreSQL isolado por Testcontainers, migrações Flyway e limpeza do contexto após execução quando necessário.
- Identidades, reservas e recursos são sintéticos; senhas usadas nos testes não são credenciais de produção.
- WireMock deve ser reiniciado entre cenários; respostas 500 e timeout devem preservar a transição da reserva.
- Horários de reserva são instantes ISO-8601/UTC. Intervalos seguem `[início, fim)`.
- Não usar banco H2 ou mock de persistência para substituir PostgreSQL nos contratos de integração.

## Comandos e gates

Execução local completa:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

O `verify` gera JaCoCo XML/HTML em `target/site/jacoco/` e falha se a cobertura de produção ficar abaixo de 80% de linhas ou 70% de branches. O CI executa o mesmo comando. Para diagnóstico restrito, Maven Surefire aceita `-Dtest=NomeDaClasse`, mas esse resultado não substitui `clean verify`.

Resultado observado: 93,52% de linhas (1053/1126) e 77,27% de branches (204/264). O gate real passou. Os bloqueios individuais foram demonstrados sem repetir testes, usando os dados da execução recém-gerada:

```bash
./mvnw --batch-mode --no-transfer-progress jacoco:check@check-coverage -Djacoco.minimum.lines=0.94 -Djacoco.minimum.branches=0.70
./mvnw --batch-mode --no-transfer-progress jacoco:check@check-coverage -Djacoco.minimum.lines=0.80 -Djacoco.minimum.branches=0.78
```

O primeiro comando falhou por linhas (93,52% < 94%); o segundo falhou por branches (77,27% < 78%). Ambos retornaram `BUILD FAILURE` por `Coverage checks have not been met`.

SonarCloud só pode ser considerado executado quando `SONAR_TOKEN`, `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY` estiverem configurados e uma análise efetiva produzir evidência. A configuração ausente mantém o item pendente.

## Critérios de aceite e evidências

- Registrar para cada RF/RN na RTM o caminho do teste, comando/execução e resultado observável.
- Não marcar requisito como verificado pela mera existência de classe de teste.
- Confirmar ambos os contadores JaCoCo e demonstrar bloqueio abaixo do limite, além de uma execução verde acima dos limites, sem exclusões artificiais ou testes triviais.
- Verificar estados persistidos e ações de auditoria no fluxo E2E; provar que perfil não autorizado recebe negação.
- Registrar severidade, responsável, estado e reteste em `docs/quality/defects.md`.
- Manter RF-13, SonarCloud e revisão humana pendentes quando não houver evidência operacional.

## Limitações desta revisão

- A suíte completa e o E2E foram executados após os ajustes: 193/193 testes aprovados; `ResourceMaintenanceIntegrationTest` 2/2 e `PublicApiWorkflowEndToEndTest` 1/1.
- A cobertura passou os limites atuais. SonarCloud não foi executado porque as variáveis de configuração necessárias não estão presentes no ambiente local.
- Não há interface visual para verificar responsividade; RF-13 permanece pendente.
- Revisão humana da RTM não foi realizada e permanece pendente.