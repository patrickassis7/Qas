# ADR 0001: Stack e decisões do projeto

- **Status:** Decisões da stack aceitas; decisões D1-D8 adotadas conforme a especificação e ainda pendentes de validação formal com o docente.
- **Data:** 2026-11-06
- **Escopo:** Stack e decisões D1-D8 do projeto Organização de Recursos.

## Contexto

O projeto acadêmico Organização de Recursos precisa de uma base Java 21 + Spring Boot 3.x executável, documentada e testável antes da implementação incremental dos recursos, reservas, identidade e evidências de qualidade.

## Decisões registradas

| ID | Decisão adotada na especificação | Implementação observada e situação de integração |
|---|---|---|
| D1 | `SOLICITADA`, `APROVADA` e `EM_USO` ocupam a agenda. | Implementada em `ReservationStatus.occupiesAgenda()` e usada por `AgendaConflictChecker`; também descrita em [ADR 0002](0002-concorrencia-reservas.md). Presente em `origin/develop`. |
| D2 | Recurso comum é aprovado automaticamente, com auditoria. | Implementada em `ReservationService.approveAutomaticallyIfCommon()`; transição e evento de auditoria são cobertos por testes de integração. Presente em `origin/develop`. |
| D3 | Material é unidade exclusiva, sem controle de estoque. | O tipo `MATERIAL` é um recurso individual (`ResourceEntity`); não há quantidade ou tabela de estoque. A agenda comum impede sobreposição do mesmo recurso. Presente em `origin/develop`; não representa controle de inventário. |
| D4 | Recurso restrito possui `responsible_id`; responsável e administrador podem aprovar conforme o escopo. | `ResourceEntity.responsible` mapeia `responsible_id`; `ApprovalService` exige administrador ou responsável por todos os recursos restritos da reserva e impede o solicitante de decidir a própria reserva. Presente em `origin/develop`. |
| D5 | Intervalo semiaberto `[início, fim)`, conflito quando `a < d AND c < b`, com UTC no banco. | Implementada pela consulta de sobreposição em `ReservationRepository`, pelas datas `TIMESTAMPTZ` e configuração JPA UTC; concorrência usa lock pessimista por recurso, conforme ADR 0002. Presente em `origin/develop`. |
| D6 | Hash de senha com BCrypt. | Implementada pelo `PasswordEncoder` BCrypt e `LoginService`; testes de autenticação cobrem login e falhas. Presente em `origin/develop`. |
| D7 | Interface `NotificationPort` com adaptador HTTP simulado. | Implementada por `NotificationPort` e `HttpNotificationAdapter`; a integração HTTP é configurável e testada com WireMock. Não há provedor externo de produção configurado. Presente em `origin/develop`. |
| D8 | Página estática HTML/CSS/JS responsiva servida pelo Spring Boot. | Implementada em `origin/feat/16-frontend-rf13` (`src/main/resources/static/`); PR #1 está aberto para `develop`. Não está presente em `origin/develop` enquanto o PR não for integrado. A matriz completa de validação visual responsiva continua pendente. |

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

## Decisões de CI e qualidade do Commit 04

- GitHub Actions valida PRs destinados a `main` e `develop`, em `ubuntu-24.04`, com Eclipse Temurin 21, cache Maven, permissões mínimas e Maven Wrapper. Actions oficiais são fixadas por SHA.
- Testcontainers 1.21.4 é compatível com a stack testada (Spring Boot 3.4.5, Java 21 e Docker Desktop 29.9.2). Com essa versão, os testes passaram sem a propriedade artificial `api.version=1.40`; a versão fixa foi removida para permitir negociação de API com o daemon.
- JaCoCo 0.8.16 gera XML e HTML no `verify`. O gate de 80% de linhas e 70% de branches foi introduzido no Commit 14; os resultados específicos por branch e execução são registrados no relatório de qualidade.
- SonarScanner for Maven 5.8.0.7211 usa `SONAR_TOKEN` em GitHub Secret e os identificadores reais como variáveis do repositório. Análise não autenticada ou com IDs inventados não é executada; ausência de configuração não afeta `clean verify`.
- WireMock 3.13.2 fica somente em escopo de teste e cobre o adaptador HTTP de notificação implementado no módulo `notification`.
- Regras de proteção de `main`, token e identificadores do SonarCloud exigem configuração administrativa no GitHub/SonarCloud e não são alterados pelo repositório.

## Pendências

1. Registrar a validação formal de D1-D8 com o docente; a implementação observada não substitui essa confirmação acadêmica.
2. SonarCloud continua sob responsabilidade de outro integrante e depende de configuração e execução remotas confirmadas.
3. A interface D8 e a validação visual responsiva dependem da integração/revisão do PR #1; sua presença não deve ser inferida em `develop` antes do merge.
