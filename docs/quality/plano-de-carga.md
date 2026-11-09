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

## Análise dos resultados

1. No relatório HTML, anotar p50/p90/p95/p99, throughput e taxa de amostras com falha para a etiqueta `Pesquisar recursos`.
2. Para `Criar reserva concorrente`, contar os códigos no `results.jtl`: deve haver um 201, os demais 409 e nenhum 5xx. O JMeter pode classificar 409 como erro HTTP padrão; nesse cenário, 409 é o resultado esperado e deve ser separado dos erros inesperados.
3. Conferir no PostgreSQL que há uma reserva vinculada ao recurso no intervalo e nenhuma reserva órfã. Registrar a consulta/verificação usada sem exportar dados pessoais.
4. Guardar JTL e HTML fora do Git; se evidências forem anexadas à entrega, incluir somente agregados sem credenciais, tokens, dados pessoais ou logs brutos.
5. Registrar limitações, erros observados e defeitos reproduzidos em `docs/quality/defects.md`; atualizar a RTM com o commit e caminho da evidência segura.

## Resultado desta execução

Pendente até a execução real do plano. Não inferir percentis, throughput ou aprovação por existir um arquivo JMX. A análise SonarCloud fica com o integrante responsável por essa integração.
