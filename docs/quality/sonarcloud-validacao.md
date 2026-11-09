# Integração SonarQube Cloud (SonarCloud): o que foi adicionado

Validada em 2026-11-09. O PR #5 passou nos checks `verify` e `SonarCloud Code Analysis`.

## Funcionalidades ativas no CI ([ci.yml](../../.github/workflows/ci.yml))

| Funcionalidade | Como funciona |
|---|---|
| Análise estática em PR | O step `Analyze with SonarCloud (Quality Gate)` roda `sonar:sonar` após `clean verify`, em PRs para `main` e `develop`. |
| Cobertura JaCoCo | O Sonar importa `target/site/jacoco/jacoco.xml`, gerado na fase `verify` pelo `jacoco-maven-plugin`. |
| Quality Gate bloqueante | `-Dsonar.qualitygate.wait=true` faz o step esperar o resultado e falhar se o gate reprovar. |
| Configuração obrigatória | Se faltar `SONAR_TOKEN`, `SONAR_ORGANIZATION` ou `SONAR_PROJECT_KEY`, o step falha (`exit 1`). Antes só avisava e terminava verde. |
| Proteção contra forks | O step só roda quando `head.repo.full_name == github.repository`. PRs de fork não recebem o secret nem a análise. |
| Sem valores no código | O token vem de `secrets.SONAR_TOKEN`. A organização e a project key vêm de `vars.*`. Nada fica no `pom.xml`. |

O plugin `sonar-maven-plugin` 5.8.0.7211 já estava declarado no [pom.xml](../../pom.xml) e foi reaproveitado.

## Configuração externa

| Onde | Item | Valor |
|---|---|---|
| SonarQube Cloud | Método de análise | CI-based (Automatic Analysis desativado) |
| SonarQube Cloud | Organization / Project key | `trabalhoqas7` / `patrickassis7_Qas` |
| GitHub Actions Secret | `SONAR_TOKEN` | token pessoal; expira em 30 dias e deve ser renovado |
| GitHub Actions Variables | `SONAR_ORGANIZATION`, `SONAR_PROJECT_KEY` | valores acima |

## Pendências manuais

- Em `Settings → Branches`, criar regras para `develop` e `main` exigindo o check `verify`. Sem isso o Sonar não bloqueia o merge de fato.
- Renovar o `SONAR_TOKEN` antes de 09/12/2026.
- No plano gratuito só PRs e a branch principal do Sonar são analisadas. O workflow não roda em `push`.

## Dados mockados

Nenhum dado mock foi removido, porque não há dado que prejudique o código em `src/main`:

- `LocalIdentitySeeder` roda só no perfil `local` (`@Profile("local")`) e não tem senha fixa. A senha vem de `LOCAL_SEED_PASSWORD` ou é aleatória. Os testes `LocalSeedIntegrationTest` dependem dele.
- O `JWT_SECRET` do `pom.xml` existe só na configuração do Surefire, para testes. Não é usado em runtime.
