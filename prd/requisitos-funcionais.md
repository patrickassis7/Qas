# Requisitos — Organização de Recursos

Derivados do enunciado do projeto. **Críticos** = exigidos a 100% na RTM e limitadores de nota quando violados.
Perfis: **SOL** = Solicitante · **RESP** = Responsável · **ADM** = Administrador.

## Requisitos funcionais

| ID | Requisito | Perfis | Crítico |
|---|---|---|---|
| RF-01 | Autenticação e autorização por perfil. | Todos | Sim |
| RF-02 | Cadastro e consulta de salas, professores e materiais. | ADM (cadastro); todos (consulta) | Não |
| RF-03 | Pesquisa por tipo, capacidade, localização, competência e disponibilidade. | SOL, RESP, ADM | Não |
| RF-04 | Criação, alteração e cancelamento de reservas. | SOL | Não |
| RF-05 | Detecção de sobreposição de horários, inclusive na agenda do professor. | Sistema | Sim |
| RF-06 | Proteção contra dupla reserva em solicitações simultâneas. | Sistema | Sim |
| RF-07 | Aprovação obrigatória para recursos restritos. | RESP | Sim |
| RF-08 | Bloqueio de recursos em manutenção. | ADM | Sim |
| RF-09 | Registro de retirada e devolução de materiais e equipamentos. | RESP | Não |
| RF-10 | Histórico auditável de mudanças. | Sistema; consulta ADM | Sim |
| RF-11 | Notificação simulada ou integração com API externa. | Sistema | Não |
| RF-12 | Relatório de utilização por recurso, carga horária alocada e conflitos evitados. | RESP, ADM | Não |
| RF-13 | Interface responsiva e mensagens de erro compreensíveis. | Todos | Não |
| RF-14 | Documentação de API ou dos fluxos públicos do sistema. | — | Não |

## Regras de negócio críticas

| ID | Regra | Relacionado |
|---|---|---|
| RN-01 | O término da reserva deve ser posterior ao início. | RF-04 |
| RN-02 | Reservas do mesmo recurso (sala, material ou professor) não podem se sobrepor. | RF-05 |
| RN-03 | Duas solicitações simultâneas devem produzir uma única reserva aceita. | RF-06 |
| RN-04 | Recursos em manutenção não podem ser reservados. | RF-08 |
| RN-05 | Fluxo de estados: `SOLICITADA → APROVADA → EM_USO → CONCLUÍDA`; alternativos: `REJEITADA`, `CANCELADA`, `NÃO_COMPARECEU`. | RF-04, RF-07 |
| RN-06 | Somente responsáveis aprovam recursos restritos. | RF-07, RF-01 |
| RN-07 | Reservas iniciadas não podem ser apagadas. | RF-04 |
| RN-08 | Toda mudança de estado gera registro de auditoria. | RF-10 |

## Requisitos não funcionais e de qualidade

| ID | Requisito | Meta |
|---|---|---|
| RNF-01 | Cobertura de testes (JaCoCo). | ≥ 80% linhas; ≥ 70% branches |
| RNF-02 | Análise estática (SonarCloud). | 0 bugs ou vulnerabilidades críticas |
| RNF-03 | Integração contínua (GitHub Actions) em pull requests. | Pipeline ativo e verde |
| RNF-04 | Teste de carga (JMeter), incluindo reservas simultâneas do mesmo recurso. | Plano e resultado documentados |
| RNF-05 | Segurança: autorização, validação de entrada e tratamento seguro de erros. | Sem falha de autorização |
| RNF-06 | Ambiente realista: Testcontainers (banco) e WireMock (integrações externas). | Persistência e integrações críticas sem mock |
| RNF-07 | Ao menos uma funcionalidade desenvolvida com TDD ou BDD, com evidência. | Evidência registrada |
| RNF-08 | Rastreabilidade: RTM ligando requisito, risco, teste e evidência. | 100% dos requisitos críticos |
| RNF-09 | Aplicação executável com instruções de instalação. | Limitador de nota |

## Pendências de definição

- Quais recursos são "restritos".
- Granularidade e duração mínima/máxima das reservas.
- Integração externa ou notificação simulada a adotar.
- Limites de desempenho (tempo de resposta, vazão) do teste de carga.
