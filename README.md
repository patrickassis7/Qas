# Organização de Recursos

> Um sistema confiável para alocar salas, professores e materiais sem conflitos.

**Projeto semestral · Equipes · Java 21 · Spring Boot 3.x · Entrega na semana 46**

## Objetivo

Desenvolver e validar uma aplicação que organize a alocação de salas, professores e materiais sem conflitos de horário.

O foco da avaliação não é apenas fazer o sistema funcionar, mas apresentar **evidências objetivas de qualidade, segurança, rastreabilidade e desempenho**.

## Este projeto e o case das aulas

Nas aulas construímos, passo a passo, o **Foot Fanatics**, uma API de conteúdo esportivo com assinatura. Ele é o *case de demonstração*: serve para ver cada técnica funcionando em código ao vivo.

O projeto avaliado do semestre é **este**, o sistema de organização de recursos, desenvolvido pelas equipes. A cada semana, a técnica vista no Foot Fanatics deve ser aplicada ao nosso projeto.

> Qualquer material sobre futebol, torcedores, investidores ou estatísticas esportivas pertence ao case das aulas e **não** faz parte do escopo deste produto (os artefatos antigos desse domínio foram removidos deste repositório).

## Escopo e perfis

| Perfil | Responsabilidades |
|---|---|
| **Solicitante** | Professor ou coordenador que consulta a disponibilidade e cria, altera ou cancela suas reservas. |
| **Responsável** | Aprova solicitações especiais, valida a alocação de docentes e acompanha retiradas e devoluções de material. |
| **Administrador** | Gerencia salas, professores, materiais, usuários, bloqueios e períodos de manutenção. |

## Requisitos funcionais obrigatórios

- Autenticação e autorização por perfil.
- Cadastro e consulta de salas, professores e materiais.
- Pesquisa por tipo, capacidade, localização, competência e disponibilidade.
- Criação, alteração e cancelamento de reservas.
- Detecção de sobreposição de horários, inclusive na agenda do professor.
- Proteção contra dupla reserva em solicitações simultâneas.
- Aprovação obrigatória para recursos restritos.
- Bloqueio de recursos em manutenção.
- Registro de retirada e devolução de materiais e equipamentos.
- Histórico auditável de mudanças.
- Notificação simulada ou integração com API externa.
- Relatório de utilização por recurso, carga horária alocada e conflitos evitados.
- Interface responsiva e mensagens de erro compreensíveis.
- Documentação de API ou dos fluxos públicos do sistema.

## Regras críticas

### Tempo e concorrência

- O término deve ser posterior ao início.
- Reservas do mesmo recurso não podem se sobrepor: sala, material ou professor.
- Duas solicitações simultâneas devem produzir **uma única** reserva aceita.
- Recursos em manutenção não podem ser reservados.

### Estados e auditoria

Fluxo principal:

```
SOLICITADA → APROVADA → EM_USO → CONCLUÍDA
```

Estados alternativos: `REJEITADA`, `CANCELADA` e `NÃO_COMPARECEU`.

- Somente **responsáveis** aprovam recursos restritos.
- Reservas iniciadas não podem ser apagadas.
- Toda mudança de estado deve gerar auditoria.

## Estratégia obrigatória de qualidade

| Frente | O que é exigido |
|---|---|
| Testes automatizados | Unitários com JUnit 5, parametrizados, integração, API em caixa-preta e end-to-end. |
| Ambiente realista | Banco containerizado com Testcontainers e WireMock para integrações externas. |
| Concorrência | Teste automatizado comprovando a proteção contra dupla reserva. |
| TDD ou BDD | Ao menos uma funcionalidade nova desenvolvida com evidências da técnica. |
| CI e análise estática | GitHub Actions em pull requests, cobertura por JaCoCo e projeto integrado ao SonarCloud. |
| Não funcionais | JMeter, autorização, validação de entrada e tratamento seguro de erros. |

**Uso de mocks:** permitido em testes unitários isolados. Persistência e integrações críticas devem ser verificadas com ambiente real, Testcontainers ou WireMock.

## Metas mensuráveis

| Meta | Valor |
|---|---|
| Cobertura de linhas | **80%** |
| Cobertura de branches | **70%** |
| Requisitos críticos cobertos na RTM | **100%** |
| Bugs ou vulnerabilidades críticas | **0** |

> Cobertura alta, sozinha, não garante qualidade: testes triviais feitos apenas para elevar o percentual não asseguram pontuação máxima.

## Entregáveis

- [ ] Repositório com histórico de trabalho e revisão por pares.
- [ ] Aplicação executável e instruções de instalação.
- [ ] `README.md` e documentação dos fluxos ou da API.
- [ ] `RTM.md` ligando requisito, risco, teste e evidência (versão inicial criada; evidências pendentes).
- [ ] Diagramas de contexto, componentes e sequências críticas.
- [ ] Plano, suíte automatizada e relatório de testes.
- [ ] Pipeline de integração contínua.
- [ ] Relatórios de SonarCloud e cobertura (JaCoCo).
- [ ] Plano e resultado do teste de carga.
- [ ] Registro de defeitos encontrados, priorizados e corrigidos.
- [ ] Relatório final de qualidade.
- [ ] Apresentação e demonstração do sistema.

## Avaliação

| Dimensão | Peso |
|---|---|
| Requisitos e regras de negócio | 15% |
| Estratégia e documentação de testes | 15% |
| Qualidade dos testes automatizados | 25% |
| CI, cobertura e análise estática | 15% |
| Performance, segurança e robustez | 10% |
| Rastreabilidade e gestão de defeitos | 10% |
| Organização, apresentação e autoria | 10% |

**Limitadores de nota:** aplicação que não executa, pipeline ausente, falha de autorização ou possibilidade de dupla reserva.

## Evolução durante o semestre

1. Diagnóstico inicial
2. Requisitos, riscos e RTM
3. Testes e integração
4. CI e SonarCloud
5. TDD/BDD, WireMock e carga
6. Entrega, feedback e retrospectiva

### Incremento por semana

A técnica é demonstrada em aula no Foot Fanatics e aplicada, na mesma semana, ao projeto de organização de recursos.

| Semana | Tema da aula | O que avança no nosso projeto |
|---|---|---|
| 34 | Caixa branca vs caixa preta | Repositório, ambiente, escopo e perfis definidos |
| 35 | Arquitetura e análise ATAM com IA agêntica | Arquitetura do sistema de recursos: C4, utility tree, riscos, trade-offs e ADRs |
| 36 | Gestão de erros e bugs | Registro e triagem dos primeiros defeitos, com a arquitetura construída por prompts |
| 37 | Testes automatizados em CI | Casca Maven com teste mínimo e pipeline no GitHub Actions a cada pull request |
| 38 | Testes de unidade com JUnit 5 + JaCoCo + SonarCloud | Cobertura de ramo nas regras de sobreposição de horário |
| 39 | WireMock/Testcontainers e chamadas de API seguras | Banco em container e integração externa isolada em teste |
| 40 | Prova escrita | — |
| 41 | TDD e BDD | Regras de aprovação e bloqueio guiadas por teste |
| 42 | Testes de carga com JMeter | Reservas simultâneas do mesmo recurso sob carga |
| 43-44 | Desenvolvimento do projeto final | Relatórios, auditoria e fechamento dos requisitos |
| 45 | Preparação para a prova oral | RTM fechada e relatório de qualidade |
| 46 | Finalização e entrega | Release candidate, pacote de evidências e decisão de qualidade |
| 47 | Prova oral | — |
| 48 | Feedback e correções | Causa raiz, teste de regressão e atualização da memória do projeto |
| 49 | Revisão integrada | Relação entre requisito, risco, técnica, automação, evidência e decisão |
| 50 | Encerramento | Retrospectiva por métricas, dívida remanescente e plano de evolução |

## Tecnologias

- Java 21
- Spring Boot 3.x
- Maven
- JUnit 5, JaCoCo, Testcontainers, WireMock, JMeter
- GitHub Actions e SonarCloud

## Como executar

> Seção a ser preenchida a partir da semana 37, quando a casca Maven e o pipeline estiverem disponíveis. Deve conter: pré-requisitos (JDK 21, Maven, Docker), comandos para compilar, executar a aplicação e rodar a suíte de testes, e como gerar os relatórios JaCoCo.

## Documentação

| Documento | Conteúdo |
|---|---|
| [`prd/README.md`](prd/README.md) | Fluxo de documentação do produto |
| [`prd/personas/`](prd/personas) | Personas: Solicitante, Responsável e Administrador |
| [`prd/requisitos-funcionais.md`](prd/requisitos-funcionais.md) | Requisitos funcionais, regras de negócio e requisitos não funcionais |
| [`RTM.md`](RTM.md) | Rastreabilidade requisito ↔ risco ↔ teste ↔ evidência |

## Equipe

| Nome | Papel |
|---|---|
| *(preencher)* | *(preencher)* |
