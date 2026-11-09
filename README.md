# Organização de Recursos

Aplicação acadêmica para consultar e organizar reservas de salas, professores e materiais, com autorização por perfil, prevenção de conflitos, auditoria e relatórios de utilização.

O projeto do semestre é este sistema. O Foot Fanatics mencionado em materiais de aula é somente o case usado para demonstrar técnicas; futebol, torcedores e assinaturas não fazem parte deste produto.

## Tecnologias e arquitetura

- Java 21 e Spring Boot 3.4.5.
- Maven Wrapper; Spring Web, Validation, Data JPA, Spring Security, OAuth2 Resource Server e Actuator.
- OpenAPI/Swagger UI com springdoc.
- PostgreSQL 16, Flyway e Hibernate/JPA.
- JWT HS256 e senhas armazenadas com BCrypt.
- HTML semântico, CSS e JavaScript puro servido pelo Spring Boot em `src/main/resources/static/`; sem servidor frontend separado.
- JUnit 5, PostgreSQL via Testcontainers, WireMock, JaCoCo e JMeter 5.6.3.
- GitHub Actions para CI em Pull Requests.

O backend é um monólito modular organizado por domínio (`auth`, `resource`, `reservation`, `approval`, `maintenance`, `usage`, `audit`, `notification`, `report` e `shared`). Controllers delegam regras aos serviços de aplicação/domínio. O frontend chama `/api/v1` no mesmo host; o backend continua sendo a autoridade de autenticação e autorização.

## Funcionalidades e perfis

| Perfil | Acesso implementado |
|---|---|
| **SOLICITANTE** | Consulta e pesquisa recursos, verifica disponibilidade, cria reservas e consulta/altera/cancela as reservas permitidas ao próprio usuário. Não aprova a própria solicitação restrita. |
| **RESPONSAVEL** | Atua nas reservas dos recursos sob sua responsabilidade: aprovação/rejeição e operações de retirada/devolução/não comparecimento, conforme as regras do backend; também consulta relatório de utilização. |
| **ADMINISTRADOR** | Gerencia recursos e bloqueios de manutenção, consulta auditoria, aprova/acompanha operações autorizadas e consulta relatórios. |

Todas as rotas `/api/v1/**`, exceto o login, exigem JWT. Leituras de recursos e disponibilidade são autenticadas; criação, alteração e desativação de recursos e operações de manutenção exigem `ADMINISTRADOR`. A API não oferece administração de usuários nem uma fila de aprovações para responsáveis. A interface pode exigir UUID conhecido para algumas operações de responsável; o backend valida toda ação.

Recursos comuns podem ser aprovados automaticamente. Recursos restritos exigem decisão autorizada. Os intervalos são semiabertos `[início, fim)`; sobreposições e corridas de reserva são rejeitadas com HTTP 409. Erros de API usam `code`, `message`, `fieldErrors` e `traceId`.

Eventos de auditoria são append-only: a API não oferece atualização ou exclusão de eventos. Notificações usam uma porta com adaptador HTTP simulado/configurável e falhas externas não revertem a operação de negócio; não há provedor de produção configurado.

### Endpoints principais

| Uso | Endpoint | Acesso/resumo |
|---|---|---|
| Login | `POST /api/v1/auth/login` | Público; recebe e-mail e senha, retorna `accessToken`. |
| Pesquisa de recursos | `GET /api/v1/resources` | JWT; filtros `type`, `location`, `minimumCapacity`, `skill`, `status`, `page` e `size`. |
| Disponibilidade | `GET /api/v1/resources/availability?startAt=...&endAt=...` | JWT; horários ISO-8601. |
| Consultar recurso | `GET /api/v1/resources/{id}` | JWT. |
| Gerir recursos | `POST /api/v1/resources`, `PATCH /api/v1/resources/{id}`, `POST /api/v1/resources/{id}/deactivate` | `ADMINISTRADOR`. |
| Manutenção | `POST /api/v1/maintenance-blocks`, `GET /api/v1/maintenance-blocks/resource/{resourceId}`, `POST /api/v1/maintenance-blocks/{id}/cancel` | `ADMINISTRADOR`. |
| Reservas | `POST/GET /api/v1/reservations`, `GET/PATCH /api/v1/reservations/{id}`, `POST /api/v1/reservations/{id}/cancel` | JWT; o serviço valida proprietário, papel, estado e conflito. |
| Aprovação e uso | `POST /api/v1/reservations/{id}/approve`, `/reject`, `/start`, `/complete`, `/no-show` | JWT; o serviço verifica papel, responsável pelo recurso e transição permitida. |
| Auditoria | `GET /api/v1/audit-events` | `ADMINISTRADOR`. |
| Relatório de utilização | `GET /api/v1/reports/utilization?startAt=...&endAt=...&resourceId=...` | `RESPONSAVEL` ou `ADMINISTRADOR`; `resourceId` é opcional. |

## Interface web

A interface em <http://localhost:8080/> oferece login, dashboard com informações consultadas na API, pesquisa de recursos, disponibilidade por período, criação/acompanhamento de reservas e, conforme o papel e os endpoints disponíveis, gestão de recursos e ações de responsável/administrador. Ela não inventa dados ou respostas de sucesso. Datas/horas são enviadas no formato ISO-8601 esperado pela API.

O token JWT é mantido em `sessionStorage` na aba atual e removido no logout ou após HTTP 401; não é escrito em logs. Mensagens de erro apresentam o contrato da API, incluindo `message` e `fieldErrors`. Dados recebidos são inseridos como texto no DOM, não interpretados como HTML. A implementação está integrada, mas a inspeção visual completa em 320, 375, 390, 768, 1024 e 1440 px ainda não foi comprovada.

## Pré-requisitos

- JDK 21 (verifique com `java -version`).
- Docker Engine funcionando e Docker Compose v2 (`docker version`, `docker compose version`).
- PowerShell no Windows; em Bash, OpenSSL para gerar a chave aleatória do exemplo.
- Maven global não é necessário: use `mvnw.cmd` no Windows ou `./mvnw` em Bash.

Os testes de integração usam Docker/Testcontainers. O banco do Compose usa volume nomeado, separado pelo nome do projeto Compose. Ao encerrar, `docker compose down` preserva esse volume; não use `down -v` se quiser manter os dados locais.

## Configurar PostgreSQL e iniciar localmente

O perfil `local` precisa ser ativado explicitamente. O Compose exige `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD`; `DB_PORT` é opcional e usa `5432` por padrão. O backend usa `DB_URL` (padrão `jdbc:postgresql://localhost:5432/organizacao_recursos`) e exige `JWT_SECRET` no ambiente do processo. A aplicação não lê `.env` automaticamente: Compose usa `.env` para interpolação, mas o terminal que inicia Spring Boot precisa receber as variáveis do processo.

O [`.env.example`](.env.example) contém configuração compartilhada com AIOX. Preserve esse arquivo; não o substitua. O [`.gitignore`](.gitignore) ignora `.env`. Os comandos abaixo definem as variáveis na sessão atual e não imprimem senhas nem tokens. Use senhas sintéticas locais. Se a porta `5432` estiver ocupada, escolha outra `DB_PORT` antes de iniciar e mantenha o mesmo valor em `DB_URL`.

### Windows PowerShell

Execute na raiz do repositório, na mesma janela PowerShell:

```powershell
$env:COMPOSE_PROJECT_NAME = 'organizacao-recursos-local'
$env:DB_NAME = 'organizacao_recursos'
$env:DB_USERNAME = 'or_local'
if ([string]::IsNullOrWhiteSpace($env:DB_PORT)) { $env:DB_PORT = '5432' }
$dbPasswordSecure = Read-Host 'Senha local do PostgreSQL' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $dbPasswordSecure).Password
$env:DB_URL = "jdbc:postgresql://127.0.0.1:$env:DB_PORT/$env:DB_NAME"
$env:SPRING_PROFILES_ACTIVE = 'local'

$jwtBytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($jwtBytes) } finally { $rng.Dispose() }
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)

$seedPasswordSecure = Read-Host 'Senha sintética para os usuários locais' -AsSecureString
$env:LOCAL_SEED_PASSWORD = [System.Net.NetworkCredential]::new('', $seedPasswordSecure).Password

docker compose up -d postgres
docker compose ps
```

Antes de iniciar Spring Boot, confirme que `postgres` aparece como `healthy`. A chave `JWT_SECRET` gerada é Base64 de 48 bytes aleatórios. O código aceita chave Base64 padrão que represente pelo menos 32 bytes, lida exclusivamente do ambiente do processo. Não a passe em argumento de linha de comando, propriedade `-D`, arquivo versionado ou log.

### Bash (Linux, macOS ou Git Bash com Docker disponível)

Execute na raiz do repositório, na mesma sessão:

```bash
export COMPOSE_PROJECT_NAME=organizacao-recursos-local
export DB_NAME=organizacao_recursos
export DB_USERNAME=or_local
export DB_PORT="${DB_PORT:-5432}"
read -r -s -p 'Senha local do PostgreSQL: ' DB_PASSWORD; printf '\n'
export DB_PASSWORD
export DB_URL="jdbc:postgresql://127.0.0.1:${DB_PORT}/${DB_NAME}"
export SPRING_PROFILES_ACTIVE=local
export JWT_SECRET="$(openssl rand -base64 48 | tr -d '\n')"
read -r -s -p 'Senha sintética para os usuários locais: ' LOCAL_SEED_PASSWORD; printf '\n'
export LOCAL_SEED_PASSWORD

docker compose up -d postgres
docker compose ps
```

Espere `postgres` ficar `healthy` antes de iniciar a aplicação. O segredo gerado não é impresso; mantenha-o somente no ambiente local.

### Testar e iniciar a aplicação

Com Docker disponível, execute a suíte na mesma sessão que contém as variáveis. O Maven usa PostgreSQL isolado por Testcontainers para os testes:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

Em Bash:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Depois, inicie o backend com o perfil `local`, ainda na mesma sessão:

```powershell
.\mvnw.cmd spring-boot:run
```

```bash
./mvnw spring-boot:run
```

O seed local cria apenas contas ausentes, com o mesmo `LOCAL_SEED_PASSWORD` escolhido, e não troca a senha de contas já existentes. Sem essa variável, novos usuários recebem hash aleatório desconhecido. Para testar login, defina-a antes da primeira inicialização de uma base local nova. O seed é restrito ao perfil `local`; não use esse perfil em produção.

Contas sintéticas sem senhas versionadas:

| Perfil | E-mail local |
|---|---|
| Solicitante | `professor.local@organizacao-recursos.local` |
| Responsável | `responsavel.local@organizacao-recursos.local` |
| Administrador | `admin.local@organizacao-recursos.local` |

### URLs locais

- Interface: <http://localhost:8080/>
- Health: <http://localhost:8080/actuator/health>
- OpenAPI: <http://localhost:8080/v3/api-docs>
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>

Para encerrar, pressione `Ctrl+C` no terminal do Spring Boot e execute `docker compose down` no terminal com as variáveis de ambiente. Isso remove o container e a rede do projeto Compose, mantendo o volume PostgreSQL. Ao terminar de usar a sessão, remova as variáveis secretas do ambiente do processo:

```powershell
Remove-Item Env:DB_PASSWORD, Env:JWT_SECRET, Env:LOCAL_SEED_PASSWORD -ErrorAction SilentlyContinue
```

```bash
unset DB_PASSWORD JWT_SECRET LOCAL_SEED_PASSWORD
```

Não registre nem compartilhe o valor de `JWT_SECRET` ou senhas.

### Variáveis de ambiente

| Variável | Obrigatória? | Uso/default |
|---|---|---|
| `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | Sim para Compose e conexão local | Nome do banco e credenciais sintéticas locais. |
| `DB_PORT` | Não | Porta host do Compose; padrão `5432`. A URL JDBC deve usar essa mesma porta. |
| `DB_URL` | Recomendada no perfil `local` | JDBC, por exemplo `jdbc:postgresql://127.0.0.1:5432/organizacao_recursos`. |
| `SPRING_PROFILES_ACTIVE` | Sim para executar localmente | Defina explicitamente como `local`; não existe ativação local automática. |
| `JWT_SECRET` | Sim para iniciar a aplicação | Base64 padrão de pelo menos 32 bytes aleatórios criptograficamente seguros; fonte obrigatória é o ambiente do processo. |
| `LOCAL_SEED_PASSWORD` | Opcional | Senha para contas novas do seed `local`; sem ela o hash é aleatório e desconhecido. Nunca substitui a senha de conta existente. |
| `JWT_ISSUER`, `JWT_TTL_MINUTES` | Não | Padrões: `organizacao-recursos` e 30 minutos; o TTL permitido é limitado pelo código. |
| `NOTIFICATION_BASE_URL` | Não | Padrão `http://localhost:8099`, adaptador HTTP simulado/configurável. Timeouts: `NOTIFICATION_CONNECT_TIMEOUT_MS` e `NOTIFICATION_READ_TIMEOUT_MS`, padrão 500 ms. |

## Qualidade, CI e cobertura

O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) executa `clean verify` em Pull Requests destinados a `main` ou `develop`, com Java 21 Temurin e cache Maven. Testcontainers precisa de Docker no runner. O build falha quando os limites JaCoCo configurados não são atingidos: **80% de linhas e 70% de branches**. `verify` gera relatórios XML e HTML em `target/site/jacoco/`.

Na última validação integrada registrada, executada em `d4b02bb` após os merges dos PRs #1 e #2, foram aprovados **202 testes**, com 0 falhas, 0 erros e 0 ignorados. JaCoCo mediu **93,64% de linhas (1.074/1.147)** e **78,57% de branches (220/280)**. A atualização atual do README é documental; esses resultados são a última validação registrada, não uma execução desta revisão nem prova de 100% dos requisitos.

Os checks GitHub Actions `CI`/`verify` foram comprovados nos heads dos PRs #1 (`f37e958`) e #2 (`3705037`): [execução PR #1](https://github.com/patrickassis7/Qas/actions/runs/42566300669) · [execução PR #2](https://github.com/patrickassis7/Qas/actions/runs/42575378609). Não foi executado CI para esta branch documental após as alterações do README.

**SonarCloud está pendente** e é responsabilidade de outro integrante. O workflow só inicia análise quando o secret `SONAR_TOKEN` e as variáveis `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY` estiverem configurados com valores confirmados no GitHub/SonarCloud. Não há análise nem Quality Gate SonarCloud declarado como executado.

## Teste de carga

O plano JMeter 5.6.3 está em [`docs/quality/plano-de-carga.md`](docs/quality/plano-de-carga.md); o resumo sanitizado da execução real está em [`docs/quality/evidencias/jmeter-commit15-2026-11-09.md`](docs/quality/evidencias/jmeter-commit15-2026-11-09.md). A execução local registrada produziu 55.812 amostras: 55.792 pesquisas HTTP 200, 12 logins HTTP 200, uma reserva HTTP 201 e sete conflitos HTTP 409 esperados; não houve HTTP 500 nem reservas órfãs. Para pesquisa: p50 4 ms, p95 7 ms, p99 9 ms e throughput 933,9918 req/s; throughput total 931,8929 req/s. Os arquivos brutos JTL/HTML foram mantidos fora do Git. É uma execução local única, não um SLO nem uma garantia de capacidade em produção; a meta de latência ainda precisa ser acordada com o docente.

## Documentação do projeto

| Documento | Conteúdo |
|---|---|
| [RTM oficial](docs/RTM.md) | Requisitos RF-01–RF-14, riscos, testes, evidências e status. [`RTM.md`](RTM.md) na raiz é somente um encaminhamento compatível. |
| [Relatório final de qualidade](docs/quality/relatorio-final.md) | Resultados por revisão, CI, cobertura, carga e riscos residuais. |
| [Plano de testes](docs/quality/plano-de-testes.md) | Estratégia automatizada e limitações. |
| [Registro de defeitos](docs/quality/defects.md) | Defeitos encontrados, correções e retestes. |
| [Plano e evidência JMeter](docs/quality/plano-de-carga.md) · [resumo](docs/quality/evidencias/jmeter-commit15-2026-11-09.md) | Procedimento seguro e resultados agregados. |
| [Decisões arquiteturais](docs/adr/) | ADRs 0001–0003, incluindo decisões D1–D8. |
| [Diagramas](docs/architecture/) | Contexto, componentes e três sequências críticas. |
| [Roteiro de demonstração](docs/apresentacao/roteiro-demo.md) | Passos, perfis, pré-requisitos e resultados esperados. |
| [Story do frontend](docs/stories/1.6-commit-16-frontend-rf13.md) | Escopo, critérios, implementação e evidências de RF-13. |

## Limitações conhecidas

- A interface está integrada, mas ainda falta validação visual completa nas larguras 320, 375, 390, 768, 1024 e 1440 px.
- SonarCloud não foi executado.
- Não existe meta/SLO de latência acordada com o docente; resultados JMeter caracterizam somente uma execução local.
- A API não fornece fila de aprovações nem endpoint de consulta de usuários; algumas operações da interface exigem UUID conhecido.
- A integração de notificação usa adaptador HTTP simulado/configurável; não há provedor externo de produção validado.
- As decisões acadêmicas D1–D8 estão implementadas/documentadas, mas a validação formal pelo docente ainda é pendente conforme os ADRs.
- A RTM registra cobertura parcial onde faltam evidências; cobertura JaCoCo não equivale à cobertura funcional dos requisitos nem à ausência de vulnerabilidades.

O sistema não recebe aprovação global nesta documentação enquanto essas pendências permanecerem abertas. Veja a [RTM](docs/RTM.md) e o [relatório de qualidade](docs/quality/relatorio-final.md) para os status detalhados.
