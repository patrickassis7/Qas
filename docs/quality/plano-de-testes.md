# Plano de Testes — estratégia e evidências

## Objetivo e escopo

Demonstrar por evidência reproduzível os requisitos RF-01 a RF-14 e as regras RN-01 a RN-08, preservando os contratos do produto. O mapeamento requisito → risco → teste → evidência está na [RTM oficial](../RTM.md). A execução de 193 testes descrita adiante é referência histórica do Commit 14. A revisão do PR #2 (head `3705037`) registrou 202 testes e JaCoCo 93,64% linhas/78,57% branches; PR #2 foi integrado por `87903e1`, e PR #1 por `8e5fb34`. Após a integração, `clean verify` foi executado novamente na branch documental em `d4b02bb`, com 202 testes, 0 falhas, 0 erros, 0 ignorados e os mesmos percentuais de JaCoCo. Consulte o [relatório final de qualidade](relatorio-final.md), a [story 1.7](../stories/1.7-commit-15-carga-seguranca-robustez.md) e a [matriz oficial](../RTM.md).

## Estratégia por camada

| Camada | Escopo | Ambiente/evidência |
|---|---|---|
| Unitário parametrizado | Limites de intervalo, transições, validação de recursos e configurações de autenticação | JUnit 5; `src/test/java/br/com/organizacaorecursos/reservation/`, `resource/` e `auth/` |
| Integração | Persistência, auditoria, concorrência, manutenção, aprovação, retirada/devolução e relatório | PostgreSQL real via Testcontainers; migrations Flyway; `src/test/java/br/com/organizacaorecursos/integration/` |
| Integração externa | Envio de aprovação/rejeição/cancelamento e resposta HTTP/timeout | WireMock; `ApprovalIntegrationTest` |
| API caixa-preta | Rotas HTTP, status, payload, autenticação/autorização, filtros e erros | MockMvc nos testes de integração; dados sintéticos e banco isolado por classe |
| Ponta a ponta | Login → pesquisar → reservar recurso restrito → aprovar → retirar/iniciar → devolver/concluir → consultar auditoria | `PublicApiWorkflowEndToEndTest`, `TestRestTemplate`, servidor HTTP aleatório e PostgreSQL Testcontainers |
| Interface visual | Responsividade e inspeção visual de mensagens | O frontend foi integrado em `develop` pelo merge `8e5fb34`; a matriz visual completa de 320, 375, 390, 768, 1024 e 1440 px continua pendente. Não substituir por teste de API. |

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

Referência histórica do Commit 14: 93,52% de linhas (1053/1126) e 77,27% de branches (204/264); o gate 80%/70% passou naquela execução. Os bloqueios individuais abaixo foram demonstrados com os dados daquela execução, sem repetir testes:

```bash
./mvnw --batch-mode --no-transfer-progress jacoco:check@check-coverage -Djacoco.minimum.lines=0.94 -Djacoco.minimum.branches=0.70
./mvnw --batch-mode --no-transfer-progress jacoco:check@check-coverage -Djacoco.minimum.lines=0.80 -Djacoco.minimum.branches=0.78
```

Na execução histórica, o primeiro comando falhou por linhas (93,52% < 94%) e o segundo por branches (77,27% < 78%). Ambos retornaram `BUILD FAILURE` por `Coverage checks have not been met`. Esses valores não são a cobertura mais recente.

SonarCloud só pode ser considerado executado quando `SONAR_TOKEN`, `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY` estiverem configurados e uma análise efetiva produzir evidência. A configuração ausente mantém o item pendente.

## Critérios de aceite e evidências

- Registrar para cada RF/RN na RTM o caminho do teste, comando/execução e resultado observável.
- Não marcar requisito como verificado pela mera existência de classe de teste.
- Confirmar ambos os contadores JaCoCo e demonstrar bloqueio abaixo do limite, além de uma execução verde acima dos limites, sem exclusões artificiais ou testes triviais.
- Verificar estados persistidos e ações de auditoria no fluxo E2E; provar que perfil não autorizado recebe negação.
- Registrar severidade, responsável, estado e reteste em `docs/quality/defects.md`.
- Manter a validação visual responsiva de RF-13 e SonarCloud pendentes até existir evidência operacional. A revisão humana da RTM está registrada em `docs/RTM.md`; ela não comprova, por si só, cobertura completa dos requisitos.

## Limitações desta revisão

- Histórico do Commit 14: 193/193 testes aprovados, com 93,52% de linhas e 77,27% de branches. Esses dados permanecem apenas como referência daquela revisão.
- Evidência da revisão do PR #2: head `3705037`, 202/202 testes e JaCoCo 93,64%/78,57%, documentados na story 1.7. PR #2 foi integrado em `develop` por `87903e1`. A execução pós-integração desta branch documental em `d4b02bb` também passou com 202/202 e JaCoCo 93,64%/78,57%.
- O workflow `CI` e o job `verify` foram observados em sucesso nos heads consultados dos PRs #1 e #2; os links e SHAs estão no [relatório final](relatorio-final.md). Esta branch de documentação não teve CI executado durante a atualização deste arquivo.
- Frontend integrado em `develop` pelo merge `8e5fb34`, mas sem matriz completa de inspeção visual responsiva; RF-13 permanece parcialmente pendente.
- SonarCloud não foi executado e permanece sob responsabilidade do integrante designado.
- A RTM oficial está em `docs/RTM.md`; o README foi alinhado à matriz nesta entrega documental.
