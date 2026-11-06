# Registro de defeitos

Este registro começa no Commit 01. Defeitos de produto devem conter severidade, causa, responsável, correção e evidência de reteste.

| ID | Severidade | Status | Descrição | Impacto | Responsável | Correção/reteste |
|---|---|---|---|---|---|---|
| DEF-001 | Baixa / ambiente | Aberto | O script de greeting AIOX não inicia porque o módulo Node `js-yaml` não está disponível no ambiente local. | Impede apenas a ativação automática do greeting do agente; não impede compilar, testar ou iniciar a aplicação Java. | Ambiente AIOX | Instalar a dependência Node e repetir `generate-greeting.js`; não corrigido neste commit. |

## Defeitos de produto

Nenhum defeito de produto conhecido foi identificado no escopo do Commit 01 até o momento deste registro.
