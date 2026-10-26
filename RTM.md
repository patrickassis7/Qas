# RTM — Matriz de Rastreabilidade

Liga **requisito → risco → teste → evidência**. Riscos e testes abaixo são **propostos** pela equipe e devem ser revisados; a coluna Status deve ser atualizada a cada semana. Evidência = link para relatório de CI, JaCoCo, SonarCloud, JMeter ou defeito registrado.

Legenda de status: ⬜ Pendente · 🟨 Em andamento · ✅ Verificado

## Requisitos críticos

| Requisito | Risco | Teste planejado | Tipo | Evidência | Status |
|---|---|---|---|---|---|
| RF-01 Autenticação/autorização por perfil | Acesso indevido a ações de outro perfil | Acesso negado por perfil em cada endpoint protegido | API caixa-preta, integração | — | ⬜ |
| RF-05 / RN-02 Sobreposição (sala, material, professor) | Reserva sobreposta aceita | Casos parametrizados de limites de horário (antes, depois, contido, borda); sobreposição na agenda do professor | Unitário parametrizado, integração | — | ⬜ |
| RF-06 / RN-03 Dupla reserva simultânea | Duas reservas aceitas para o mesmo recurso | N threads reservando o mesmo recurso e horário; exatamente 1 aceita | Concorrência com Testcontainers; carga JMeter | — | ⬜ |
| RF-07 / RN-06 Aprovação de recurso restrito | Aprovação por perfil não autorizado | SOL tenta aprovar → negado; RESP aprova → sucesso | TDD/BDD, API | — | ⬜ |
| RF-08 / RN-04 Bloqueio por manutenção | Recurso em manutenção reservado | Reserva dentro de período bloqueado é recusada; fora do período é aceita | Unitário, integração, TDD/BDD | — | ⬜ |
| RF-10 / RN-08 Histórico auditável | Mudança de estado sem registro | Cada transição de estado gera registro com autor, data e estados | Integração | — | ⬜ |
| RN-01 Término > início | Intervalo inválido persistido | Início igual, anterior e posterior ao término | Unitário parametrizado | — | ⬜ |
| RN-05 Fluxo de estados | Transição inválida de estado | Matriz de transições válidas e inválidas | Unitário parametrizado | — | ⬜ |
| RN-07 Reserva iniciada não pode ser apagada | Perda de registro de reserva em uso | Exclusão de reserva `EM_USO` é recusada | Integração, API | — | ⬜ |

## Demais requisitos

| Requisito | Risco | Teste planejado | Tipo | Evidência | Status |
|---|---|---|---|---|---|
| RF-02 Cadastro e consulta de recursos | Dados inválidos ou duplicados | Validação de entrada e persistência real | Integração | — | ⬜ |
| RF-03 Pesquisa | Resultado inconsistente com a disponibilidade | Filtros por tipo, capacidade, localização, competência e disponibilidade | Integração, API | — | ⬜ |
| RF-04 Criar/alterar/cancelar reserva | Alteração gera conflito não detectado | Alterar para horário em conflito é recusado | API, E2E | — | ⬜ |
| RF-09 Retirada e devolução | Material sem registro de retirada | Registro de retirada e devolução ligado à reserva | Integração | — | ⬜ |
| RF-11 Notificação | Falha externa derruba a reserva | Integração externa simulada (WireMock): sucesso, erro e timeout | Integração com WireMock | — | ⬜ |
| RF-12 Relatório de utilização | Números divergentes dos dados | Cálculo de utilização, carga horária e conflitos evitados | Integração | — | ⬜ |
| RF-13 Interface e mensagens de erro | Erro técnico exposto ao usuário | Mensagens compreensíveis; layout em tela pequena | E2E | — | ⬜ |
| RF-14 Documentação de API | Documentação desatualizada | Documentação gerada/validada a partir da API | Revisão | — | ⬜ |
| RNF-01 Cobertura | Cobertura alta com testes triviais | Relatório JaCoCo ≥ 80% linhas / 70% branches + revisão da qualidade dos testes | CI | — | ⬜ |
| RNF-02 Análise estática | Vulnerabilidade crítica | Quality Gate do SonarCloud | CI | — | ⬜ |
| RNF-03 Pipeline | Regressão chega ao branch principal | GitHub Actions em cada pull request | CI | — | ⬜ |
| RNF-04 Carga | Degradação sob concorrência | Plano JMeter com reservas simultâneas do mesmo recurso | Carga | — | ⬜ |
| RNF-05 Segurança | Entrada maliciosa; vazamento por mensagem de erro | Validação de entrada; erros sem detalhes internos | API, integração | — | ⬜ |
| RNF-06 Ambiente realista | Mock mascara falha de persistência | Testcontainers e WireMock nos testes de integração | Integração | — | ⬜ |
| RNF-07 TDD/BDD | Sem evidência da técnica | Histórico de commits teste-primeiro de ao menos uma funcionalidade | Processo | — | ⬜ |
| RNF-09 Aplicação executável | Entrega que não executa | Build e execução a partir das instruções do README | CI, manual | — | ⬜ |

## Resumo

| Item | Meta | Atual |
|---|---|---|
| Requisitos críticos verificados | 100% | 0 / 9 |
