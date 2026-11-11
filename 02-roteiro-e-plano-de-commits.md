# Roteiro de Implementação e Plano de 16 Commits

Acompanha `01-especificacao-e-documentacao.md` (v2.0). **4 pessoas × 4 commits = 16 commits**, integrados em ordem. Cobre RF-01 a RF-14.

## 1. Acordos de equipe

| Pessoa | Foco | Commits |
|---|---|---|
| A — fundação e identidade | Estrutura, banco, auditoria base, login, CI | 01–04 |
| B — recursos e busca | Recursos, filtros, disponibilidade, manutenção | 05–08 |
| C — reservas e concorrência | Reserva, API, concorrência, aprovação e uso | 09–12 |
| D — evidências e entrega | Relatório/notificação, testes e RTM, carga, docs e interface | 13–16 |

Todos revisam PRs. É possível trocar módulos 1:1 mantendo 4 commits por pessoa.

**Convenções:** uma branch por item (`feat/NN-descricao`); prefixos `chore|feat|fix|test|docs|ci`; PR pequeno com objetivo, RF, testes rodados e evidência; revisão cruzada; nunca versionar `.env`, senha ou token; usar `./mvnw clean verify`. Decisões D1–D8 da especificação registradas em `docs/adr/` antes do commit 05.

**Mudanças em relação à versão anterior:**
- Auditoria passa a existir desde o commit 02 (a imagem exige auditoria em *toda* mudança de estado; deixá-la para o commit 13 obrigaria refazer tudo).
- Incluídos os requisitos que faltavam: retirada/devolução (RF-09), notificação simulada (RF-11), interface responsiva (RF-13), estados `SOLICITADA/EM_USO/NAO_COMPARECEU`, conflitos evitados no relatório, teste end-to-end, testes parametrizados, evidência de TDD/BDD, relatório final de qualidade e risco na RTM.
- Gerenciar recursos é do **Administrador**; o Responsável aprova e acompanha retirada/devolução.
- Simplificado para o nível do 4º período: BCrypt, sem rate limit, sem outbox, sem idempotency key, sem Argon2.

## 2. Os 16 commits

### Pessoa A

**Commit 01 — Esqueleto e execução local**
- **Implementar:** projeto Maven com Spring Web, Validation, Data JPA, PostgreSQL, Flyway, Security, Actuator, springdoc, JUnit 5; Maven Wrapper; `application.yml` com perfis `local` e `test`; `.gitignore`; `.env.example` (só nomes); `/actuator/health`; pacotes por domínio; teste mínimo que passa; `docs/adr/0001-stack.md` com decisões D1–D8 e pendências; início de `docs/quality/defects.md`.
- **Validar:** `java -version` = 21; `./mvnw clean verify` verde; app sobe; health 200; Swagger abre.
- **Mensagem:** `chore(bootstrap): cria esqueleto Spring Boot e execução local`

**Commit 02 — Banco, identidade e auditoria base**
- **Implementar:** `docker-compose.yml` com PostgreSQL (senha por variável); Flyway V1: `users`, `roles`, `user_roles`, `audit_events` (append-only: sem endpoints de update/delete); `AuditService.registrar(ator, ação, entidade, id, detalhes)` para todos os módulos usarem; seed dos 3 papéis; seed de usuários só no perfil `local`.
- **Validar:** teste de integração com Testcontainers confirma schema, FK, e-mail único e gravação de evento; ambiente de teste isolado.
- **Mensagem:** `feat(persistence): adiciona Flyway, identidade e auditoria base`

**Commit 03 — Autenticação e autorização (RF-01)**
- **Implementar:** `POST /auth/login`; BCrypt; JWT com chave em variável de ambiente; Spring Security sem sessão; regras por papel; 401/403 com erro uniforme em português (`code`, `message`, `fieldErrors`, `traceId`); usuário inativo bloqueado; nunca retornar hash; usuários de teste só em fixtures.
- **Validar (parametrizados):** login ok, senha errada, token ausente/inválido/expirado, rota restrita por papel, usuário inativo; logs sem senha/token.
- **Mensagem:** `feat(auth): implementa login JWT e autorização por perfil`

**Commit 04 — Pipeline CI e gate de qualidade**
- **Implementar:** GitHub Actions (checkout, Java 21, cache, `./mvnw verify`) a cada PR; JaCoCo com relatório; SonarCloud com token só em secret; dependência WireMock no `pom`; ativar proteção da branch principal; meta 80%/70% como gate (gradual no início, com data para ficar obrigatório).
- **Validar:** PR de teste dispara o workflow; falha proposital bloqueia o merge; volta a verde. Equipe toda confirma as regras da branch.
- **Mensagem:** `ci(quality): adiciona pipeline Java 21, JaCoCo e SonarCloud`

### Pessoa B

**Commit 05 — Modelo de recursos (RF-02, parte 1)**
- **Implementar:** Flyway V2 `resources` (incluindo `restricted`, `responsible_id`, `status`, `version`); enums de tipo/status; entidade e repositório; DTOs; validação por tipo (sala exige capacidade e local; professor exige competências; material é unidade exclusiva); ADR de como representar competências.
- **Validar:** constraints no PostgreSQL; testes parametrizados das validações por tipo.
- **Mensagem:** `feat(resource): modela salas, professores e materiais`

**Commit 06 — API de cadastro (RF-02, parte 2)**
- **Implementar:** `POST /resources`, `GET /resources/{id}`, `PATCH /resources/{id}`, `POST /resources/{id}/deactivate`; só **Administrador** gerencia; sem exclusão física; `@Version`; auditoria em cada alteração via `AuditService`; OpenAPI com exemplos.
- **Validar:** testes de API 201/400/401/403/404/409; desativado continua referenciável.
- **Mensagem:** `feat(resource): expõe cadastro e desativação de recursos`

**Commit 07 — Pesquisa e disponibilidade (RF-03)**
- **Implementar:** `GET /resources` com filtros combináveis (tipo, localização, capacidade mínima, competência, status), paginação com limite e ordenação permitida; `GET /resources/availability?from=&to=&type=` (nesta etapa considera status e manutenção; reservas entram no commit 10); consultas sem SQL concatenado.
- **Validar:** testes de cada filtro e combinações; janela inválida; paginação.
- **Mensagem:** `feat(resource): adiciona filtros e consulta de disponibilidade`

**Commit 08 — Bloqueio de manutenção (RF-08)**
- **Implementar:** Flyway V3 `maintenance_blocks`; criar/listar/cancelar bloqueio (Administrador); validação `fim > início`; recurso em bloqueio sai da disponibilidade; auditoria; regra de sobreposição de bloqueios. A recusa de reserva sobre bloqueio é concluída no commit 10. Abordagem **TDD**: testes escritos antes, registrados no PR (evidência de técnica).
- **Validar:** unitários de sobreposição; integração PostgreSQL; revisão B+C da compatibilidade de `Resource`.
- **Mensagem:** `feat(maintenance): gerencia bloqueios de manutenção de recursos`

### Pessoa C

**Commit 09 — Domínio da reserva e regra temporal**
- **Implementar:** Flyway V4 `reservations` (CHECK `end_at > start_at`) e `reservation_resources`; enum com **SOLICITADA, APROVADA, EM_USO, CONCLUIDA, REJEITADA, CANCELADA, NAO_COMPARECEU**; classe de transição de estados (tabela da seção 4.1 da especificação); `IntervalOverlap` (`a < d && c < b`, semiaberto). Fazer em **TDD** (testes primeiro).
- **Validar (parametrizados):** idênticos, parcial, contido, adjacente permitido, invertido; todas as transições válidas e inválidas; cobertura de ramos na regra de sobreposição.
- **Mensagem:** `feat(reservation): cria estados e regra de sobreposição de horários`

**Commit 10 — API de reservas (RF-04, RF-05)**
- **Implementar:** `POST/GET /reservations`, `GET /{id}`, `PATCH /{id}`, `POST /{id}/cancel`; transação no serviço; checa sobreposição por recurso (sala, professor, material) e bloqueios de manutenção, considerando estados que ocupam agenda (D1); recurso comum é aprovado automaticamente com auditoria (D2); alteração ignora a própria reserva; cancelamento só em SOLICITADA/APROVADA; **não existe DELETE**; conflito → 409 com mensagem clara e evento `CONFLITO_EVITADO`; atualizar a disponibilidade do commit 07.
- **Validar:** API com PostgreSQL: conflito de sala, de professor e de material; adjacência válida; dono vs. outro usuário; manutenção bloqueia; cancelar mantém o registro.
- **Mensagem:** `feat(reservation): implementa criação, alteração e cancelamento com validação de agenda`

**Commit 11 — Proteção contra dupla reserva (RF-06)**
- **Implementar:** estratégia no PostgreSQL, escolhida em ADR (a mais simples para o grupo): `EXCLUDE USING gist` com `btree_gist` por recurso e intervalo nos estados que ocupam agenda, **ou** lock pessimista por recurso na transação; traduzir a violação para 409; todos os recursos da reserva gravados atomicamente.
- **Validar:** teste com Testcontainers e N threads sincronizadas (`CountDownLatch`) no mesmo recurso/horário: exatamente 1 aceita, demais 409, sem linhas órfãs; repetir ~20 vezes para detectar instabilidade; caso com vários recursos. Revisão A+C da estratégia.
- **Mensagem:** `fix(reservation): impede dupla reserva concorrente no PostgreSQL`

**Commit 12 — Aprovação, retirada e devolução (RF-07, RF-09)**
- **Implementar:** Flyway V5 `approvals` e `withdrawal_records`; recurso restrito fica SOLICITADA até decisão; `approve`/`reject` só pelo Responsável do recurso ou Admin, nunca pelo solicitante; `start` (APROVADA→EM_USO, registra retirada), `complete` (EM_USO→CONCLUIDA, registra devolução), `no-show` (só após o início); reserva EM_USO/CONCLUIDA não cancela; **cada mudança de estado grava auditoria**. Fazer em **BDD** ou TDD (cenários Given/When/Then).
- **Validar:** autoaprovação negada; decisão duplicada → 409; devolução sem retirada negada; conflito entre SOLICITADA e outra reserva conforme D1; auditoria de cada transição.
- **Mensagem:** `feat(approval): adiciona aprovação de restritos, retirada e devolução`

### Pessoa D

**Commit 13 — Consulta de auditoria, relatório e notificação (RF-10, RF-11, RF-12)**
- **Implementar:** `GET /audit-events` (Administrador, filtros, paginação; sem segredos nos detalhes); `GET /reports/utilization` por período/recurso: **utilização por recurso, carga horária alocada e conflitos evitados** (conta eventos `CONFLITO_EVITADO`); `NotificationPort` + adaptador HTTP simulado chamado em aprovação, rejeição e cancelamento; falha da notificação não desfaz a reserva.
- **Validar:** relatório conferido com dataset pequeno calculado à mão; WireMock verifica a chamada de notificação e o comportamento quando ela falha (500/timeout); nenhuma senha/token na auditoria.
- **Mensagem:** `feat(audit): consulta de auditoria, relatório de utilização e notificação simulada`

**Commit 14 — Testes transversais, RTM e cobertura**
- **Implementar:** completar testes de RF-01..RF-14 (unitários parametrizados, integração, API caixa-preta); **teste end-to-end** (login → pesquisar → reservar → aprovar → retirar → devolver → auditoria); `docs/RTM.md` com **requisito, risco, teste e evidência**; plano de testes (`docs/quality/plano-de-testes.md`); gate JaCoCo 80% linhas/70% branches; corrigir defeitos e registrar em `defects.md` (severidade, responsável, reteste); sem testes triviais só para subir percentual.
- **Validar:** `./mvnw clean verify` verde; pipeline falha se abaixo da meta; Sonar sem bug/vulnerabilidade crítica; todos revisam a RTM; não declarar 100% sem evidência.
- **Mensagem:** `test(quality): fecha RTM, testes end-to-end e metas de cobertura`

**Commit 15 — Carga, segurança e robustez**
- **Implementar:** plano e script JMeter (`docs/quality/plano-de-carga.md`): consulta de recursos e **reservas simultâneas do mesmo recurso** (esperado: 1 aceita, demais 409, sem erro 500), dataset fictício, ramp-up, duração, throughput, p50/p95/p99, taxa de erro; meta de latência combinada com o docente; testes de segurança (acesso a reserva de outro usuário, perfil errado, entradas inválidas, erro sem stack trace, headers); revisar relatório do Sonar; falha do adaptador de notificação.
- **Validar:** executar e anexar resultados; descrever limitações; registrar defeitos achados e correções.
- **Mensagem:** `test(nonfunctional): carga JMeter, segurança e robustez`

**Commit 16 — Interface, documentação final e demonstração (RF-13, RF-14)**
- **Implementar:** página estática responsiva em `static/` (login, pesquisa, reservar, minhas reservas) exibindo `message` de erro em linguagem clara, testada em tela de celular; README completo (requisitos, setup, variáveis, migrations, execução, testes, Swagger); diagramas de contexto, componentes e sequências (criar reserva, conflito concorrente, aprovar restrita); `docs/quality/relatorio-final.md` (decisão de qualidade, métricas, defeitos, dívida); roteiro de demo (perfis, pesquisa, reserva, duplicada, manutenção, aprovação, retirada/devolução, auditoria).
- **Validar:** clone limpo seguindo só o README; `./mvnw clean verify`; CI verde; migrations do zero; todos executam a demo. Conferir os 12 entregáveis da especificação.
- **Mensagem:** `docs(delivery): interface responsiva, diagramas e relatório final`

## 3. Cobertura dos requisitos

| RF | Commits | RF | Commits |
|---|---|---|---|
| 01 | 03 | 08 | 08, 10 |
| 02 | 05, 06 | 09 | 12 |
| 03 | 07, 10 | 10 | 02 (base), 12, 13 |
| 04 | 09, 10 | 11 | 13 |
| 05 | 09, 10 | 12 | 13 |
| 06 | 11 | 13 | 16 |
| 07 | 12 | 14 | 01, 06, 16 |

## 4. Marcos e alinhamento com as semanas das imagens

| Marco | Commits | Semanas | Saída |
|---|---|---|---|
| Base executável | 01–04 | 34–37 | App sobe, banco, login, CI verde |
| Recursos | 05–08 | 38–39 | Cadastro, filtros, manutenção, Testcontainers |
| Reserva segura | 09–12 | 41–42 | Estados, conflito, concorrência, aprovação (TDD/BDD) |
| Evidência e entrega | 13–16 | 43–46 | Auditoria, RTM, carga, docs; **release candidate na 46** |

Semana 40 é a prova escrita (sem commit). Semanas 47–50: prova oral, correções com teste de regressão, revisão integrada e retrospectiva. Ritmo: não deixar CI, concorrência, defeitos e documentação para a última semana. B e C alinham o contrato de `Resource` antes do PR 09; evitar duas pessoas editando a mesma migration.

## 5. Checklist de PR

- [ ] Título igual à mensagem do commit; RF indicado.
- [ ] Sem segredos nem dados reais.
- [ ] Testes novos, incluindo caso negativo; autorização testada (perfil permitido e negado).
- [ ] Mudança de estado gera auditoria.
- [ ] Migrations rodam do zero; `./mvnw clean verify` verde; CI verde.
- [ ] README/OpenAPI/RTM atualizados quando necessário.
- [ ] Defeito encontrado registrado em `defects.md`.
- [ ] Revisão cruzada concluída.

## 6. Ciclo para cada funcionalidade

1. Transformar o RF em cenários: sucesso, inválido, sem permissão, conflito.
2. Escrever o teste que falha (TDD) ou o cenário Given/When/Then (BDD).
3. Implementar: domínio, serviço transacional, repositório, controller/DTO.
4. Criar migration e constraints; pensar na concorrência.
5. Rodar `./mvnw clean verify` e olhar os relatórios, não só o resultado final.
6. Atualizar RTM/README/ADR e abrir PR pequeno com evidência.

## 7. Comandos de validação

```bash
java -version                      # Java 21
./mvnw clean verify                # compila, testa, JaCoCo
./mvnw -Dtest=Reservation* test    # testes de um módulo
```

O README deve trazer apenas comandos que a equipe efetivamente executou.
