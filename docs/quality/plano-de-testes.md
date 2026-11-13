# Plano de Testes — Commit 14

## Objetivo e escopo

Demonstrar por evidência reproduzível os requisitos RF-01 a RF-14 e as regras RN-01 a RN-08, preservando os contratos do produto. O mapeamento requisito → risco → teste → evidência está em [`docs/RTM.md`](../RTM.md). Em 2026-11-09, `./mvnw --batch-mode --no-transfer-progress clean verify` passou com 193 testes, sem falhas, erros ou skips.

## Estratégia por camada

| Camada | Escopo | Ambiente/evidência |
|---|---|---|
| Unitário parametrizado | Limites de intervalo, transições, validação de recursos e configurações de autenticação | JUnit 5; `src/test/java/br/com/organizacaorecursos/reservation/`, `resource/` e `auth/` |
| Integração | Persistência, auditoria, concorrência, manutenção, aprovação, retirada/devolução e relatório | PostgreSQL 16 instalado nativamente no runner CI; migrations Flyway; `src/test/java/br/com/organizacaorecursos/integration/` |
| Integração externa | Envio de aprovação/rejeição/cancelamento e resposta HTTP/timeout | WireMock; `ApprovalIntegrationTest` |
| API caixa-preta | Rotas HTTP, status, payload, autenticação/autorização, filtros e erros | MockMvc nos testes de integração; dados sintéticos e banco isolado por classe |
| Ponta a ponta | Login → pesquisar → reservar recurso restrito → aprovar → retirar/iniciar → devolver/concluir → consultar auditoria | `PublicApiWorkflowEndToEndTest`, `TestRestTemplate`, servidor HTTP aleatório e PostgreSQL local nativo no CI |
| Interface visual | Responsividade e inspeção visual de mensagens | Validação manual reportada pelo usuário em Chrome DevTools: login, navegação, recursos, reservas, formulários e mensagens de erro; overflow retornou `true` em 320, 375, 390, 768, 1024 e 1440 px. Sem capturas preservadas; consultar [story 1.6](../stories/1.6-commit-16-frontend-rf13.md). |

## Dados e isolamento

- Cada classe de integração usa um schema PostgreSQL exclusivo no servidor local do job; Flyway migra esse schema. O teste comprova `current_schema()`, ausência de visibilidade de dados sentinela entre duas classes e limpeza/recriação do schema por classe.
- Dentro de uma classe, os casos não dependem da ordem nem de dados compartilhados: cada método cria/limpa seus dados de forma determinística. A suíte completa deve passar em duas execuções consecutivas.
- O datasource do perfil de teste é explícito e aceita somente PostgreSQL em loopback. Validação fail-closed rejeita URL ausente/malformada, hostname/IP remoto, Supabase e configuração externa via propriedades/variáveis; `SUPABASE_DB_URL` nunca é consumida pela suíte ou pelo job `verify`.
- O PostgreSQL de integração é local ao runner. Um probe de conectividade Supabase, se habilitado, ocorre em job independente de `workflow_dispatch`, não executa Spring/Flyway/testes e executa exclusivamente `BEGIN TRANSACTION READ ONLY; SELECT 1; ROLLBACK;`. O probe não é evidência de destino válido para testes.
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

## Conectividade Supabase (fora da suíte)

- O job remoto só pode ser elegível em `workflow_dispatch`; não é dependência do job `verify` nem recebe o secret em Pull Requests.
- `SUPABASE_DB_URL` ausente causa falha explícita e segura no próprio job. Se presente, o cliente recebe parâmetros por ambiente sem imprimir o URL, incluí-lo em argumentos de processo ou gravá-lo em arquivo.
- A única instrução SQL permitida é `BEGIN TRANSACTION READ ONLY; SELECT 1; ROLLBACK;`. Nenhum teste, inicialização Spring, Flyway, migration, seed ou operação de escrita pode ser executada contra Supabase.

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
- RF-13: o usuário reportou validação visual manual das telas e larguras listadas na tabela; automação de navegador e capturas de tela não foram produzidas nesta execução. Ver [story 1.6](../stories/1.6-commit-16-frontend-rf13.md).
- Revalidação em 2026-11-09: `.\mvnw.cmd --batch-mode --no-transfer-progress clean verify` passou com 202 testes, 0 falhas, 0 erros e 0 ignorados. JaCoCo: 93,64% de linhas (1074/1147) e 78,57% de branches (220/280).
- A RTM recebeu revisão humana aprovada por `@patrickassis7` em 2026-11-09.
