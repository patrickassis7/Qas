# Proposta revisada: gate AIOX para projetos Maven e multi-stack

**Estado:** candidata consolidada para nova revisão de `@architect` e `@po`; não aprovada nem implementada

**Revisão da proposta:** 2, em 2026-11-09. Esta atualização incorpora recomendações para resolver objeções anteriores; não substitui os pareceres dos revisores nem registra consenso.

**Projeto de referência:** Organização de Recursos (Java 21, Spring Boot, Maven, frontend estático)

**Branch/HEAD de referência:** `feat/rf13-responsive-overflow` / `94f1b6e`

**Commits locais preservados:** `ded9437`, `94f1b6e`

## 1. Decisão solicitada

Adicionar ao gate AIOX perfis de qualidade por stack. Para Maven, o fluxo deve usar Maven Wrapper e `clean verify`, conservar os limites JaCoCo e manter controles de segurança verificáveis. Em repositórios mistos, cada stack detectada recebe seus checks aplicáveis. Nenhuma verificação obrigatória vira recomendação ou PASS apenas porque a ferramenta ou script não existe.

Esta proposta não aprova a emenda, não autoriza a opção CodeRabbit GitHub, nem autoriza publicação dos commits pendentes. A Constitution determina proposta documentada, revisão de `@architect` e `@po`, consenso, atualização de versão e propagação aos artefatos dependentes.

## 2. Base e incompatibilidade observada

O Artigo V da Constitution atualmente lista como MUST `npm run lint`, `npm run typecheck`, `npm test`, `npm run build` e ausência de CRITICAL do CodeRabbit. A tarefa pre-push também torna a port denylist bloqueante. `AGENTS.md` repete comandos npm. A tarefa posteriormente diz que scripts npm ausentes podem ser ignorados, mas o detector atual retorna falha antes dessa lógica quando não acha `package.json`. A tarefa exige CodeRabbit CLI, enquanto o checklist diz “if available”; além disso, erros genéricos do CLI podem resultar em `CONCERNS`, apesar de outros tipos de falha bloquearem. A tarefa de security scan também usa `npm audit`/ESLint/Secretlint, permite pular alguns scans indisponíveis com aviso e trata Semgrep como opcional. Esses comportamentos precisam ser reconciliados; indisponibilidade de controle obrigatório não deve virar sucesso.

Este repositório tem `pom.xml`, Maven Wrapper e assets HTML/CSS/JavaScript, mas não tem `package.json` raiz. Portanto, adaptar somente os comandos de build sem resolver Artigo V, segurança, assets e semântica de N/A deixaria lacunas e contradições.

### Evidências históricas da revisão

- Revisão anteriormente registrada: Maven `clean verify`, BUILD SUCCESS, 202 testes aprovados, 0 falhas e 0 erros.
- JaCoCo naquela revisão: 93,64% linhas e 78,57% branches; o `pom.xml` define mínimos de 80% e 70%.
- Scanner AIOX anteriormente executado: 1.094 arquivos no escopo padrão e 0 ocorrências. Não foi reexecutado nesta revisão da proposta.
- Esses dados não foram recalculados agora e não constituem aprovação de política.

### 2.1 Objeções anteriores e cobertura nesta candidata

O quadro reproduz as objeções informadas pelo solicitante a partir do histórico anterior; não são pareceres novos nem respostas oficiais dos revisores.

| Revisor de origem | Objeção anterior | Tratamento nesta candidata | Situação |
|---|---|---|---|
| `@architect` | Precedência BLOCK/WARN e versão constitucional | Propõe BLOCK para todo MUST aplicável e Constitution 1.2.0/MINOR | Proposta clara; requer consenso |
| `@architect` e `@po` | Cobertura JaCoCo multimódulo | Define relatório agregado do reactor e prova de inclusão de todos os módulos com produção | Regra candidata; requer aceite |
| `@architect` e `@po` | Integridade/checksums do Maven Wrapper | Exige Wrapper, URL HTTPS, versão e checksums do Wrapper JAR e distribuição; falha fechado | Regra candidata; implementação precisa de testes |
| `@architect` | CodeRabbit no range/SHA exatos | Mantém CLI pre-push; exige base/head SHA, range completo e invalida resultado obsoleto | Capacidade da CLI ainda precisa de POC |
| `@architect` e `@po` | HIGH, timeout, falta de autenticação, erro e resultado inválido | Recomenda CRITICAL/HIGH bloqueantes; falhas operacionais e SHA divergente ficam BLOCKED | Semântica proposta; severity/waiver requer consenso |
| `@architect` e `@po` | Ferramentas/versões de segurança e critérios de aceite | Registra candidatos e versões para SCA, SAST, segredo, JS e CSS; exige POCs, fixtures e pins | Parcial; compatibilidade e rulesets não confirmados |
| `@architect` | Arquivos locais, ignorados e binários | Não ler/enviar não rastreados automaticamente; rastreados no range entram no scan; binário obrigatório não analisável bloqueia | Regra candidata; requer consenso sobre working tree |
| `@po` | Falsos positivos, waivers e indisponibilidade | Waiver estreito com finding, responsável, prazo; ferramenta indisponível nunca PASS/N/A | Regra candidata; política de exceção precisa de aprovação |
| `@po` | Evidência obrigatória por SHA | Relatório inclui base/head, comandos, versões, estados, resultados, métricas e links de artefatos | Regra candidata; formato/retention devem ser aprovados |
| `@po` | Cenários de aceite redundantes | Consolida combinações de stack, falhas, segurança, CodeRabbit, Git e SHA em uma única matriz | Consolidado nesta revisão |

## 3. Perfil por stack proposto

### 3.1 Detecção e seleção

1. Detectar Maven por `pom.xml`; exigir `mvnw`, `mvnw.cmd` e arquivos necessários em `.mvn/wrapper/`. O Wrapper é o executor canônico. Verificar URLs/versões explícitas, checksum do wrapper JAR e `distributionSha256Sum` da distribuição. Ausência, corrupção ou divergência bloqueia; não recorrer silenciosamente ao Maven global.
2. Detectar Node apenas por manifestos reais de projeto (`package.json` com scripts ou configuração Node reconhecida), nunca criar manifesto fictício para satisfazer o gate.
3. Projeto Java/Maven puro executa Maven e controles gerais; checks exclusivos de Node ficam N/A com a razão e evidência “nenhum pacote Node declarado”.
4. Projeto Node executa seus scripts e controles Node; Maven não é invocado se não existir projeto Maven.
5. Projeto misto executa ambos os perfis identificados. Um componente não anula o outro.
6. Maven multimódulo executa o Wrapper na raiz do reactor. Recomenda-se aplicar 80%/70% ao relatório agregado do reactor; o relatório deve demonstrar que todos os módulos com código de produção estão incluídos. Módulo de produção omitido, relatório incompleto ou denominador incompatível bloqueia. Esta proposta não adiciona limite independente por módulo: o agregado é a regra única para evitar requisitos divergentes.
7. Execução a partir de subdiretório localiza a raiz por regra determinística. Descritores conflitantes/ambíguos, `pom.xml` inválido, Wrapper ausente/corrompido ou stack não reconhecida provocam falha explícita; não há seleção silenciosa.

### 3.2 Build, testes e JaCoCo

Comandos obrigatórios para Maven:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

`clean verify` deve compilar e executar os testes configurados no projeto, incluindo integração/Testcontainers quando configurados. Os limites permanecem **80% de linhas e 70% de branches**, com Maven/JaCoCo como fonte única desses dois limites para o perfil Maven. O YAML genérico não deve introduzir um segundo limiar incompatível.

Para cada execução, o relatório do gate registra: SHA completo avaliado, branch/base, comando exato, perfil(s) selecionado(s), runtime e versões relevantes, código de saída, contagem de testes, falhas/erros/skips, cobertura observada, caminho/artefato dos relatórios e duração. Testes de integração que não iniciem por falta de Docker ou indisponibilidade externa são **bloqueio ambiental**, não PASS e não falha de código; o push segue bloqueado até a verificação obrigatória poder ser concluída ou até existir exceção formal aplicável.

Falha de compilação, teste, migration, cobertura ou scanner do código é classificada como falha de código/configuração e bloqueia. A classificação deve apontar evidência reproduzível sem divulgar segredo.

## 4. Controles de segurança por stack

Os controles abaixo preservam os objetivos da tarefa atual. Ferramentas e versões são **candidatas para avaliação**, não equivalências já aprovadas. Antes da implementação, revisores devem aprovar ferramenta, versão/digest, limiar, escopo, armazenamento de dados, comportamento sem rede e código de saída. Atualizar a versão no momento da implementação exige nova validação; versão de referência não é pin final.

| Objetivo | Proposta Maven/produto | Limites e condição de gate |
|---|---|---|
| Segredos | Recomenda-se Gitleaks CLI `8.30.1` como candidata, fixada por release verificada e checksum; antes da adoção, executar fixtures sintéticas positivas/negativas para comprovar o comportamento do digest escolhido. Secretlint permanece alternativa a deliberar, não fallback automático. | Finding confirmado bloqueia. Falha/indisponibilidade bloqueia. Logs mostram regra e caminho/linha, nunca segredo ou excerpt. Falso positivo requer exceção estreita, revisão, responsável e expiração. |
| CVEs em dependências Java | Recomenda-se avaliar OWASP Dependency-Check Maven Plugin `13.0.0`, com cache NVD e versão fixada após teste de compatibilidade. | Proposta de limiar: Critical e High (CVSS >= 7.0 quando houver score CVSS confiável) bloqueiam; Medium/Low são reportados como WARN/INFO; findings sem score ficam em triagem bloqueante até classificação. Feed ausente/desatualizado ou relatório inválido bloqueia ambientalmente. Waiver fica ligado ao CVE, componente/versão, justificativa, responsável e prazo. SCA não detecta vulnerabilidades desconhecidas nem substitui SAST. |
| SAST Java | Recomenda-se avaliar SpotBugs Maven Plugin `4.10.5.0` com Find Security Bugs `1.14.0`, fixados e validados em conjunto com Java 21, Maven Wrapper e Spring Boot 3.4.5. | Proposta de limiar: findings de severidade alta/crítica e confiança suficiente bloqueiam; demais são reportados para triagem. Falta de classes, erro do plugin ou relatório ausente bloqueia. Compatibility POC é requisito antes da aprovação; não presumir compatibilidade apenas porque as versões são recentes. |
| JavaScript frontend | `node --check` para sintaxe; Semgrep CLI `1.182.0` como candidato SAST, com ruleset versionado por referência imutável. ESLint `10.14.0` é candidato a lint somente com config e dependências de desenvolvimento reais, nunca script fictício. Node `24.11.0` é candidato por ser a versão observada no ambiente da auditoria anterior; confirmar patch suportado no momento da implementação e fixá-lo. | `node --check` não equivale a lint nem SAST. Falha ou ausência de check aprovado obrigatório bloqueia. Semgrep community/configuração e regras precisam de POC e análise de false positives antes da adoção. |
| HTML e CSS frontend | Nu Html Checker em artefato com checksum fixado. Stylelint `18.0.0` é candidato a lint CSS, condicionado a config, compatibilidade do runtime Node e execução reprodutível. | Validação estrutural/estilo não é SAST. Não usar tag móvel `latest` sem digest; erros segundo política aprovada bloqueiam. Não enviar conteúdo do projeto a serviço remoto sem autorização. |
| Port denylist AIOX | Executar o scanner AIOX existente diretamente, conforme seção 5. | Continua bloqueante dentro do seu escopo; não conta como SAST Java/HTML/CSS. |

**Semântica de indisponibilidade:** todo check obrigatório aplicável que não executa, não consegue ler seu input ou não produz relatório verificável é `BLOCKED`, nunca `PASS` nem `N/A`. Falha de código/configuração e bloqueio ambiental são estados distintos; ambos bloqueiam. `N/A` só é permitido quando a stack/artefato não existe e a justificativa aparece no relatório. Findings só podem ser dispensados por waiver formal, específico, versionado, com responsável, justificativa e validade; ferramenta ausente nunca é waiver.

O workflow Node existente mantém seus controles próprios. A migração para Maven não pode converter `npm audit`, ESLint ou secret detection em PASS/N/A sem mapear o objetivo de segurança correspondente. N/A só é legítimo quando a tecnologia/artefato não existe e a justificativa fica no relatório. “Não executado”, “indisponível”, “falhou” e “N/A” são estados distintos; os três primeiros não podem ser resumidos como PASS.

### Referências técnicas e versões candidatas — não aprovadas

- OWASP Dependency-Check `13.0.0` é candidato a SCA Maven: [projeto OWASP](https://owasp.org/projects/dependency-check) e [releases oficiais](https://github.com/dependency-check/DependencyCheck/releases).
- SpotBugs Maven Plugin `4.10.5.0` e Find Security Bugs `1.14.0` são candidatos para POC Java 21: [releases SpotBugs Maven](https://github.com/spotbugs/spotbugs-maven-plugin/releases) e [releases Find Security Bugs](https://github.com/find-sec-bugs/find-sec-bugs/releases). A combinação ainda não foi testada neste projeto.
- Gitleaks `8.30.1` é candidato a scanner de segredos, condicionado à integridade do artefato e a fixtures positivas/negativas: [releases oficiais](https://github.com/gitleaks/gitleaks/releases). Não selecionar apenas com base no número da versão.
- Semgrep CLI `1.182.0` é candidato para SAST Java/JavaScript; a configuração/ruleset também precisa de referência imutável: [release oficial](https://github.com/semgrep/semgrep/releases).
- ESLint `10.14.0` e Stylelint `18.0.0` são candidatos para lint frontend, não aprovados: [releases ESLint](https://github.com/eslint/eslint/releases) e [releases Stylelint](https://github.com/stylelint/stylelint/releases). Verificar Node engine e compatibilidade das configurações antes de fixar.
- Node documenta `--check` como verificação sintática sem executar o script: [CLI Node.js](https://nodejs.org/api/cli.html#-c---check).
- Nu Html Checker valida HTML/CSS e suporta CLI; a distribuição documentada usa `latest`, portanto o artefato deve ser fixado por checksum/digest: [projeto oficial](https://github.com/validator/validator). Stylelint não tem versão selecionada nesta proposta: [guia oficial](https://stylelint.io/user-guide/get-started/).

Essas ferramentas têm propósitos e limites diferentes. Não são equivalentes entre si nem equivalentes automaticamente aos checks atuais; sua adoção e severidade são decisões pendentes.

## 5. Port denylist

Manter o scanner real `.aiox-core/core/security/port-denylist.js` e seus findings/erros de leitura como bloqueantes. O módulo exporta `scanProject`; o exemplo de chamada direta abaixo evita script npm inventado e omite excerpts da saída:

```js
const { scanProject } = require('./.aiox-core/core/security/port-denylist');
const result = scanProject({ projectRoot: process.cwd() });
console.log(JSON.stringify({
  ok: result.ok,
  filesScanned: result.filesScanned,
  findings: result.findings.map(({ file, id, description, line }) => ({ file, id, description, line }))
}, null, 2));
if (!result.ok) process.exitCode = 1;
```

O escopo padrão cobre superfícies de portabilidade AIOX: `.claude`, `.codex`, `.gemini`, `.grok`, partes de `.aiox-core`, `bin`, `packages` e `scripts`; extensões JS/TS, JSON, YAML, Markdown, shell e Dockerfile; há exclusões/allowlists definidos no módulo. Não percorre `src/main/java` nem os assets de produto. O relatório deve guardar comando, SHA, número de arquivos, resultado e findings redigidos. O scanner inclui `excerpt` no objeto de finding e erros de leitura podem incluir detalhes de exceção; o consumidor do gate nunca deve imprimir/serializar o objeto bruto, somente campos permitidos (arquivo, id, descrição estática e linha). O teste de aceite precisa provar a redação inclusive no caminho de erro. Logs protegidos não devem conter conteúdo de origem. O resultado histórico de 1.094 arquivos/0 findings não substitui execução na revisão futura.

## 6. CodeRabbit: recomendação e decisão formal necessária

As alternativas não são intercambiáveis sem decisão sobre quando o gate ocorre.

### A. CLI em ambiente compatível

Manter CLI como verificação pre-push obrigatória até existir alternativa formalmente aprovada. Antes de contar o resultado, provar que a CLI revisou o range exato de saída, registrar base SHA e head SHA completos e validar a sintaxe suportada. `-t uncommitted` não comprova revisão de commits já feitos. Não presumir que a CLI aceita range/SHA sem uma prova. Recomendação: CRITICAL e HIGH bloqueiam; HIGH só pode ser resolvido ou dispensado por waiver formal aprovado. Medium/Low são registrados conforme regra aprovada. CLI ausente, não autenticada, timeout, erro genérico, saída não analisável ou SHA diferente resultam em `BLOCKED`; nenhuma confirmação interativa converte falha em PASS.

### B. Integração GitHub como verificação pré-merge

Pode ser avaliada apenas como alternativa pré-merge. A revisão associada ao PR/SHA ocorre depois do push e não satisfaz automaticamente o gate pre-push. Até que haja emenda e aprovação formal, a CLI pre-push continua sendo o requisito proposto. Adotar B exige demonstrar:

- check/revisão CodeRabbit requerido no branch protection/ruleset e associado ao SHA exato do head do PR;
- escopo revisado cobrindo todo o diff pretendido e atualizações da branch/head;
- mapeamento de severidade coerente com a política: CRITICAL/HIGH bloqueiam conforme esta recomendação; waiver é formal e limitado; demais findings são registrados segundo regra aprovada;
- check ausente, pendente, falho, obsoleto ou em SHA diferente mantém merge bloqueado;
- publicação pré-push segue bloqueada enquanto o CLI obrigatório não estiver satisfeito, salvo se a emenda constitucional e a equipe aprovarem explicitamente a troca de etapa/mecanismo.

Para A e B, estados distintos — finding CRITICAL/HIGH, timeout, erro genérico, autenticação ausente, check pendente/ausente e resultado obsoleto — precisam de resultados separados no gate. Falha de infraestrutura nunca equivale a review limpa. A semântica atual (HIGH como CONCERNS/aviso e erro genérico como possível confirmação) deve ser explicitamente reconciliada entre tarefa, `quality-gate-config.yaml` e checklist.

Até essa aprovação e evidência técnica, B não substitui A. Não se presume integração, nome de check ou configuração de branch protection.

## 7. Arquivos não rastreados, limpeza e publicação

Resolver “working tree limpo” como **estado seguro de publicação**, sem apagar trabalho local:

1. O gate calcula o conjunto publicável a partir do range explícito de commits (`base..HEAD`) e do SHA que será enviado; não usa `git add .`/`-A`.
2. Alterações staged/unstaged em arquivos rastreados fora dos commits pretendidos bloqueiam até resolução, pois tornam ambíguo o objeto revisado.
3. Arquivos não rastreados são inventariados por estado/caminho relativo, não entram no conjunto CodeRabbit nem no push e nunca são staged automaticamente. Sua presença não exige apagar ou mover arquivos: recomenda-se definir “estado limpo para publicação” como ausência de alterações tracked fora dos commits e untracked comprovadamente fora do range.
4. A varredura de segredos cobre os commits/arquivos publicáveis e logs de ferramentas, com conteúdo redigido. Conteúdo pessoal fora do conjunto não é examinado por padrão; o relatório registra apenas estado Git/caminho relativo. Se um arquivo for selecionado para publicação, passa a todos os scans. Nenhuma leitura ou transferência externa de conteúdo local pessoal sem autorização do proprietário.
5. Antes do push, comparar o range e SHA dos commits de saída, `git diff --name-only base...HEAD`, `git diff --cached --name-only` e `git status --porcelain`. Alterações tracked fora do conjunto bloqueiam; untracked são preservados, inventariados e ficam fora do range. Nenhuma operação destrutiva é usada para “limpar” a árvore.
6. Qualquer divergência entre SHA revisado, SHA staged/publicado ou head do PR invalida os checks dependentes daquele SHA e exige reexecução/revisão.

Arquivos ignorados e binários seguem a mesma regra de escopo: um arquivo ignorado ou não rastreado não é lido, copiado, enviado ou examinado automaticamente. Arquivos já rastreados no range são examinados mesmo se também corresponderem a uma regra de ignore. Se um binário for selecionado para publicação e um scanner obrigatório não puder examiná-lo, o resultado é `BLOCKED` até existir método aprovado; limitação do scanner não vira `N/A`. Colisões de caminho entre itens locais e arquivos pretendidos devem ser identificadas antes de checkout/merge que possa substituí-los.

No estado atual, `.vscode/`, `01-especificacao-e-documentacao.md` e `02-roteiro-e-plano-de-commits.md` são preservados e permanecem fora dos commits `ded9437` e `94f1b6e`. Esta proposta também permanece não rastreada. A verificação se concentra na árvore exata enviada, sem allowlist que esconda arquivos dentro do range.

## 8. Texto constitucional proposto para o Artigo V

O Artigo V atual é explícito sobre comandos npm como MUST. A adaptação requer alterar esse texto; não basta alterar só a task. Redação candidata para revisão:

> **MUST:** Cada projeto executa todos os gates obrigatórios aplicáveis ao perfil de tecnologia detectado por descritores reais e/ou configuração aprovada. O perfil define comandos reproduzíveis de build, testes, cobertura, segurança e revisão. Em projetos multi-stack, todos os perfis presentes são verificados. Um check obrigatório aplicável não executado, falho ou sem evidência verificável bloqueia publicação/integração. Falha ambiental permanece bloqueante até execução válida ou exceção formal prevista nesta Constitution. Check exclusivo de stack/artefato ausente pode ser N/A apenas com justificativa rastreável e sem reduzir o objetivo de qualidade/segurança correspondente. Checks npm permanecem obrigatórios no perfil Node; os checks de story e revisão continuam obrigatórios.

Os checks npm atuais ficam descritos como perfil Node, não removidos do perfil Node. Maven, Node e outras stacks terão comandos e controles próprios aprovados. A seção geral da Constitution atribui BLOCK a MUST críticos e WARN a MUST não críticos; o Artigo V também define gate de pre-push BLOCK. A emenda precisa declarar a precedência: recomendação desta proposta é que cada check obrigatório do gate de publicação seja BLOCK, enquanto WARN/INFO se apliquem apenas a itens consultivos que não são pré-condições. A falha ambiental de check obrigatório também bloqueia. Revisores devem aprovar ou substituir essa regra explícita. A redação é proposta, não vigente.

### Versionamento constitucional

Recomendação desta revisão: **MINOR — Constitution 1.2.0**. O Artigo V passa de comandos npm como regra geral para um sistema de perfis, mantendo npm obrigatório para Node e acrescentando regras Maven/multistack; isso amplia materialmente o alcance da política. PATCH seria adequado somente se os revisores justificarem que se trata de clarificação sem expansão significativa. A versão recomendada segue pendente de consenso e não altera a versão vigente 1.1.0.

## 9. Decisões ainda requeridas

| ID | Decisão | Recomendação para revisão |
|---|---|---|
| D1 | Texto final e alcance do Artigo V | Aprovar uma redação de gates obrigatórios por perfil; resolver MUST npm atual. |
| D2 | Detecção, wrapper, subdiretórios e multistack | Maven por POM + wrapper; casos ambíguos falham com instrução de configuração. |
| D3 | Maven multimódulo e cobertura | Recomendar agregado do reactor como regra única, 80%/70%, com prova de todos os módulos com produção; sem segundo limiar por módulo nesta proposta. |
| D4 | Wrapper e integridade | Recomendar Wrapper como único executor, URLs HTTPS/versões explícitas e checksums do Wrapper JAR/distribuição; ausência/divergência bloqueia. |
| D5 | Ferramentas e limiares de dependências/SAST | Candidatos: Dependency-Check 13.0.0 e SpotBugs Maven 4.10.5.0 + FindSecBugs 1.14.0; Critical/High como bloqueio recomendado e POC obrigatória Java 21. |
| D6 | Segredos e privacidade | Candidato Gitleaks 8.30.1, condicionado a checksum e fixtures; scanner selecionado varre o range exato e redige saída. Arquivos locais fora do range não são lidos/enviados sem autorização. |
| D7 | Assets frontend | Definir verificações obrigatórias de sintaxe JS (Node), HTML/CSS (Nu) e análise JS de segurança/lint; não confundir propósitos. |
| D8 | Port denylist | Preservar scanner/bloqueio; escopo AIOX e logs sem excerpts, inclusive erros de leitura. |
| D9 | CodeRabbit e momento do controle | Recomendar CLI pre-push associada ao range/base/head SHA; CRITICAL/HIGH bloqueiam. B continua pré-merge e exige emenda/aprovação; não substitui A implicitamente. |
| D10 | Estado Git/arquivos não rastreados | Recomendar estado seguro: itens não rastreados/ignorados preservados e inventariados sem leitura de conteúdo; binário publicável não analisável bloqueia. |
| D11 | Emenda/versionamento e autoridade | Recomendar Constitution 1.2.0 MINOR e todo MUST aplicável como BLOCK; consenso de `@architect` + `@po` e qualquer aprovação adicional da equipe devem ser registrados. |
| D12 | Evidências e retenção | Relatórios por execução vinculados aos SHAs; manter retenção vigente de 30 dias, sem mudança nesta proposta. |

## 10. Testes de aceite propostos para a implementação

| Cenário | Resultado esperado |
|---|---|
| Maven puro; Node puro; Maven + Node; assets estáticos sem pacote Node | Detectar todos e somente os perfis reais. Checks sem artefato recebem N/A justificado; falha obrigatória nunca recebe PASS. |
| Maven multimódulo | Wrapper executa o reactor; JaCoCo agregado cobre todos os módulos de produção; módulo ou relatório omitido bloqueia. |
| Wrapper ausente/corrompido, checksum inválido, POM inválido/ambíguo ou execução em subdiretório | Raiz e perfil resolvidos deterministicamente ou gate bloqueia; não usa Maven global como fallback. |
| Build/teste/migration falha ou cobertura abaixo de 80%/70% | Bloqueia e vincula exit code, contagens e relatório ao SHA avaliado. |
| Docker, feed CVE, secret scanner, SCA, SAST, Nu ou CodeRabbit indisponível/timeout/sem relatório | `BLOCKED` ambiental para check obrigatório; nunca PASS/N/A silencioso. |
| Denylist encontra ocorrência ou falha ao ler arquivo | Bloqueia; teste comprova que logs não contêm `excerpt`, conteúdo de origem ou stack trace sensível. |
| Secret scanner, SCA ou SAST encontra finding; falso positivo ou waiver | Findings bloqueantes impedem publicação; waiver amplo, vencido ou sem responsável/revisão não suprime. Saída redige valor secreto. |
| Node, HTML/CSS ou análise estática frontend falha | Check aplicável bloqueia; ferramenta ausente não vira PASS. Sintaxe não é apresentada como SAST. |
| CodeRabbit: finding CRITICAL/HIGH, timeout, auth ausente, erro genérico, `-t uncommitted`, range incompleto ou SHA divergente | Findings bloqueantes e falhas operacionais bloqueiam; `-t uncommitted` não comprova commits prontos; revisão deve estar vinculada ao range/base/head SHA. |
| Alternativa GitHub CodeRabbit ausente, pendente, falha ou SHA antigo | Pré-merge bloqueado se B tiver aprovação formal; não satisfaz pre-push. |
| Arquivos não rastreados/ignorados, pessoais, binários ou colisão de caminho | Preservar e inventariar por caminho/estado sem leitura/upload automático. Arquivo selecionado entra nos scans; binário obrigatório não analisável ou risco de sobrescrita bloqueia. Nada é staged automaticamente. |
| SHA de build/teste/review diverge do SHA que será publicado | Invalida checks dependentes; repetir no SHA final antes de liberar publicação. |

Os testes do gate também devem verificar que achados falsos/waivers são explícitos, auditáveis, limitados e não imprimem segredos. Não enfraquecer os testes do produto para obter gate verde.

### Evidência mínima por execução

O relatório local/CI deve ser um artefato estruturado (por exemplo, JSON com `headSha`, `baseSha`, perfis, checks, comandos, estado, duração, resumo redigido, caminhos de relatórios e versão/digest das ferramentas). Não deve incluir segredos, tokens, payloads, excerpts de código ou conteúdo de arquivos pessoais. O mesmo SHA deve aparecer nos resultados de Maven, JaCoCo, scanners e revisão CodeRabbit. Recomenda-se manter a retenção vigente de 30 dias no quality-gate config; qualquer mudança exige justificativa e aprovação própria. O resumo pode permanecer na story/PR após a expiração do artefato, sem copiar logs sensíveis.

## 11. Revisores, aprovações e sequência

- **`@architect`:** arquitetura do detector/perfis, segurança e equivalência técnica dos controles.
- **`@po`:** escopo, prioridades, critérios verificáveis, impacto e dependências.
- **Consenso:** aprovação explícita de ambos é exigida pela Constitution. Parecer favorável condicionado não constitui consenso.
- **Equipe/dono da política:** deve formalizar qualquer aprovação adicional estabelecida pela governança da equipe, em especial a alternativa CodeRabbit B e a mudança do momento pre-push para pré-merge. A Constitution lida não nomeia um aprovador humano separado para emendas.
- **Após consenso:** `@sm` ou `@po` cria/refina story com critérios/File List (Artigo III); então a política é implementada com versão e propagada aos artefatos dependentes. `@devops` conserva a autoridade exclusiva para push.

### Sequência proposta para fechar decisões

1. `@architect` e `@po` aprovam o alcance do Artigo V, precedência BLOCK/WARN e justificativa PATCH/MINOR.
2. A equipe decide a etapa do CodeRabbit e aprova os objetivos/ferramentas/limiares de SCA, SAST, segredos e assets; nenhuma ferramenta candidata é instalada por esta proposta.
3. Aprovar semantics de Maven reactor/coverage, wrapper integrity e publicação com itens untracked fora do conjunto.
4. Fechar formato do relatório e os casos de aceitação; registrar consenso dos revisores.
5. Só então `@sm`/`@po` prepara a story; implementação e propagação seguem depois.

Prioridade recomendada: alta para desbloquear publicação segura dos commits, sem permitir push enquanto um gate atualmente obrigatório estiver insatisfeito e sem alternativa aprovada.

## 12. Artefatos candidatos à mudança após autorização

Somente após consenso e story aprovada: `.aiox-core/constitution.md` (Artigo V e versão), tarefa pre-push, tarefa `security-scan.md`, detector de repositório, `quality-gate-config.yaml`, checklist pre-push, instruções `AGENTS.md` quando aplicável, agentes/templates/tasks dependentes, testes do detector/gate e documentação da story. Não modificar esses artefatos nesta etapa.

## 13. Referências

### Repositório

- [AGENTS.md](../../AGENTS.md)
- [Constitution AIOX](../../.aiox-core/constitution.md)
- [Tarefa pre-push](../../.aiox-core/development/tasks/github-devops-pre-push-quality-gate.md)
- Tarefa security scan: `.aiox-core/development/tasks/security-scan.md`
- [Configuração de quality gates](../../.aiox-core/core/quality-gates/quality-gate-config.yaml)
- [Checklist pre-push](../../.aiox-core/product/checklists/pre-push-checklist.md)
- [Detector](../../.aiox-core/infrastructure/scripts/repository-detector.js)
- [Port denylist](../../.aiox-core/core/security/port-denylist.js)
- [Agentes Architect](../../.aiox-core/development/agents/architect.md), [PO](../../.aiox-core/development/agents/po.md) e [DevOps](../../.aiox-core/development/agents/devops.md)
- Configuração JaCoCo no `pom.xml`

### Documentação pública para avaliação dos candidatos

- [OWASP Dependency-Check](https://owasp.org/projects/dependency-check)
- [SpotBugs Maven e integração Find Security Bugs](https://spotbugs.readthedocs.io/en/stable/maven.html)
- [OWASP Find Security Bugs](https://owasp.org/projects/find-security-bugs)
- [Gitleaks](https://github.com/gitleaks/gitleaks)
- [Node.js `--check`](https://nodejs.org/api/cli.html#-c---check)
- [Nu Html Checker](https://github.com/validator/validator)
- [Stylelint](https://stylelint.io/user-guide/get-started/)

## 14. Histórico e estado desta revisão

- Revisão 1: proposta técnica; registrava escolhas de ferramenta, severidade, cobertura multimódulo, CodeRabbit e estado Git como decisões abertas.
- Revisão 2 (2026-11-09): consolida recomendações de Constitution 1.2.0/MINOR, BLOCK para MUST aplicável, cobertura agregada, checksums do Wrapper, candidatos de ferramentas/versões, severidades e waivers, saída sanitizada da denylist, CodeRabbit CLI pre-push, preservação de arquivos locais e retenção de 30 dias.

**Estado:** candidata consolidada para revisão efetiva de `@architect` e `@po`; ainda não aprovada nem implementada. Versões, thresholds e exceções são recomendações sujeitas a validação e consenso. Compatibility POCs pendentes: Dependency-Check no reactor do projeto; SpotBugs/FindSecBugs juntos em Java 21; Semgrep e ruleset fixo; Gitleaks com fixtures; ESLint/Stylelint com Node pinado; Nu Html Checker com checksum; e capacidade da CLI CodeRabbit de revisar o range de commits e comprovar base/head SHA. Nenhuma política, gate, template, agente, código ou configuração SonarCloud foi alterada. Não houve build/teste nesta atualização documental. Os commits `ded9437` e `94f1b6e`, `.vscode/`, os dois documentos locais não rastreados e esta proposta foram preservados.
