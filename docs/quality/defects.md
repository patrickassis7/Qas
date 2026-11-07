# Registro de defeitos

Este registro começa no Commit 01. Defeitos de produto devem conter severidade, causa, responsável, correção e evidência de reteste.

| ID | Severidade | Status | Descrição | Impacto | Responsável | Correção/reteste |
|---|---|---|---|---|---|---|
| DEF-001 | Baixa / ambiente | Aberto | O script de greeting AIOX não inicia porque o módulo Node `js-yaml` não está disponível no ambiente local. | Impede apenas a ativação automática do greeting do agente; não impede compilar, testar ou iniciar a aplicação Java. | Ambiente AIOX | Instalar a dependência Node e repetir `generate-greeting.js`; não corrigido neste commit. |
| DEF-002 | Baixa / ambiente | Corrigido | Testcontainers 1.20.6 negociava a API Docker 1.32, incompatível com o Docker Desktop local, que exige no mínimo a API 1.40; fixar `api.version=1.40` poderia falhar em daemons com API mínima superior. | Impedia os testes de integração antes da criação do banco de teste e tornava a compatibilidade dependente de uma versão fixa da API. | Commit 02 / Commit 04 | No Commit 02, a propriedade `api.version=1.40` permitiu os testes locais. No Commit 04, Testcontainers foi atualizado para 1.21.4 e a propriedade removida; `clean verify` passou com PostgreSQL 16.15 no Docker Desktop 29.9.2 (API 1.56, mínimo 1.40), sem fixação de API. |
| DEF-003 | Média / configuração | Corrigido | O perfil `local` era ativado por padrão, permitindo que uma execução sem perfil explícito acionasse o seed de usuários fictícios. | Risco de criação indevida de usuários locais em uma execução de produção sem perfil explícito. | Commit 02 | Removido `spring.profiles.default: local`; ativação local passou a ser explícita e foi retestada com PostgreSQL Compose. |
| DEF-004 | Alta / segurança | Corrigido | A variável de ambiente `DEBUG` permitia que logs Spring MVC mostrassem o corpo do login e valores rejeitados pela validação. | Exposição de senha em logs durante login inválido. | Commit 03 | Limitados logs HTTP, segurança e binding SQL; `LoginRequest.toString()` foi redigido. Reteste com captura de logs para senha válida, senha rejeitada, token e chave. |
| DEF-005 | Média / API | Corrigido | A captura genérica de exceções convertia a `NoResourceFoundException` de uma rota sem handler em HTTP 500. | Clientes recebiam erro interno para uma URL inexistente, ocultando o status correto de não encontrada. | Commit 03 | Adicionado tratamento específico para 404 com contrato `ApiErrorResponse`; falhas inesperadas continuam em 500. Reteste de integração com token válido e rota protegida sem mapeamento. |

## Defeitos de produto

Nenhum defeito de produto conhecido foi identificado no escopo do Commit 01 até o momento deste registro.
