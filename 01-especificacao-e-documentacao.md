# Organização de Recursos — Especificação e Documentação

**Versão:** 2.0 (corrigida contra as imagens de orientação) · **Stack:** Java 21 + Spring Boot 3.x · **Entrega:** semana 46
**Nível:** projeto semestral de equipe (4º período). Prefira soluções simples e bem testadas a soluções sofisticadas.

> Nota: o "Foot Fanatics" citado nas imagens é só o case das aulas (demonstração de técnicas). O projeto avaliado é este. Nenhum requisito do Foot Fanatics entra aqui.

## 1. Objetivo

Aplicação/API que organiza a alocação de **salas, professores e materiais** sem conflitos de horário. A avaliação **não** é só o sistema funcionar: exige **evidências objetivas de qualidade, segurança, rastreabilidade e desempenho**.

## 2. Perfis (conforme as imagens)

| Perfil | Papel |
|---|---|
| **Solicitante** | Professor ou coordenador. Consulta disponibilidade e cria, altera ou cancela as **suas** reservas. |
| **Responsável** | Aprova solicitações especiais (recursos restritos), valida a alocação de docentes e acompanha retiradas e devoluções de material. |
| **Administrador** | Gerencia salas, professores, materiais, usuários, bloqueios e períodos de manutenção. |

Autorização sempre verificada no servidor (menor privilégio).

## 3. Requisitos funcionais obrigatórios

As imagens listam 14 requisitos em duas colunas. A versão anterior tratou a coluna direita como "derivada" e omitiu itens; aqui todos são obrigatórios.

| ID | Requisito | Aceite mínimo |
|---|---|---|
| RF-01 | Autenticação e autorização por perfil | Login válido gera token; inválido/expirado → 401; perfil sem permissão → 403 |
| RF-02 | Cadastro e consulta de salas, professores e materiais | Criar, obter, listar, atualizar, desativar; sem exclusão física |
| RF-03 | Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Filtros combináveis, paginados |
| RF-04 | Criação, alteração e cancelamento de reservas | Valida intervalo, recursos e dono; alteração revalida conflitos |
| RF-05 | Detecção de sobreposição, inclusive na agenda do professor | Rejeita interseção; aceita intervalos contíguos |
| RF-06 | Proteção contra dupla reserva em solicitações simultâneas | Duas solicitações concorrentes → uma única reserva aceita, a outra 409 |
| RF-07 | Aprovação obrigatória para recursos restritos | Só Responsável aprova; solicitante não aprova a própria |
| RF-08 | Bloqueio de recursos em manutenção | Recurso bloqueado não é reservável na janela |
| RF-09 | Registro de retirada e devolução de materiais e equipamentos | Registrado pelo Responsável; vincula à reserva |
| RF-10 | Histórico auditável de mudanças | Evento imutável com ator, ação, entidade, data |
| RF-11 | Notificação simulada ou integração com API externa | Adaptador desacoplado; testado com WireMock |
| RF-12 | Relatório de utilização por recurso, carga horária alocada e conflitos evitados | Filtros por período |
| RF-13 | Interface responsiva e mensagens de erro compreensíveis | Página web simples que funciona em celular; erros em linguagem clara |
| RF-14 | Documentação de API ou dos fluxos públicos | OpenAPI/Swagger + README |

## 4. Regras críticas (conforme as imagens)

**Tempo e concorrência**
1. O término deve ser posterior ao início.
2. Reservas do mesmo recurso (sala, material ou professor) não podem se sobrepor.
3. Duas solicitações simultâneas devem produzir **uma única** reserva aceita.
4. Recursos em manutenção não podem ser reservados.

**Estados e auditoria**
- Fluxo principal: `SOLICITADA → APROVADA → EM_USO → CONCLUIDA`.
- Alternativos: `REJEITADA`, `CANCELADA`, `NAO_COMPARECEU`.
- Somente responsáveis aprovam recursos restritos.
- **Reservas iniciadas não podem ser apagadas** (e nenhuma reserva é apagada; o sistema não tem DELETE).
- **Toda mudança de estado gera auditoria.**

> Correção: a versão anterior usava `PENDENTE` e omitia `SOLICITADA`, `EM_USO` e `NAO_COMPARECEU`, e propunha expiração automática de pendentes, que não está nas imagens. Foi removida.

### 4.1 Transições permitidas

| De | Para | Quem | Observação |
|---|---|---|---|
| (criação) | SOLICITADA | Solicitante | Sempre inicia assim |
| SOLICITADA | APROVADA | Sistema (recurso comum) ou Responsável (recurso restrito) | Recurso comum é aprovado automaticamente na criação (decisão D2) |
| SOLICITADA | REJEITADA | Responsável | Comentário opcional |
| SOLICITADA, APROVADA | CANCELADA | Dono ou Responsável | Antes de iniciar |
| APROVADA | EM_USO | Responsável | Registra retirada (RF-09) |
| APROVADA | NAO_COMPARECEU | Responsável | Só após o horário de início |
| EM_USO | CONCLUIDA | Responsável | Registra devolução (RF-09) |

Qualquer outra transição → 409. Estados finais: REJEITADA, CANCELADA, NAO_COMPARECEU, CONCLUIDA.

### 4.2 Decisões adotadas (validar com o docente)

| # | Decisão | Padrão adotado |
|---|---|---|
| D1 | Quais estados ocupam agenda | `SOLICITADA`, `APROVADA`, `EM_USO` (evita duas solicitações aceitas para o mesmo horário) |
| D2 | Recurso comum | Aprovação automática, com evento de auditoria |
| D3 | Material | Unidade exclusiva (não compartilhável); sem controle de estoque |
| D4 | "Responsável" | Campo `responsible_id` no recurso restrito; aprova reservas dos recursos pelos quais responde (Admin também pode) |
| D5 | Intervalo | Semiaberto `[início, fim)`; conflito se `a < d AND c < b`; UTC no banco |
| D6 | Hash de senha | BCrypt |
| D7 | Notificação | Interface `NotificationPort` com adaptador HTTP simulado |
| D8 | Interface | Página estática HTML/CSS/JS responsiva servida pelo próprio Spring Boot |

## 5. Modelo de dados (UUID, UTC)

- `users(id, name, email UNIQUE, password_hash, active, created_at)`, `roles`, `user_roles`. Papéis: `SOLICITANTE`, `RESPONSAVEL`, `ADMINISTRADOR`.
- `resources(id, type SALA|PROFESSOR|MATERIAL, name, location, capacity, skills, restricted, responsible_id, status ATIVO|MANUTENCAO|INATIVO, version, created_at, updated_at)`.
- `reservations(id, requester_id, start_at, end_at, status, purpose, version, created_at, updated_at)`; CHECK `end_at > start_at`.
- `reservation_resources(reservation_id, resource_id)`.
- `maintenance_blocks(id, resource_id, start_at, end_at, reason, created_by)`.
- `approvals(id, reservation_id, reviewer_id, decision, comment, decided_at)`.
- `withdrawal_records(id, reservation_id, resource_id, picked_up_at, returned_at, handled_by, notes)` (RF-09).
- `audit_events(id, actor_id, action, entity_type, entity_id, occurred_at, details)`, append-only. Também registra `CONFLITO_EVITADO` quando uma reserva é recusada por conflito (alimenta o relatório de "conflitos evitados").

Flyway para todas as migrations. Concorrência provada em **PostgreSQL real via Testcontainers** (H2 não serve para isso).

## 6. API REST (`/api/v1`)

| Rota | Uso | Acesso |
|---|---|---|
| `POST /auth/login` | Login | Público |
| `GET /resources`, `GET /resources/{id}` | Pesquisa/consulta | Autenticado |
| `POST /resources`, `PATCH /resources/{id}`, `POST /resources/{id}/deactivate` | Gestão de recursos | Administrador |
| `GET /resources/availability?from=&to=&type=` | Disponibilidade | Autenticado |
| `POST/GET /resources/{id}/maintenance-blocks`, `POST .../{blockId}/cancel` | Manutenção | Administrador |
| `POST /reservations`, `GET /reservations`, `GET /reservations/{id}`, `PATCH /reservations/{id}` | Reservas | Dono (Responsável/Admin conforme escopo) |
| `POST /reservations/{id}/cancel` | Cancelar | Dono ou Responsável |
| `POST /reservations/{id}/approve`, `.../reject` | Decisão | Responsável (do recurso) ou Admin; nunca o próprio solicitante |
| `POST /reservations/{id}/start`, `.../complete`, `.../no-show` | Retirada, devolução, não comparecimento | Responsável |
| `GET /audit-events` | Auditoria | Administrador |
| `GET /reports/utilization` | Relatório | Responsável/Admin |

Regras: DTOs (nunca entidade JPA), Bean Validation, paginação com limite, ordenação restrita a campos permitidos. Erro uniforme: `code`, `message` (em português, compreensível), `fieldErrors`, `traceId`. Códigos: 400, 401, 403, 404, 409, 500 (sem stack trace).

## 7. Arquitetura

Monólito modular em pacotes por domínio: `auth`, `resource`, `reservation`, `approval`, `maintenance`, `usage` (retirada/devolução), `audit`, `notification`, `report`, `shared`. Camadas: `api` (controller+dto), `application` (service transacional), `domain`, `infrastructure`. Regras de agenda ficam no domínio, testáveis sem HTTP. Controllers não têm regra de negócio.

```text
src/main/java/.../{auth,resource,reservation,approval,maintenance,usage,audit,notification,report,shared}/
src/main/resources/{application.yml,db/migration/,static/}
src/test/java/.../{unit,integration,api,e2e}/
docs/{adr,quality,diagrams}/   RTM em docs/RTM.md
.github/workflows/ci.yml
```

## 8. Estratégia obrigatória de qualidade

| Item (imagem) | O que fazer |
|---|---|
| Testes automatizados | JUnit 5 com **testes parametrizados** (intervalos, transições); integração; **API caixa-preta**; **end-to-end** (fluxo completo login → reservar → aprovar → usar → devolver) |
| Ambiente realista | Testcontainers (PostgreSQL) e WireMock (notificação externa) |
| Concorrência | Teste automatizado com várias threads no mesmo recurso/horário: exatamente 1 aceita |
| TDD ou BDD | Ao menos uma funcionalidade (sugerido: regra de sobreposição e regras de aprovação/bloqueio), com evidência: commits "teste falhando" → "implementação" no PR, ou cenários Given/When/Then |
| CI e análise estática | GitHub Actions em PRs, JaCoCo, projeto integrado ao SonarCloud |
| Não funcionais | JMeter, autorização, validação de entrada, tratamento seguro de erros |
| Mocks | Só em testes unitários isolados; persistência e integrações críticas com ambiente real |

**Metas:** 80% linhas · 70% branches · 100% dos requisitos críticos na RTM · 0 bugs/vulnerabilidades críticas. Cobertura alta sozinha não basta: testes triviais só para subir percentual não rendem nota.

## 9. Requisitos não funcionais (essenciais)

Senha com hash; segredos fora do Git (`.env.example` só com nomes); validação no servidor; CORS restrito; logs sem senha/token; paginação e índices nos filtros; migrações repetíveis; Java 21; health endpoint; interface usável em celular.

## 10. Fluxo de trabalho e CI

PR obrigatório com revisão cruzada; branch principal protegida; CI roda compile, testes, JaCoCo e Sonar; secrets só no provedor. Cada requisito crítico aponta para teste, PR/commit e evidência.

## 11. Rastreabilidade (RTM)

`docs/RTM.md` liga **requisito → risco → teste → evidência** (a imagem exige o risco; a versão anterior não tinha essa coluna). Exemplo:

| Req. | Risco | Teste | Evidência |
|---|---|---|---|
| RF-06 | Dupla reserva sob concorrência | `ReservationConcurrencyIT` | Relatório Surefire + hash do PR |
| RF-07 | Autoaprovação | `ApprovalApiTest` | idem |
| RF-09 | Devolução sem retirada | `UsageFlowTest` | idem |
| RF-10 | Mudança de estado sem auditoria | `AuditOnTransitionTest` | idem |

Todos os RF-01..RF-14 entram na matriz.

## 12. Entregáveis

1. Repositório com histórico e revisão por pares.
2. Aplicação executável e instruções de instalação.
3. `README.md` e documentação dos fluxos/API.
4. `RTM.md` (requisito, risco, teste, evidência).
5. Diagramas de contexto, componentes e sequências críticas.
6. Plano, suíte automatizada e relatório de testes.
7. Pipeline de CI.
8. Relatórios SonarCloud e JaCoCo.
9. Plano e resultado do teste de carga.
10. Registro de defeitos (encontrados, priorizados, corrigidos).
11. **Relatório final de qualidade** (omitido na versão anterior).
12. Apresentação e demonstração.

**Limitadores de nota:** aplicação que não executa, pipeline ausente, falha de autorização, possibilidade de dupla reserva.

## 13. Avaliação

| Dimensão | Peso |
|---|---:|
| Requisitos e regras de negócio | 15% |
| Estratégia e documentação de testes | 15% |
| Qualidade dos testes automatizados | 25% |
| CI, cobertura e análise estática | 15% |
| Performance, segurança e robustez | 10% |
| Rastreabilidade e gestão de defeitos | 10% |
| Organização, apresentação e autoria | 10% |

## 14. Cronograma real das imagens

A versão anterior trazia um cronograma que não correspondia ao da imagem (por exemplo, prova oral em outra semana e carga na 43). O correto:

| Semana | Aula | O que avança no projeto |
|---|---|---|
| 34 | Caixa branca vs caixa preta | Repositório, ambiente, escopo e perfis |
| 35 | Arquitetura e análise ATAM com IA agêntica | C4, utility tree, riscos, trade-offs e ADRs |
| 36 | Gestão de erros e bugs | Registro e triagem dos primeiros defeitos |
| 37 | Testes automatizados em CI | Casca Maven com teste mínimo e GitHub Actions a cada PR |
| 38 | Unidade com JUnit 5 + JaCoCo + SonarCloud | Cobertura de ramo nas regras de sobreposição |
| 39 | WireMock/Testcontainers | Banco em container e integração externa isolada |
| 40 | **Prova escrita** | — |
| 41 | TDD e BDD | Regras de aprovação e bloqueio guiadas por teste |
| 42 | Carga com JMeter | Reservas simultâneas do mesmo recurso sob carga |
| 43–44 | Desenvolvimento do projeto final | Relatórios, auditoria e fechamento de requisitos |
| 45 | Preparação para a prova oral | RTM fechada e relatório de qualidade |
| 46 | **Finalização e entrega** | Release candidate, pacote de evidências, decisão de qualidade |
| 47 | **Prova oral** | — |
| 48 | Feedback e correções | Causa raiz, teste de regressão, memória do projeto |
| 49 | Revisão integrada | Requisito, risco, técnica, automação, evidência e decisão |
| 50 | Encerramento | Retrospectiva por métricas, dívida remanescente, plano de evolução |

## 15. Riscos e pendências

| Risco | Tratamento |
|---|---|
| Concorrência validada só em Java | Proteção no PostgreSQL + teste concorrente real |
| Estados que ocupam agenda (D1) e papel do Responsável (D4) ambíguos | Confirmar com docente; registrar em ADR |
| Escopo grande (14 RFs) para 4 pessoas | Interface mínima; notificação simulada; sem estoque; sem jobs automáticos |
| Carga sem SLO definido | Combinar meta com docente antes de afirmar conformidade |
| Deixar testes, CI e documentação para o fim | Seguir ordem do roteiro; defeitos registrados desde a semana 36 |
