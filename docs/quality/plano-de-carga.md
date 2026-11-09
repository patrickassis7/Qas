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
$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
$listener.Start()
$env:DB_PORT = [string]$listener.LocalEndpoint.Port
$listener.Stop()
$env:DB_URL = "jdbc:postgresql://127.0.0.1:$env:DB_PORT/$env:DB_NAME"
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
$overrideContent = @"
services:
  postgres:
    ports:
      - "127.0.0.1:$($env:DB_PORT):5432"
"@
[System.IO.File]::WriteAllText($composeOverride, $overrideContent, [System.Text.UTF8Encoding]::new($false))
$loadCsv = Join-Path $env:TEMP "or-c15-$runId-users.csv"
$csvLine = '"professor.local@organizacao-recursos.local","' + $env:LOCAL_SEED_PASSWORD + '"'
[System.IO.File]::WriteAllText($loadCsv, $csvLine, [System.Text.UTF8Encoding]::new($false))
Write-Output "Dataset sintético: $loadCsv"
Write-Output "Projeto Compose isolado: $env:COMPOSE_PROJECT_NAME"
docker compose -f docker-compose.yml -f $composeOverride --project-name $env:COMPOSE_PROJECT_NAME up -d --wait postgres
if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL não ficou saudável.' }
docker compose -f docker-compose.yml -f $composeOverride --project-name $env:COMPOSE_PROJECT_NAME ps
.\mvnw.cmd --batch-mode --no-transfer-progress spring-boot:run
```

O último comando mantém a aplicação em execução. Os segredos ficam apenas nas variáveis do processo e no CSV sintético em `%TEMP%`; não copie os valores para argumentos, arquivos do repositório ou logs.

**Terminal 2 — criar um recurso sintético e executar JMeter 5.6.3:**

```powershell
$loadCsv = Read-Host 'Informe o caminho Dataset sintético impresso pelo Terminal 1'
$credentials = Import-Csv -LiteralPath $loadCsv -Header email,password
$adminBody = @{ email = 'admin.local@organizacao-recursos.local'; password = $credentials[0].password } | ConvertTo-Json -Compress
$adminLogin = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/v1/auth/login' -ContentType 'application/json' -Body $adminBody
$adminHeaders = @{ Authorization = "Bearer $($adminLogin.accessToken)" }
$resourceBody = @{ type = 'SALA'; name = "Carga $([guid]::NewGuid().ToString('N'))"; location = 'Ambiente sintético'; capacity = 20; skills = 'carga'; restricted = $false } | ConvertTo-Json -Compress
$resource = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/v1/resources' -Headers $adminHeaders -ContentType 'application/json' -Body $resourceBody
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

Ao terminar, pare o Spring Boot com `Ctrl+C` no Terminal 1 e execute `docker compose -f docker-compose.yml -f $composeOverride --project-name $env:COMPOSE_PROJECT_NAME down` para remover somente os containers e a rede desse projeto; não use `-v`, pois o volume isolado deve ser preservado. Depois que a carga terminar e os agregados forem guardados sem segredo, remova somente o CSV e o override temporários criados por esta execução com `Remove-Item -LiteralPath $loadCsv, $composeOverride`. A aplicação, a carga e os dados de outros projetos não são encerrados nem alterados por esses comandos.

## Análise dos resultados

1. No relatório HTML, anotar p50/p90/p95/p99, throughput e taxa de amostras com falha para a etiqueta `Pesquisar recursos`.
2. Para `Criar reserva concorrente`, contar os códigos no `results.jtl`: deve haver um 201, os demais 409 e nenhum 5xx. O JMeter pode classificar 409 como erro HTTP padrão; nesse cenário, 409 é o resultado esperado e deve ser separado dos erros inesperados.
3. Conferir no PostgreSQL que há uma reserva vinculada ao recurso no intervalo e nenhuma reserva órfã. Registrar a consulta/verificação usada sem exportar dados pessoais.
4. Guardar JTL e HTML fora do Git; se evidências forem anexadas à entrega, incluir somente agregados sem credenciais, tokens, dados pessoais ou logs brutos.
5. Registrar limitações, erros observados e defeitos reproduzidos em `docs/quality/defects.md`; atualizar a RTM com o commit e caminho da evidência segura.

## Resultado desta execução

Execução real contra a aplicação pendente. O JMX 5.6.3 foi carregado e iniciado com um alvo não roteável apenas para validar sua estrutura; não foram medidos percentis nem throughput da aplicação. A tentativa de inicializar um ambiente isolado para a carga foi bloqueada pela revisão automática de comandos (`blocked by policy`), então nenhum container/app foi iniciado por essa tentativa. Um roteiro manual PowerShell para PostgreSQL isolado, execução JMeter, classificação de 409 e consulta de integridade foi preparado acima; ainda não foi executado e não constitui evidência de carga. A análise SonarCloud fica com o integrante responsável por essa integração.
