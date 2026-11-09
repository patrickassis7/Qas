# Roteiro de demonstração

## Objetivo e duração

Roteiro de aproximadamente 12–15 minutos para demonstrar autenticação por perfil, pesquisa de recursos, reservas, conflitos, manutenção, aprovação, retirada/devolução, auditoria e relatório de utilização. Os dados e horários são sintéticos; a execução deve usar somente uma instância local isolada.

## Pré-requisitos

- Java 21, Docker Desktop com Docker Compose e o Maven Wrapper do repositório.
- PostgreSQL local iniciado pelo Compose com nome de projeto e volume próprios. Não reutilize banco de produção, de aula ou de outro projeto; não remova volumes existentes.
- Backend executando na porta 8080 com `SPRING_PROFILES_ACTIVE=local` e variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` configuradas no processo.
- `JWT_SECRET` em Base64 representando ao menos 32 bytes aleatórios gerados por fonte criptográfica. Não coloque segredos em arquivos versionados nem em argumentos ou logs.
- Para criar as contas sintéticas na primeira inicialização, configure `LOCAL_SEED_PASSWORD` antes de iniciar a aplicação. O seed é idempotente e não troca a senha de usuários que já existam. Se as contas existentes não tiverem uma senha conhecida, use outro banco isolado novo; não apague volumes.
- Verifique `http://localhost:8080/actuator/health` antes da apresentação e confirme `status: UP`.

As três identidades são criadas somente no perfil `local` e usam a mesma senha de seed:

| Perfil | E-mail sintético |
|---|---|
| SOLICITANTE | `professor.local@organizacao-recursos.local` |
| RESPONSAVEL | `responsavel.local@organizacao-recursos.local` |
| ADMINISTRADOR | `admin.local@organizacao-recursos.local` |

Não há senha padrão no código. Digite a senha somente nos prompts protegidos abaixo; não a registre em transcrição do terminal, captura ou documentação.

## Demonstração no navegador

O frontend está na branch `feat/16-frontend-rf13`, associada ao [PR #1](https://github.com/patrickassis7/Qas/pull/1) para `develop`. No momento deste roteiro, o PR continua aberto como Draft e os arquivos estáticos ainda não fazem parte desta branch de documentação. Para demonstrar pela web, execute a aplicação a partir da branch do frontend revisada ou depois que ela for integrada; só então abra `http://localhost:8080/`. Não é necessário alterar este roteiro para afirmar que o frontend foi integrado.

1. **Perfil: ADMINISTRADOR. Ação:** entre com `admin.local@organizacao-recursos.local` e abra **Gestão de recursos**. Cadastre uma sala (localização e capacidade positiva), um professor (competências preenchidas) e um material restrito (marque restrição e informe o UUID do responsável). Use o UUID obtido localmente na preparação PowerShell abaixo. **Resultado esperado:** recursos sintéticos persistidos e exibidos na gestão. **Requisitos:** RF-01, RF-02 e RF-13.
2. **Perfil: SOLICITANTE. Ação:** saia da conta administrativa, entre com `professor.local@organizacao-recursos.local`, abra **Recursos** e pesquise separadamente por Sala, Professor e Material usando os filtros disponíveis. Depois abra **Disponibilidade**, informe início e fim, consulte os horários e envie uma reserva para um recurso disponível. **Resultado esperado:** resultados vindos da API; a disponibilidade é consultiva e o envio é validado novamente pelo backend. **Requisitos:** RF-01, RF-03, RF-04 e RF-13.
3. **Perfil: SOLICITANTE. Ação:** consulte **Minhas reservas** e tente repetir a reserva. A lista de disponibilidade pode deixar de oferecer o recurso já ocupado; para demonstrar especificamente o conflito de sobreposição, use a chamada repetida do roteiro PowerShell/API abaixo com o mesmo recurso e intervalo. **Resultado esperado:** mensagem compreensível de conflito e HTTP 409 na chamada de API. **Requisitos:** RF-04, RF-05 e RF-13.
4. **Perfil: RESPONSAVEL ou ADMINISTRADOR, conforme vínculo/autorização. Ação:** em **Acompanhamento**, informe o UUID da reserva e escolha aprovar, rejeitar, registrar retirada/início ou registrar devolução/conclusão. Para o perfil RESPONSAVEL, obtenha o UUID da reserva por canal institucional autorizado; a interface não oferece fila de solicitações nem consulta reservas de terceiros para esse perfil. **Resultado esperado:** a transição permitida é aplicada; perfil não autorizado recebe erro da API. **Requisitos:** RF-01, RF-07, RF-09 e RF-13.

**Limites da interface atual:** não há tela própria para criar/consultar bloqueios de manutenção, consultar eventos de auditoria ou visualizar o relatório de utilização. Demonstre essas operações pela API/Swagger, conforme as etapas abaixo; não apresente os endpoints como botões ou telas do frontend. O Swagger está em `http://localhost:8080/swagger-ui/index.html` e as rotas exigem JWT, salvo as rotas públicas documentadas pelo backend. A validação visual completa em 320, 375, 390, 768, 1024 e 1440 px continua pendente; este roteiro não é evidência de ensaio visual.

## Preparação de uma sessão PowerShell

Execute os comandos na raiz do repositório. A função guarda tokens apenas em variáveis da sessão, não os imprime e extrai o UUID do `sub` para associar um recurso ao responsável. A extração do `sub` apenas lê o payload do JWT local; as chamadas à API continuam sendo validadas pelo backend.

```powershell
$BaseUrl = 'http://localhost:8080'
$SeedPassword = Read-Host 'Senha configurada em LOCAL_SEED_PASSWORD' -AsSecureString

function Get-LocalSession([string]$Email, [securestring]$Password) {
    $plain = [System.Net.NetworkCredential]::new('', $Password).Password
    try {
        $body = @{ email = $Email; password = $plain } | ConvertTo-Json -Compress
        $login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/login" `
            -ContentType 'application/json' -Body $body
    } finally {
        $plain = $null
    }

    $segment = $login.accessToken.Split('.')[1].Replace('-', '+').Replace('_', '/')
    switch ($segment.Length % 4) {
        2 { $segment += '==' }
        3 { $segment += '=' }
    }
    $claims = [System.Text.Encoding]::UTF8.GetString(
        [Convert]::FromBase64String($segment)
    ) | ConvertFrom-Json

    [pscustomobject]@{ Token = $login.accessToken; UserId = [guid]$claims.sub }
}

function Invoke-LocalApi {
    param(
        [Parameter(Mandatory)][string]$Method,
        [Parameter(Mandatory)][string]$Path,
        [Parameter(Mandatory)][string]$Token,
        [object]$Body
    )
    $options = @{
        Method = $Method
        Uri = "$BaseUrl$Path"
        Headers = @{ Authorization = "Bearer $Token" }
    }
    if ($PSBoundParameters.ContainsKey('Body')) {
        $options.ContentType = 'application/json'
        $options.Body = $Body | ConvertTo-Json -Depth 8 -Compress
    }
    Invoke-RestMethod @options
}

$solicitante = Get-LocalSession 'professor.local@organizacao-recursos.local' $SeedPassword
$responsavel = Get-LocalSession 'responsavel.local@organizacao-recursos.local' $SeedPassword
$administrador = Get-LocalSession 'admin.local@organizacao-recursos.local' $SeedPassword
$SeedPassword = $null
```

As chamadas de login retornam `accessToken`; a variável `$login` contém temporariamente a resposta dentro da função e não deve ser exibida. Não execute chamadas de sessão sem atribuí-las a uma variável.

## Demonstração

### 1. Pesquisa e disponibilidade — SOLICITANTE

**Ação:** pesquisar recursos e consultar um intervalo futuro. **Resultado esperado:** filtros e disponibilidade retornam dados reais. **Requisitos:** RF-03.

Prepare um intervalo futuro comum a todos os passos:

```powershell
$slotStart = [DateTime]::UtcNow.AddDays(7).AddHours(2)
$slotEnd = $slotStart.AddHours(1)
$startAt = $slotStart.ToString("yyyy-MM-ddTHH:mm:ss'Z'")
$endAt = $slotEnd.ToString("yyyy-MM-ddTHH:mm:ss'Z'")
$startQuery = [uri]::EscapeDataString($startAt)
$endQuery = [uri]::EscapeDataString($endAt)
```

Pesquise salas ativas e consulte a disponibilidade real na API:

```powershell
$location = [uri]::EscapeDataString('Bloco Demo')
$search = Invoke-LocalApi GET "/api/v1/resources?type=SALA&location=$location&status=ATIVO&page=0&size=10" $solicitante.Token
$availability = Invoke-LocalApi GET "/api/v1/resources/availability?startAt=$startQuery&endAt=$endQuery" $solicitante.Token
"Resultados da pesquisa: $($search.totalElements); disponibilidades consultadas: $($availability.Count)"
```

Os filtros existentes são `type`, `location`, `minimumCapacity`, `skill`, `status`, `page` e `size`. A resposta paginada contém `content` e `totalElements`.

### 2. Recursos sintéticos — ADMINISTRADOR

**Ação:** criar recursos de demonstração. **Resultado esperado:** API retorna recursos persistidos; salas exigem localização e capacidade positiva. **Requisitos:** RF-02.

Crie uma sala comum, uma sala para o cenário de manutenção e um material restrito atribuído ao usuário RESPONSAVEL:

```powershell
$suffix = [guid]::NewGuid().ToString('N').Substring(0, 8)
$commonRoom = Invoke-LocalApi POST '/api/v1/resources' $administrador.Token @{
    type = 'SALA'; name = "Demo Sala $suffix"; location = 'Bloco Demo'
    capacity = 24; restricted = $false
}
$maintenanceRoom = Invoke-LocalApi POST '/api/v1/resources' $administrador.Token @{
    type = 'SALA'; name = "Demo Manutenção $suffix"; location = 'Bloco Demo'
    capacity = 12; restricted = $false
}
$restrictedMaterial = Invoke-LocalApi POST '/api/v1/resources' $administrador.Token @{
    type = 'MATERIAL'; name = "Demo Material Restrito $suffix"; location = 'Laboratório Demo'
    restricted = $true; responsibleId = $responsavel.UserId
}
"Sala comum: $($commonRoom.id); material restrito: $($restrictedMaterial.id)"
```

O backend exige localização e capacidade positiva para `SALA`. Cada material é um recurso individual; não há cadastro de quantidade ou controle de estoque.

### 3. Reserva comum aprovada automaticamente e conflito 409 — SOLICITANTE

**Ação:** reservar a sala comum e repetir a mesma requisição no mesmo intervalo. **Resultado esperado:** primeira chamada HTTP 201 (`APROVADA`); repetição HTTP 409. **Requisitos:** RF-04, RF-05 e RF-06.

```powershell
$commonReservation = @{
    startAt = $startAt; endAt = $endAt
    resourceIds = @($commonRoom.id)
    justification = 'Demonstração acadêmica com dados sintéticos'
}
$accepted = Invoke-LocalApi POST '/api/v1/reservations' $solicitante.Token $commonReservation
"Reserva comum: $($accepted.id); estado: $($accepted.status)"
```

Resultado esperado: HTTP 201 e estado `APROVADA`, pois a sala não é restrita. Repita exatamente a mesma solicitação para demonstrar o conflito:

```powershell
try {
    $unexpected = Invoke-LocalApi POST '/api/v1/reservations' $solicitante.Token $commonReservation
    throw 'A API aceitou a sobreposição; interrompa a demonstração e investigue.'
} catch {
    if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 409) {
        'Conflito esperado: HTTP 409.'
    } else {
        throw
    }
}
```

O backend registra `CONFLITO_EVITADO`; não trate o resultado como falha do cenário.

### 4. Bloqueio de manutenção — ADMINISTRADOR e SOLICITANTE

**Ação:** administrador cria bloqueio e solicitante tenta reservar no período bloqueado. **Resultado esperado:** tentativa recusada com HTTP 409; a consulta do bloqueio é restrita a ADMINISTRADOR. **Requisitos:** RF-01 e RF-08.

Crie um bloqueio futuro para a segunda sala e tente reservá-la durante o mesmo intervalo:

```powershell
$maintenance = Invoke-LocalApi POST '/api/v1/maintenance-blocks' $administrador.Token @{
    resourceId = $maintenanceRoom.id; startAt = $startAt; endAt = $endAt
    reason = 'Manutenção preventiva sintética para demonstração'
}
$blockedReservation = @{
    startAt = $startAt; endAt = $endAt
    resourceIds = @($maintenanceRoom.id)
    justification = 'Tentativa sintética durante manutenção'
}
```

Envie `$blockedReservation` por `POST /api/v1/reservations` com o token de `$solicitante`. Resultado esperado: HTTP 409 por conflito de agenda/manutenção. O administrador pode conferir o bloqueio em `GET /api/v1/maintenance-blocks/resource/{resourceId}`. A criação e consulta de bloqueios são restritas a ADMINISTRADOR.

### 5. Reserva restrita, aprovação, retirada e devolução

**Ação:** solicitante pede material restrito; responsável autorizado aprova, registra retirada e devolução. **Resultado esperado:** estados `SOLICITADA` → `APROVADA` → `EM_USO` → `CONCLUIDA`. **Requisitos:** RF-01, RF-07 e RF-09.

O solicitante pede o material restrito. Resultado esperado: HTTP 201 com estado `SOLICITADA`.

```powershell
$restrictedRequest = @{
    startAt = $startAt; endAt = $endAt
    resourceIds = @($restrictedMaterial.id)
    justification = 'Demonstração de aprovação de recurso restrito'
}
$pending = Invoke-LocalApi POST '/api/v1/reservations' $solicitante.Token $restrictedRequest
"Reserva aguardando decisão: $($pending.id); estado: $($pending.status)"
```

O responsável pelo recurso aprova e registra a retirada e a devolução:

```powershell
$reservationPath = "/api/v1/reservations/$($pending.id)"
$approved = Invoke-LocalApi POST "$reservationPath/approve" $responsavel.Token @{
    note = 'Aprovado na demonstração'
}
$withdrawn = Invoke-LocalApi POST "$reservationPath/start" $responsavel.Token @{
    note = 'Retirada demonstrativa'
}
$returned = Invoke-LocalApi POST "$reservationPath/complete" $responsavel.Token @{
    note = 'Devolução demonstrativa'
}
"Estados: $($approved.status) -> $($withdrawn.status) -> $($returned.status)"
```

Resultados esperados: HTTP 200 nas três ações e estados `APROVADA`, `EM_USO` e `CONCLUIDA`. Somente o responsável associado a todos os recursos restritos da reserva ou um ADMINISTRADOR pode decidir; o solicitante não pode aprovar a própria reserva. A retirada cria o registro necessário para a devolução.

### 6. Auditoria e relatório — ADMINISTRADOR / RESPONSAVEL

**Ação:** administrador consulta eventos; administrador ou responsável consulta utilização para o período. **Resultado esperado:** auditoria somente leitura e relatório calculado a partir dos dados persistidos. **Requisitos:** RF-10 e RF-12.

O administrador consulta eventos de reserva; o administrador ou responsável consulta o relatório de utilização para o intervalo demonstrado:

```powershell
$audit = Invoke-LocalApi GET '/api/v1/audit-events?entityType=RESERVATION&page=0&size=50' $administrador.Token
$report = Invoke-LocalApi GET "/api/v1/reports/utilization?startAt=$startQuery&endAt=$endQuery" $responsavel.Token
"Eventos de reserva: $($audit.totalElements); recursos no relatório: $($report.resources.Count)"
```

Auditoria é somente leitura pela API e restrita a ADMINISTRADOR. O relatório inclui horas alocadas, percentual de utilização e conflitos evitados; não use a pesquisa de auditoria como prova de ações que ainda não foram executadas.

## Resultados esperados resumidos

| Parte | Perfil | Ação | Resultado observável | Requisito |
|---|---|---|---|---|
| Login | Os três perfis | Entrar com as contas sintéticas | HTTP 200; sessão autenticada | RF-01, RF-13 |
| Pesquisa/disponibilidade | SOLICITANTE | Pesquisar e consultar período | Resultados reais da API | RF-03 |
| Cadastro de recurso | ADMINISTRADOR | Criar recurso sintético | HTTP 201 e recurso persistido | RF-01, RF-02 |
| Sala comum | SOLICITANTE | Reservar recurso comum | HTTP 201, `APROVADA` automaticamente | RF-04 |
| Repetição no mesmo período | SOLICITANTE | Reenviar a mesma requisição | HTTP 409 `RESERVATION_CONFLICT` | RF-05, RF-06 |
| Tentativa durante manutenção | ADMINISTRADOR e SOLICITANTE | Criar bloqueio e tentar reservar | HTTP 409; bloqueio só é criado/consultado por ADMINISTRADOR | RF-01, RF-08 |
| Material restrito | SOLICITANTE e RESPONSAVEL | Solicitar, aprovar, retirar e devolver | `SOLICITADA` → `APROVADA` → `EM_USO` → `CONCLUIDA` | RF-07, RF-09 |
| Auditoria | ADMINISTRADOR | Consultar eventos | Eventos consultáveis sem alteração/exclusão | RF-10 |
| Relatório | ADMINISTRADOR ou RESPONSAVEL | Consultar utilização no período | Métricas retornadas pela API | RF-12 |
| Interface | Todos | Executar os fluxos web disponíveis | Mensagens da API; telas conforme o perfil | RF-01, RF-13 |

## Encerramento

**Perfil:** operador da demonstração. **Ação:** encerre o Spring Boot com `Ctrl+C` no terminal onde ele está em execução. Para parar somente o PostgreSQL de demonstração, no terminal que mantém `COMPOSE_PROJECT_NAME` configurado, execute `docker compose --project-name $env:COMPOSE_PROJECT_NAME stop postgres`. **Resultado esperado:** aplicação e container param; o volume do banco é preservado para uma próxima sessão. Não use `down -v` nem remova volumes. **Requisito:** RNF-09 (execução local documentada).

## Limitações e cuidados

- As contas locais só são criadas quando ainda não existem; a alteração de `LOCAL_SEED_PASSWORD` não redefine usuários existentes.
- O serviço de notificação HTTP pode não estar em execução no ambiente local. A falha é capturada pelo adaptador e não deve impedir a reserva/aprovação; não prometa entrega externa de mensagem.
- O conflito da API é HTTP 409 e deve ser mostrado como resultado esperado da demonstração.
- Os passos PowerShell/API documentam as operações de backend e continuam executáveis sem frontend. A interface web do PR #1 (`feat/16-frontend-rf13`) ainda não está em `origin/develop` enquanto o PR permanecer sem merge. Quando os arquivos estáticos estiverem no artefato executado, pode-se demonstrar no navegador em `http://localhost:8080/`; manutenção, auditoria e relatório continuam sem telas próprias. A matriz completa de validação responsiva está pendente.
- Use um banco Compose isolado e não execute limpeza destrutiva ao terminar. Os registros criados são sintéticos e podem permanecer no banco de demonstração.
