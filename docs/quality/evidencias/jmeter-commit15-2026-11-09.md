# Evidência JMeter — Commit 15

## Identificação e integridade

- Data: 2026-11-09 (horário local, UTC−03:00).
- Aplicação: Organização de Recursos, execução local no perfil `local`.
- Ferramenta: Apache JMeter 5.6.3.
- Execução: 09:30:40.871 a 09:31:40.757; intervalo observado de 59,886 s.
- JTL: `results.jtl`, 8.372.788 bytes, SHA-256 `C32B6B4F1D9EB2C84A923F7718EA37FBC76F7CCCBE1825A6B976D865B119EA2A`.
- Agregados do relatório JMeter: `statistics.json`, SHA-256 `580C53AC4C7607299775FD74F21E0BCDB4F03C66029789C4EEDAC9973A26E149`.
- O JTL e o relatório HTML completo (127 arquivos, 3.001.590 bytes) foram copiados para `%USERPROFILE%\Documents\Qas-load-evidence\commit15-2026-11-09`. Os originais em `%TEMP%` foram preservados. Os hashes do JTL e do `statistics.json` foram conferidos após a cópia.
- Os artefatos brutos permanecem fora do Git. O JTL não contém corpos de resposta nem dados de sampler; a revisão automatizada não encontrou JWT ou cabeçalho Bearer. O HTML inclui recursos JavaScript padrão do relatório JMeter, por isso somente os agregados e hashes são versionados.

## Resultados observados

| Grupo | Amostras | Respostas | Falhas inesperadas |
|---|---:|---|---:|
| Pesquisa de recursos | 55.792 | 55.792 × HTTP 200 | 0 |
| Login | 12 | 12 × HTTP 200 | 0 |
| Reservas concorrentes | 8 | 1 × HTTP 201; 7 × HTTP 409 esperados | 0 |
| Total | 55.812 | Todas as amostras receberam resposta HTTP | 0 |

Para a pesquisa de recursos, o relatório JMeter registrou p50 de 4 ms, p90 de 6 ms, p95 de 7 ms, p99 de 9 ms, média de 4,0937 ms e throughput de 933,9918 req/s. O throughput total foi 931,8929 req/s. O JMeter contabiliza os sete HTTP 409 como `success=false` por serem respostas não 2xx; eles são os conflitos previstos pelo cenário, não falhas inesperadas. O conjunto teve 7/55.812 amostras classificadas como erro pelo JMeter (0,0125421%), todas correspondentes a esses conflitos. Não houve falha de transporte ou HTTP 500.

A consulta de integridade no PostgreSQL isolado confirmou uma reserva `APROVADA` vinculada ao recurso e zero reservas órfãs.

## Método e limites

Os agregados foram extraídos de `results.jtl` e do `report/statistics.json` produzidos pela execução real. A distribuição dos códigos HTTP foi conferida diretamente no JTL. A verificação de integridade consultou a reserva da janela e os vínculos em `reservation_resources` no PostgreSQL isolado.

Ambiente registrado: Windows 11 Home, AMD Ryzen 7 5700X, 15,9 GB de RAM, Java 21.0.13, Docker Engine 29.9.2 e PostgreSQL 16.15. Trata-se de uma única execução local, não de um SLO, teste de capacidade de produção ou garantia de desempenho em outros ambientes. Não foi estabelecida meta de latência. SonarCloud continua sob responsabilidade de outro integrante.
