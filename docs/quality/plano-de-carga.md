# Plano de carga — Commit 15

## Objetivo e limites

Medir a consulta paginada de recursos e submeter várias reservas ao mesmo recurso e intervalo. A carga serve para caracterizar este ambiente e comprovar a regra de concorrência; não é certificação de produção.

Não existe SLO de latência acordado com o docente. Registrar p50, p95, p99, throughput e códigos HTTP observados, sem declarar que um limite de latência foi aprovado. O cenário deve usar somente a aplicação local e um PostgreSQL descartável isolado; nunca aponte o teste para produção ou para dados de terceiros.

## Dataset e segredo

- O plano usa um usuário `SOLICITANTE` fictício ativo e um recurso ativo já criado no banco local.
- `docs/quality/jmeter/load-user.csv.example` é apenas um modelo, sem senha real. Crie a cópia de execução fora do repositório, por exemplo em `%TEMP%\or-jmeter-users.csv`, com credenciais sintéticas do perfil local.
- Passe o caminho do CSV por `-JcredentialsFile=...`; não forneça a senha em argumentos, propriedades, logs, JTL ou capturas compartilhadas.
- Passe o UUID do recurso e os Instants UTC por propriedades JMeter. O intervalo deve ser futuro e livre para que o teste confronte todas as threads na mesma janela.
- O plano não cria, limpa ou exclui usuários, recursos, reservas, bancos ou volumes.

## Cenários

| Cenário | Configuração inicial | Critério observável |
|---|---|---|
| Pesquisa de recursos | 4 threads, ramp-up 5 s, duração padrão 60 s; ajustável via propriedades | HTTP 200 em `GET /api/v1/resources?page=0&size=20`; coletar latência e throughput |
| Reserva concorrente | 8 threads fixas, uma iteração, barreira de sincronização e mesmo `resourceId`, `startAt` e `endAt` | Exatamente 1 HTTP 201, 7 HTTP 409, nenhum HTTP 500 e sem reservas sem vínculo em `reservation_resources` |

Os valores são um perfil inicial conservador, não uma meta de capacidade. O grupo de colisão e sua barreira usam as mesmas oito threads fixas; altere ambos em conjunto se ajustar esse cenário. Para comparar execuções, registrar CPU/memória, versão Java, PostgreSQL, máquina, número de threads, ramp-up, duração, dataset e commit.

## Execução

Use Apache JMeter 5.6.3 ou versão posterior compatível com Java 21. Inicie a aplicação em perfil `local` com PostgreSQL isolado, configure um usuário local sintético e crie um recurso sem restrição. Não versionar os valores usados.

No PowerShell, defina os Instants antes da execução e gere resultados fora do repositório:

```powershell
$loadStart = [DateTime]::UtcNow.AddMinutes(5).ToString('yyyy-MM-ddTHH:mm:ssZ')
$loadEnd = [DateTime]::UtcNow.AddMinutes(20).ToString('yyyy-MM-ddTHH:mm:ssZ')
$runDir = Join-Path $env:TEMP ('or-jmeter-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $runDir | Out-Null
jmeter -n -t docs/quality/jmeter/organizacao-recursos-load.jmx `
  -Jhost=localhost -Jport=8080 `
  -JcredentialsFile="$env:TEMP\or-jmeter-users.csv" `
  -JresourceId='<UUID_DO_RECURSO_ATIVO>' `
  "-JstartAt=$loadStart" "-JendAt=$loadEnd" `
  -JreadThreads=4 -JreadRampUp=5 -JreadDuration=60 `
  -l "$runDir\results.jtl" -e -o "$runDir\report"
```

O CSV precisa conter uma linha de usuário de teste no formato `email,password`. O caminho, senha, UUID e valores temporais são parâmetros locais; não os registre em shell transcript público. O plano usa o endpoint HTTP de login para obter um token por thread e não grava o token no JTL.

### Execução manual isolada no Windows/PowerShell

A execução automatizada local foi bloqueada pela revisão do ambiente. Para fazê-la manualmente, use dois terminais PowerShell na raiz do repositório. O primeiro cria nomes aleatórios para o projeto Compose e para o dataset, gera segredos criptográficos sem imprimi-los, inicia o PostgreSQL sob um projeto/volume exclusivo e com a porta publicada somente em `127.0.0.1`, e mantém o Spring Boot em primeiro plano. O segundo cria um recurso sintético, executa o JMeter e confere os resultados. Não reutilize o nome de um projeto Compose existente.

**Terminal 1 — criar o ambiente e iniciar o backend:**

```powershell
$runId = [guid]::NewGuid().ToString('N')
$env:COMPOSE_PROJECT_NAME = "or-c15-$runId"
$env:DB_NAME = 'or_commit15_load'
$env:DB_USERNAME = 'or_load'
$env:SPRING_PROFILES_ACTIVE = 'local'
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$dbBytes = New-Object byte[] 32
$seedBytes = New-Object byte[] 32
$jwtBytes = New-Object byte[] 48
$rng.GetBytes($dbBytes)
$rng.GetBytes($seedBytes)
$rng.GetBytes($jwtBytes)
$rng.Dispose()
$env:DB_PASSWORD = [Convert]::ToBase64String($dbBytes)
$env:LOCAL_SEED_PASSWORD = [Convert]::ToBase64String($seedBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$composeOverride = Join-Path $env:TEMP "or-c15-$runId-compose.yml"
$composeContent = @'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${DB_NAME:?DB_NAME must be set}
      POSTGRES_USER: ${DB_USERNAME:?DB_USERNAME must be set}
      POSTGRES_PASSWORD: ${DB_PASSWORD:?DB_PASSWORD must be set}
    ports:
      - "127.0.0.1::5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
      interval: 5s
      timeout: 5s
      retries: 10
volumes:
  postgres_data:
'@
[System.IO.File]::WriteAllText($composeOverride, $composeContent, [System.Text.UTF8Encoding]::new($false))
$loadCsv = Join-Path $env:TEMP "or-c15-$runId-users.csv"
$csvLine = '"professor.local@organizacao-recursos.local","' + $env:LOCAL_SEED_PASSWORD + '"'
[System.IO.File]::WriteAllText($loadCsv, $csvLine, [System.Text.UTF8Encoding]::new($false))
Write-Output "Dataset sintético: $loadCsv"
Write-Output "Projeto Compose isolado: $env:COMPOSE_PROJECT_NAME"
docker compose -f $composeOverride --project-name $env:COMPOSE_PROJECT_NAME up -d --wait postgres
if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL não ficou saudável.' }
$publishedPort = docker compose -f $composeOverride --project-name $env:COMPOSE_PROJECT_NAME port postgres 5432
if ($LASTEXITCODE -ne 0 -or -not $publishedPort) { throw 'Não foi possível consultar a porta publicada.' }
$env:DB_PORT = [string]($publishedPort -split ':')[-1]
$env:DB_URL = "jdbc:postgresql://127.0.0.1:$env:DB_PORT/$env:DB_NAME"
docker compose -f $composeOverride --project-name $env:COMPOSE_PROJECT_NAME ps
.\mvnw.cmd --batch-mode --no-transfer-progress spring-boot:run
```

O último comando mantém a aplicação em execução. Os segredos ficam apenas nas variáveis do processo e no CSV sintético em `%TEMP%`; não copie os valores para argumentos, arquivos do repositório ou logs.

**Terminal 2 — criar um recurso sintético e executar JMeter 5.6.3:**

```powershell
$loadCsv = Read-Host 'Informe o caminho Dataset sintético impresso pelo Terminal 1'
$credentials = @(Import-Csv -LiteralPath $loadCsv -Header email,password)
$adminBody = @{ email = 'admin.local@organizacao-recursos.local'; password = $credentials[0].password } | ConvertTo-Json -Compress
$adminLogin = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/v1/auth/login' -ContentType 'application/json' -Body $adminBody
$adminHeaders = @{ Authorization = "Bearer $($adminLogin.accessToken)" }
$resourceBody = @{ type = 'SALA'; name = "Carga $([guid]::NewGuid().ToString('N'))"; location = 'Ambiente sintetico'; capacity = 20; skills = 'carga'; restricted = $false } | ConvertTo-Json -Compress
$resourceBytes = [System.Text.Encoding]::UTF8.GetBytes($resourceBody)
$resource = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/v1/resources' -Headers $adminHeaders -ContentType 'application/json; charset=utf-8' -Body $resourceBytes
$resourceId = $resource.id
if (-not $resourceId) { throw 'A API não retornou o ID do recurso sintético.' }
$health = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health'
if ($health.status -ne 'UP') { throw 'Health check não está UP.' }
$loadStart = [DateTime]::UtcNow.AddMinutes(5).ToString('yyyy-MM-ddTHH:mm:ssZ')
$loadEnd = [DateTime]::UtcNow.AddMinutes(20).ToString('yyyy-MM-ddTHH:mm:ssZ')
$runDir = Join-Path $env:TEMP ('or-jmeter-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $runDir | Out-Null
$jmeter = Join-Path $env:TEMP 'apache-jmeter-5.6.3\bin\jmeter.bat'
& $jmeter -n -t 'docs/quality/jmeter/organizacao-recursos-load.jmx' `
  -Jhost=localhost -Jport=8080 `
  "-JcredentialsFile=$loadCsv" "-JresourceId=$resourceId" `
  "-JstartAt=$loadStart" "-JendAt=$loadEnd" `
  -JreadThreads=4 -JreadRampUp=5 -JreadDuration=60 `
  -Jjmeter.save.saveservice.response_data=false `
  -Jjmeter.save.saveservice.samplerData=false `
  -Jjmeter.save.saveservice.print_field_names=true `
  -l (Join-Path $runDir 'results.jtl') -e -o (Join-Path $runDir 'report')
if ($LASTEXITCODE -ne 0) { throw 'JMeter terminou com erro.' }
Write-Output "Relatório JMeter: $(Join-Path $runDir 'report\index.html')"
Write-Output "Resultados JTL: $(Join-Path $runDir 'results.jtl')"
```

O token do administrador permanece somente na variável de sessão PowerShell; não o imprima. No relatório HTML, anote p50/p95/p99 e throughput da amostra `Pesquisar recursos`. Para classificar a corrida sem tratar 409 esperado como falha real:

```powershell
$jtlPath = Join-Path $runDir 'results.jtl'
$samples = Import-Csv -LiteralPath $jtlPath
$collisions = @($samples | Where-Object label -eq 'Criar reserva concorrente')
$collisions | Group-Object responseCode | Select-Object Name,Count
$unexpected = @($collisions | Where-Object { $_.responseCode -notin @('201', '409') })
if ($collisions.Count -ne 8 -or @($collisions | Where-Object responseCode -eq '201').Count -ne 1 -or @($collisions | Where-Object responseCode -eq '409').Count -ne 7 -or $unexpected.Count -ne 0) {
    throw 'Resultado de concorrência divergente: confira JTL, erros de transporte e códigos HTTP.'
}
```

Depois, verifique no banco isolado a reserva e a ausência de reservas sem recurso. Execute no Terminal 2, informando o nome não secreto do projeto Compose impresso pelo Terminal 1:

```powershell
$composeProject = Read-Host 'Informe o projeto Compose isolado impresso pelo Terminal 1'
$dbUsername = 'or_load'
$dbName = 'or_commit15_load'
$sql = "SELECT COUNT(DISTINCT r.id) AS reservas_na_janela FROM reservations r JOIN reservation_resources rr ON rr.reservation_id = r.id WHERE rr.resource_id = '$resourceId'::uuid AND r.start_at = '$loadStart'::timestamptz AND r.end_at = '$loadEnd'::timestamptz; SELECT COUNT(*) AS reservas_orfas FROM reservations r WHERE NOT EXISTS (SELECT 1 FROM reservation_resources rr WHERE rr.reservation_id = r.id);"
$dbContainer = docker ps -q --filter "label=com.docker.compose.project=$composeProject" --filter 'label=com.docker.compose.service=postgres'
if (-not $dbContainer) { throw 'Container PostgreSQL do projeto isolado não foi encontrado.' }
docker exec $dbContainer psql -U $dbUsername -d $dbName -c $sql
```

Ao terminar, pare o Spring Boot com `Ctrl+C` no Terminal 1 e execute `docker compose -f $composeOverride --project-name $env:COMPOSE_PROJECT_NAME down` para remover somente os containers e a rede desse projeto; não use `-v`, pois o volume isolado deve ser preservado. Depois que a carga terminar e os agregados forem guardados sem segredo, remova somente o CSV e o arquivo Compose temporários criados por esta execução com `Remove-Item -LiteralPath $loadCsv, $composeOverride`. A aplicação, a carga e os dados de outros projetos não são encerrados nem alterados por esses comandos.

## Análise dos resultados

1. No relatório HTML, anotar p50/p90/p95/p99, throughput e taxa de amostras com falha para a etiqueta `Pesquisar recursos`.
2. Para `Criar reserva concorrente`, contar os códigos no `results.jtl`: deve haver um 201, os demais 409 e nenhum 5xx. O JMeter pode classificar 409 como erro HTTP padrão; nesse cenário, 409 é o resultado esperado e deve ser separado dos erros inesperados.
3. Conferir no PostgreSQL que há uma reserva vinculada ao recurso no intervalo e nenhuma reserva órfã. Registrar a consulta/verificação usada sem exportar dados pessoais.
4. Guardar JTL e HTML fora do Git; se evidências forem anexadas à entrega, incluir somente agregados sem credenciais, tokens, dados pessoais ou logs brutos.
5. Registrar limitações, erros observados e defeitos reproduzidos em `docs/quality/defects.md`; atualizar a RTM com o commit e caminho da evidência segura.

## Resultado desta execução

Execução real concluída em 2026-11-09, contra o perfil `local` e PostgreSQL 16.15 em um projeto Compose/volume exclusivo, com porta publicada somente em `127.0.0.1`. O ambiente foi iniciado manualmente após o bloqueio anterior da orquestração automatizada; o roteiro PowerShell acima registra o procedimento que funcionou. JMeter 5.6.3 executou 55.812 amostras HTTP, todas concluídas com resposta: 55.792 pesquisas de recursos (todas 200, 0 erros, p50 4 ms, p95 7 ms, p99 9 ms, média 4,09 ms e throughput 933,99 req/s), 12 logins (todos 200) e 8 reservas concorrentes (1 HTTP 201 e 7 HTTP 409 esperados). O JMeter marca os 7 conflitos como `success=false`; não houve outra falha real, falha de transporte nem HTTP 500. No banco, houve uma reserva `APROVADA`, vinculada ao recurso, e zero reservas órfãs. O relatório HTML e o JTL foram mantidos em `%TEMP%` e não foram versionados.

Ambiente observado: Windows 11 Home, AMD Ryzen 7 5700X, 15,9 GB de RAM, Java 21.0.13, Docker Engine 29.9.2 e PostgreSQL 16.15. Os resultados caracterizam somente esta execução local; não constituem SLO ou garantia de capacidade de produção. SonarCloud permanece com o integrante responsável.

Durante a preparação, duas portas escolhidas previamente falharam com `address already in use` apesar de não haver listener visível. A execução prosseguiu após deixar o Docker alocar a porta publicada. A primeira tentativa de criar o recurso pelo PowerShell recebeu HTTP 400 `INVALID_REQUEST`; a repetição com corpo UTF-8 explícito foi aceita. Não houve alteração de código nem defeito de produto associado a esses dois problemas de preparação.
