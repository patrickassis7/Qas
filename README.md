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

### Commits 01–03 — execução local, persistência e login

Pré-requisitos: JDK 21 e acesso a um banco PostgreSQL. O Maven global não é necessário, pois o projeto possui Maven Wrapper.

No Windows PowerShell:

```powershell
java -version
.\mvnw.cmd clean verify
Copy-Item .env.example .env
# Preencha o .env com valores locais; ele é usado pelo Docker Compose.
docker compose up -d postgres
$env:SPRING_PROFILES_ACTIVE="local"
$env:DB_URL="jdbc:postgresql://localhost:5432/organizacao_recursos"
$env:DB_USERNAME="<usuario-local>"
$env:DB_PASSWORD="<senha-local>"
$jwtKeyBytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($jwtKeyBytes) } finally { $rng.Dispose() }
$env:JWT_SECRET = [Convert]::ToBase64String($jwtKeyBytes)
$seedPassword = Read-Host "Senha de desenvolvimento" -AsSecureString
$env:LOCAL_SEED_PASSWORD = [System.Net.NetworkCredential]::new("", $seedPassword).Password
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

O perfil `local` deve ser ativado explicitamente, como no comando acima ou com `SPRING_PROFILES_ACTIVE=local`. O Maven não carrega automaticamente o arquivo `.env`; por isso, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` precisam estar definidos na sessão do PowerShell que inicia a aplicação. `JWT_SECRET` deve ser Base64 padrão representando pelo menos 32 bytes gerados por fonte criptograficamente segura; o exemplo gera 48 bytes com `RandomNumberGenerator`. A aplicação lê essa chave exclusivamente do ambiente do processo e a decodifica antes de configurar HS256. Não passe a chave como argumento de linha de comando ou propriedade `-D`. `JWT_TTL_MINUTES` é opcional (padrão 30, limite 1–60) e `JWT_ISSUER` tem padrão `organizacao-recursos`. O `.env` nunca deve ser versionado. Reiniciar com outra chave invalida tokens anteriores.

`LOCAL_SEED_PASSWORD` é opcional e só é usada quando o perfil `local` cria usuários novos. Se ausente, os usuários recebem hash de senha aleatória desconhecida; não há senha padrão. Usuários já existentes nunca têm a senha alterada automaticamente. Para login manual, defina a variável antes da **primeira** inicialização de um banco local novo. Não use o perfil `local` em produção.

Com a aplicação em execução:

- Health: `http://localhost:8080/actuator/health`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

O Commit 02 ativa PostgreSQL, Flyway V1, identidade e auditoria base. Usuários fictícios são semeados apenas no perfil `local`; o perfil `test` usa PostgreSQL isolado via Testcontainers. Não há autenticação JWT no Commit 02.

No Commit 03, o login aceita e-mail e senha e emite JWT com validade limitada. Exemplo em outra sessão PowerShell, com a senha local definida nela:

```powershell
$body = @{ email = "professor.local@organizacao-recursos.local"; password = $env:LOCAL_SEED_PASSWORD } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/auth/login -ContentType application/json -Body $body
$token = $login.accessToken
```

Os três papéis persistidos são `SOLICITANTE`, `RESPONSAVEL` e `ADMINISTRADOR`. As rotas de negócio exigem JWT. A auditoria é consultada por `GET /api/v1/audit-events` (somente Administrador), com filtros `actorId`, `action`, `entityType`, `entityId`, `from` e `to`, além de `page` e `size` (máximo 100). A resposta expõe `actorId`, nunca a entidade de usuário ou seu hash; detalhes com chaves de senha, token, segredo ou autorização são recusados pela aplicação e pelo banco.

O relatório `GET /api/v1/reports/utilization` é permitido a Responsável e Administrador. Informe `startAt` e `endAt` em ISO-8601; `resourceId` é opcional. O intervalo é semiaberto `[startAt, endAt)`. A carga soma a duração sobreposta ao período para reservas `APROVADA`, `EM_USO` e `CONCLUIDA`; canceladas, rejeitadas e não comparecimentos não contam. A utilização percentual divide as horas alocadas pela duração total do intervalo. Conflitos evitados contam eventos `CONFLITO_EVITADO` no intervalo e são atribuídos apenas aos recursos efetivamente conflitantes.

As transições de aprovação, rejeição e cancelamento (incluindo aprovação automática) enviam `POST /notifications` ao adaptador HTTP simulado. Configure `NOTIFICATION_BASE_URL` (padrão `http://localhost:8099`), `NOTIFICATION_CONNECT_TIMEOUT_MS` e `NOTIFICATION_READ_TIMEOUT_MS` (padrão de 500 ms). Erros HTTP e timeouts são registrados sem desfazer a reserva.

Erros de autenticação e autorização usam `code`, `message`, `fieldErrors` e `traceId` em português, sem expor hash ou token. A aplicação recusa iniciar sem `JWT_SECRET`.

## Interface web (Commit 16 / RF-13)

Com o banco PostgreSQL configurado e a aplicação iniciada pelo comando PowerShell acima, abra **http://localhost:8080/**. A interface é servida pelo próprio Spring Boot, não requer Node.js, servidor separado ou bibliotecas JavaScript externas. Use as mesmas credenciais locais cadastradas na primeira inicialização do perfil `local`.

A tela inicial oferece consulta de recursos, verificação de disponibilidade por período, criação e acompanhamento das reservas. Administradores também podem cadastrar, atualizar e desativar recursos; administradores e responsáveis podem enviar ações de aprovação e operação para reservas permitidas pelo backend. O token fica somente no `sessionStorage` da aba e é removido ao sair ou quando a API retorna 401.

A API não fornece endpoint de perfil nem fila de aprovações para responsáveis. A interface identifica capacidades consultando os endpoints já protegidos de auditoria e relatório; responsáveis precisam conhecer o UUID da reserva para iniciar uma ação. A gestão de recurso aceita o UUID do responsável porque não existe endpoint de consulta de usuários. O backend permanece responsável pela autorização final. Erros de conflito (409), acesso (401/403), validação (400) e falhas de servidor são exibidos com a mensagem retornada pela API; dados da API são inseridos como texto.

Para encerrar a aplicação, pressione `Ctrl+C` no terminal da execução. O CSS e JavaScript não dependem de serviços de terceiros.

## CI e qualidade (Commit 04)

O workflow `.github/workflows/ci.yml` executa em Pull Requests direcionados a `main` ou `develop`, usando Java 21 Temurin, cache Maven e `./mvnw clean verify`. Os testes de integração iniciam PostgreSQL real via Testcontainers; o runner hospedado do GitHub fornece Docker. O wrapper recebe permissão de execução no runner antes do uso.

O JaCoCo gera relatórios HTML e XML em `target/site/jacoco/` durante `verify`. O XML é usado pelo SonarScanner for Maven quando a integração estiver configurada. As metas de 80% de linhas e 70% de branches são acompanhadas como referência e não bloqueiam o build até o gate previsto para o Commit 14.

Para habilitar a análise SonarCloud, um administrador do repositório deve configurar o secret `SONAR_TOKEN` e as variáveis `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY` com os identificadores confirmados no SonarCloud. O workflow omite a análise quando qualquer item estiver ausente e não expõe o token a PRs de forks. Quando a análise é iniciada, erros do scanner causam falha no step. A integração ainda depende de configuração e execução remotas; sua operacionalidade não é presumida.

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
