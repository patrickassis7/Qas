# Registro de defeitos

Este registro começa no Commit 01. Defeitos de produto devem conter severidade, causa, responsável, correção e evidência de reteste.

| ID | Severidade | Status | Descrição | Impacto | Responsável | Correção/reteste |
|---|---|---|---|---|---|---|
| DEF-001 | Baixa / ambiente | Aberto | O script de greeting AIOX não inicia porque o módulo Node `js-yaml` não está disponível no ambiente local. | Impede apenas a ativação automática do greeting do agente; não impede compilar, testar ou iniciar a aplicação Java. | Ambiente AIOX | Instalar a dependência Node e repetir `generate-greeting.js`; não corrigido neste commit. |
| DEF-002 | Baixa / ambiente | Corrigido | Testcontainers 1.20.6 negociava a API Docker 1.32, incompatível com o Docker Desktop local, que exige no mínimo a API 1.40. | Impedia os testes de integração antes da criação do banco de teste. | Commit 02 | Configurada a propriedade de teste `api.version=1.40` no Surefire; reteste confirmado por `clean verify` com PostgreSQL 16.15 e todos os testes passando. |
| DEF-003 | Média / configuração | Corrigido | O perfil `local` era ativado por padrão, permitindo que uma execução sem perfil explícito acionasse o seed de usuários fictícios. | Risco de criação indevida de usuários locais em uma execução de produção sem perfil explícito. | Commit 02 | Removido `spring.profiles.default: local`; ativação local passou a ser explícita e foi retestada com PostgreSQL Compose. |

## Defeitos de produto

Nenhum defeito de produto conhecido foi identificado no escopo do Commit 01 até o momento deste registro.
