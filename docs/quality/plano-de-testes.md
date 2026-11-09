# Plano de Testes — Commit 14

## Objetivo e escopo

Demonstrar por evidência reproduzível os requisitos RF-01 a RF-14 e as regras RN-01 a RN-08, preservando os contratos do produto. O mapeamento requisito → risco → teste → evidência está em [`docs/RTM.md`](../RTM.md). Esta revisão não executou testes, conforme solicitação do usuário; os resultados permanecem pendentes.

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

O `verify` deve gerar JaCoCo XML/HTML em `target/site/jacoco/` e falhar se a cobertura de produção ficar abaixo de 80% de linhas ou 70% de branches. O CI executa o mesmo comando. Para diagnóstico restrito, Maven Surefire aceita `-Dtest=NomeDaClasse`, mas esse resultado não substitui `clean verify`.

SonarCloud só pode ser considerado executado quando `SONAR_TOKEN`, `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY` estiverem configurados e uma análise efetiva produzir evidência. A configuração ausente mantém o item pendente.

## Critérios de aceite e evidências

- Registrar para cada RF/RN na RTM o caminho do teste, comando/execução e resultado observável.
- Não marcar requisito como verificado pela mera existência de classe de teste.
- Confirmar ambos os contadores JaCoCo e demonstrar bloqueio abaixo do limite, além de uma execução verde acima dos limites, sem exclusões artificiais ou testes triviais.
- Verificar estados persistidos e ações de auditoria no fluxo E2E; provar que perfil não autorizado recebe negação.
- Registrar severidade, responsável, estado e reteste em `docs/quality/defects.md`.
- Manter RF-13, SonarCloud e revisão humana pendentes quando não houver evidência operacional.

## Limitações desta revisão

- Nenhum teste foi executado nesta finalização por solicitação do usuário. O comando de gate, o E2E e os testes criados/alterados ainda precisam de execução após os ajustes de autorização e formatação.
- O último resultado disponível para `ResourceMaintenanceIntegrationTest` precede esses ajustes e não certifica o estado atual.
- Não há interface visual para verificar responsividade; RF-13 permanece pendente.
- SonarCloud e revisão humana da RTM não foram realizados.