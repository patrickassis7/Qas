# RTM — Matriz de Rastreabilidade

Liga **requisito → risco → teste → evidência**. Riscos e testes abaixo são **propostos** pela equipe e devem ser revisados; a coluna Status deve ser atualizada a cada semana. Evidência = link para relatório de CI, JaCoCo, SonarCloud, JMeter ou defeito registrado.

Legenda de status: ⬜ Pendente · 🟨 Em andamento · ✅ Verificado

## Requisitos críticos

| Requisito | Risco | Teste planejado | Tipo | Evidência | Status |
|---|---|---|---|---|---|
| RF-01 Autenticação/autorização por perfil | Acesso indevido a ações de outro perfil | Acesso negado por perfil em cada endpoint protegido | API caixa-preta, integração | `AuthIntegrationTest`, `PublicApiWorkflowEndToEndTest`; [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RF-05 / RN-02 Sobreposição (sala, material, professor) | Reserva sobreposta aceita | Casos parametrizados de limites de horário (antes, depois, contido, borda); sobreposição na agenda do professor | Unitário parametrizado, integração | `IntervalOverlapTest`, `ReservationIntegrationTest`; [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RF-06 / RN-03 Dupla reserva simultânea | Duas reservas aceitas para o mesmo recurso | N threads reservando o mesmo recurso e horário; exatamente 1 aceita | Concorrência com PostgreSQL 16 nativo no CI; carga JMeter | `ReservationConcurrencyIntegrationTest`; resultados JMeter em [plano de carga](docs/quality/plano-de-carga.md); [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RF-07 / RN-06 Aprovação de recurso restrito | Aprovação por perfil não autorizado | SOL tenta aprovar → negado; RESP aprova → sucesso | Integração, API | `ApprovalIntegrationTest`, `PublicApiWorkflowEndToEndTest`; [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RF-08 / RN-04 Bloqueio por manutenção | Recurso em manutenção reservado | Reserva dentro de período bloqueado é recusada; fora do período é aceita | Unitário, integração | `ResourceMaintenanceIntegrationTest`, `ReservationIntegrationTest`; [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RF-10 / RN-08 Histórico auditável | Mudança de estado sem registro ou exposição de credenciais | Persistência, consulta administrativa paginada e bloqueio de senha/token | Integração | `IdentityAuditIntegrationTest`, `ApprovalIntegrationTest`; [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RN-01 Término > início | Intervalo inválido persistido | Início igual, anterior e posterior ao término | Unitário parametrizado | `ResourceValidatorTest`, `ReservationIntegrationTest`; [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RN-05 Fluxo de estados | Transição inválida de estado | Matriz de transições válidas e inválidas | Unitário parametrizado | `ReservationStatusTransitionTest`, `ApprovalIntegrationTest`; [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RN-07 Reserva iniciada não pode ser apagada | Perda de registro de reserva em uso | Exclusão de reserva `EM_USO` é recusada | Integração, API | `ReservationIntegrationTest`; [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |

## Demais requisitos

| Requisito | Risco | Teste planejado | Tipo | Evidência | Status |
|---|---|---|---|---|---|
| RF-02 Cadastro e consulta de recursos | Dados inválidos ou duplicados | Validação de entrada e persistência real | Integração | — | ⬜ |
| RF-03 Pesquisa | Resultado inconsistente com a disponibilidade | Filtros por tipo, capacidade, localização, competência e disponibilidade | Integração, API | — | ⬜ |
| RF-04 Criar/alterar/cancelar reserva | Alteração gera conflito não detectado | Alterar para horário em conflito é recusado | API, E2E | — | ⬜ |
| RF-09 Retirada e devolução | Material sem registro de retirada | Registro de retirada e devolução ligado à reserva | Integração | — | ⬜ |
| RF-11 Notificação | Falha externa derruba a reserva | WireMock valida envio, HTTP 500 e timeout sem rollback | Integração com WireMock | `ApprovalIntegrationTest` | ✅ |
| RF-12 Relatório de utilização | Números divergentes dos dados | Dataset manual verifica horas, percentual, recurso e conflitos | Integração | `UtilizationReportIntegrationTest` | ✅ |
| RF-13 Interface e mensagens de erro | Erro técnico exposto ao usuário | Mensagens compreensíveis; layout em tela pequena | E2E | Validação manual reportada nas seis larguras em [story 1.6](docs/stories/1.6-commit-16-frontend-rf13.md); capturas não preservadas | 🟨 |
| RF-14 Documentação de API | Documentação desatualizada | Revisão dos endpoints, filtros e semântica do relatório | Revisão | `README.md` | ✅ |
| RNF-01 Cobertura | Cobertura alta com testes triviais | Relatório JaCoCo ≥ 80% linhas / 70% branches + revisão da qualidade dos testes | CI | Gate JaCoCo aprovado em [workflow_dispatch](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RNF-02 Análise estática | Vulnerabilidade crítica | Quality Gate do SonarCloud | CI | [Quality Gate SonarCloud aprovado: 0 novos issues e 0 security hotspots na PR #7](https://sonarcloud.io/dashboard?id=patrickassis7_Qas&pullRequest=7) | ✅ |
| RNF-03 Pipeline | Regressão chega ao branch principal | GitHub Actions em cada pull request | CI | Jobs `verify` e `supabase-connection` aprovados em [workflow_dispatch](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RNF-04 Carga | Degradação sob concorrência | Plano JMeter com reservas simultâneas do mesmo recurso | Carga | Resultados e limites documentados em [plano de carga](docs/quality/plano-de-carga.md) | ✅ |
| RNF-05 Segurança | Entrada maliciosa; vazamento por mensagem de erro | Validação de entrada; erros sem detalhes internos | API, integração | `AuthIntegrationTest`, `IdentityAuditIntegrationTest`, `ResourceMaintenanceIntegrationTest`; [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RNF-06 Ambiente realista e isolamento | Mock mascara falha de persistência; testes podem alcançar banco remoto ou compartilhar estado | Suíte completa em PostgreSQL 16 nativo no CI, com schema exclusivo por classe, verificação de loopback e rejeição de datasource remoto; WireMock para integração externa. Probe opcional de Supabase em job manual isolado: somente `SELECT 1` em transação read-only | Integração, CI e segurança | Jobs `verify` e `supabase-connection` aprovados; probe read-only em [workflow_dispatch](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RNF-07 TDD/BDD | Sem evidência da técnica | Histórico de commits teste-primeiro de ao menos uma funcionalidade | Processo | — | ⬜ |
| RNF-08 Rastreabilidade | Requisito crítico sem caso de teste ou evidência | RTM liga requisito crítico → risco → teste → evidência | Processo | Esta RTM contém testes e evidência para os 9 requisitos críticos acima; execuções reunidas no [CI verify](https://github.com/patrickassis7/Qas/actions/runs/43480494339) | ✅ |
| RNF-09 Aplicação executável | Entrega que não executa | Build e execução a partir das instruções do README | CI, manual | Maven `clean verify` aprovado no [workflow_dispatch](https://github.com/patrickassis7/Qas/actions/runs/43480494339); smoke completo conforme instruções não reexecutado | 🟨 |

## Resumo

| Item | Meta | Atual |
|---|---|---|
| Requisitos críticos verificados | 100% | 9 / 9 |
