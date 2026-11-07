# ADR 0001: Stack e decisões do projeto

- **Status:** Aceita para a base dos Commits 01 e 02; D1-D8 pendentes de validação com o docente.
- **Data:** 2026-11-06
- **Escopo:** Commits 01 e 02 - esqueleto, execução local e persistência base.

## Contexto

O projeto acadêmico Organização de Recursos precisa de uma base Java 21 + Spring Boot 3.x executável, documentada e testável antes da implementação incremental dos recursos, reservas, identidade e evidências de qualidade.

## Decisões registradas

| ID | Decisão adotada na especificação | Registro neste incremento |
|---|---|---|
| D1 | `SOLICITADA`, `APROVADA` e `EM_USO` ocupam a agenda. | Apenas registrada. A regra de reserva será implementada nos Commits 09-10. |
| D2 | Recurso comum é aprovado automaticamente, com auditoria. | Apenas registrada. A aprovação e a auditoria funcional entram nos commits posteriores. |
| D3 | Material é unidade exclusiva, sem controle de estoque. | Apenas registrada para o modelo de recursos do Commit 05. |
| D4 | Recurso restrito possui `responsible_id`; responsável e administrador podem aprovar conforme o escopo. | Apenas registrada para o modelo e fluxo de aprovação. |
| D5 | Intervalo semiaberto `[início, fim)`, conflito quando `a < d AND c < b`, com UTC no banco. | Apenas registrada. A persistência base já usa `TIMESTAMPTZ`/UTC no Commit 02; a regra entra nos Commits 09-10. |
| D6 | Hash de senha com BCrypt. | Implementada para autenticação e seed local no Commit 03. |
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

As exclusões de persistência foram removidas no Commit 02. A exclusão temporária de `UserDetailsServiceAutoConfiguration` foi mantida, pois a cadeia de segurança continua permissiva e login/JWT pertencem ao Commit 03. A aplicação agora usa PostgreSQL, `docker-compose.yml`, a migration V1 e testes de integração com Testcontainers. As propriedades `DB_URL`, `DB_NAME`, `DB_PORT`, `DB_USERNAME` e `DB_PASSWORD` permanecem documentadas somente como nomes no `.env.example`; valores reais continuam fora do Git.

## Decisão de segurança do Commit 03

Spring Security OAuth2 Resource Server/Nimbus valida JWT HS256 assinado com `JWT_SECRET` obrigatório no ambiente do processo, sem leitura de argumentos ou propriedades Spring. O valor deve ser Base64 padrão representando pelo menos 32 bytes gerados com fonte criptograficamente segura; a aplicação decodifica e valida o formato antes de criar encoder e decoder. O `sub` é o UUID interno, o emissor é validado e a validade é limitada a 60 minutos. Em cada acesso autenticado, usuário ativo e papéis são consultados no PostgreSQL, evitando acesso após desativação e papéis desatualizados. A senha do seed local é opcional por ambiente; sem ela, o hash é aleatório e desconhecido, e o seed nunca atualiza usuários existentes. O log HTTP/security/SQL é limitado para evitar registro de credenciais mesmo quando `DEBUG` está ativo no ambiente. Erros de rota inexistente retornam 404 padronizado; erros inesperados de servidor permanecem 500. Migration V1 não é alterada.

## Pendências

1. Validar D1-D8 com o docente.
2. Adicionar CI, JaCoCo e SonarCloud no Commit 04.
