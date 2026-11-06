# ADR 0001: Stack e decisões do projeto

- **Status:** Aceita para o esqueleto; D1-D8 pendentes de validação com o docente.
- **Data:** 2026-11-06
- **Escopo:** Commit 01 - esqueleto e execução local.

## Contexto

O projeto acadêmico Organização de Recursos precisa de uma base Java 21 + Spring Boot 3.x executável, documentada e testável antes da implementação incremental dos recursos, reservas, identidade e evidências de qualidade.

## Decisões registradas

| ID | Decisão adotada na especificação | Registro neste incremento |
|---|---|---|
| D1 | `SOLICITADA`, `APROVADA` e `EM_USO` ocupam a agenda. | Apenas registrada. A regra de reserva será implementada nos Commits 09-10. |
| D2 | Recurso comum é aprovado automaticamente, com auditoria. | Apenas registrada. A aprovação e a auditoria funcional entram nos commits posteriores. |
| D3 | Material é unidade exclusiva, sem controle de estoque. | Apenas registrada para o modelo de recursos do Commit 05. |
| D4 | Recurso restrito possui `responsible_id`; responsável e administrador podem aprovar conforme o escopo. | Apenas registrada para o modelo e fluxo de aprovação. |
| D5 | Intervalo semiaberto `[início, fim)`, conflito quando `a < d AND c < b`, com UTC no banco. | Apenas registrada. A persistência UTC entra com o PostgreSQL no Commit 02 e a regra nos Commits 09-10. |
| D6 | Hash de senha com BCrypt. | Não implementada no Commit 01; fica para o Commit 03. |
| D7 | Interface `NotificationPort` com adaptador HTTP simulado. | Não implementada no Commit 01; fica para os commits de notificação. |
| D8 | Página estática HTML/CSS/JS responsiva servida pelo Spring Boot. | Não implementada no Commit 01; fica para o Commit 16. |

## Decisão de stack do Commit 01

- Maven com Maven Wrapper para reprodutibilidade local.
- Java 21 e Spring Boot 3.4.5.
- Spring Web, Validation, Data JPA, PostgreSQL Driver, Flyway, Security, Actuator, springdoc OpenAPI e JUnit 5.
- Monólito modular com pacotes por domínio conforme a seção 7 da especificação.
- Security fica com uma cadeia temporária `permitAll` somente para a casca executável. Não há login, JWT ou autorização de negócio.

## Configuração temporária para ausência do banco

O Commit 01 inclui o driver PostgreSQL, Flyway e JPA no `pom.xml`, mas exclui temporariamente `DataSourceAutoConfiguration`, `HibernateJpaAutoConfiguration`, `FlywayAutoConfiguration` e `UserDetailsServiceAutoConfiguration` nos perfis `local` e `test`. Isso permite que a aplicação e os testes passem sem PostgreSQL ou migrations e evita uma senha gerada pelo starter de Security enquanto não há identidade implementada.

Essa configuração deve ser removida no Commit 02, quando deverão ser adicionados o PostgreSQL, `docker-compose.yml`, a migration V1 e os testes de integração com Testcontainers. As propriedades `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` já estão documentadas apenas como nomes no `.env.example`.

## Pendências

1. Validar D1-D8 com o docente.
2. Remover as exclusões temporárias de persistência no Commit 02.
3. Adicionar schema e migrations Flyway no Commit 02.
4. Implementar identidade, BCrypt, JWT e autorização no Commit 03.
5. Adicionar CI, JaCoCo e SonarCloud no Commit 04.
