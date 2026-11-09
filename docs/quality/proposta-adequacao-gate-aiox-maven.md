# Proposta revisada: gate AIOX para projetos Maven e multi-stack

**Estado:** revisão 10, candidata após revisão direcionada; não aprovada nem implementada

**Revisão da proposta:** 10, em 2026-11-09. Incorpora direcionamentos do solicitante e uma POC limitada de Git blobs; não substitui pareceres, consenso ou aprovação humana e não registra política aprovada.

**Projeto de referência:** Organização de Recursos (Java 21, Spring Boot, Maven, frontend estático)

**Branch/HEAD de referência:** `feat/rf13-responsive-overflow` / `89d3c66`

**Commits locais preservados:** `ded9437`, `94f1b6e`, `89d3c66` (este último documenta o handoff e torna rastreados os documentos locais de requisitos e esta proposta)

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

### Evidência adicional desta retomada

Em 2026-11-09, o módulo `.aiox-core/core/security/port-denylist.js` foi chamado diretamente com o diretório do repositório e raízes padrão. Resumo redigido do resultado: `ok=true`, 1.094 arquivos examinados, 0 findings. O scanner deliberadamente cobre as superfícies AIOX configuradas; não examina `src/main/java` nem `src/main/resources/static`, portanto este resultado não comprova segurança completa do produto nem substitui secret scanning, SCA, SAST ou CodeRabbit. O comando npm da tarefa não foi repetido nesta retomada; sua incompatibilidade com este Maven permanece documentada na evidência anterior.

### 2.1 Objeções anteriores e cobertura nesta candidata

O quadro reproduz as objeções informadas pelo solicitante a partir do histórico anterior; não são pareceres novos nem respostas oficiais dos revisores.

| Revisor de origem | Objeção anterior | Tratamento nesta candidata | Situação |
|---|---|---|---|
| `@architect` | Precedência BLOCK/WARN e versão constitucional | Propõe BLOCK para todo MUST aplicável e Constitution 1.2.0/MINOR | Proposta clara; requer consenso |
| @architect e @po | Cobertura JaCoCo multimódulo | Revisão 10 mantém suporte multimódulo fora do escopo mínimo; POM com modules bloqueia. Suporte futuro exige decisão, story e POC específica | Tratamento atual proposto; não há regra universal agregada |
| `@architect` e `@po` | Integridade/checksums do Maven Wrapper | Exige Wrapper, URL HTTPS, versão e checksums do Wrapper JAR e distribuição; falha fechado | Regra candidata; implementação precisa de testes |
| `@architect` | CodeRabbit no range/SHA exatos | Mantém CLI pre-push; exige base/head SHA, range completo e invalida resultado obsoleto | Capacidade da CLI ainda precisa de POC |
| @architect e @po | HIGH, timeout, falta de autenticação, erro e resultado inválido | CRITICAL bloqueia conforme regra vigente; HIGH conserva semântica atual por enquanto; falha operacional/SHA inválido fica BLOCKED. Endurecer HIGH é decisão separada D14 | Regra candidata coerente com adaptação mínima; segurança de execução continua a validar |
| `@architect` e `@po` | Ferramentas/versões de segurança e critérios de aceite | Registra candidatos e versões para SCA, SAST, segredo, JS e CSS; exige POCs, fixtures e pins | Parcial; compatibilidade e rulesets não confirmados |
| `@architect` | Arquivos locais, ignorados e binários | Não ler/enviar não rastreados automaticamente; rastreados no range entram no scan; binário obrigatório não analisável bloqueia | Regra candidata; requer consenso sobre working tree |
| @po | Falsos positivos, waivers e indisponibilidade | Check obrigatório indisponível nunca PASS/N/A; waiver de gate obrigatório é proibido pela regra atual e só poderia ser criado por emenda constitucional explícita | Política de waiver não habilitada |
| `@po` | Evidência obrigatória por SHA | Relatório inclui base/head, comandos, versões, estados, resultados, métricas e links de artefatos | Regra candidata; formato/retention devem ser aprovados |
| `@po` | Cenários de aceite redundantes | Consolida combinações de stack, falhas, segurança, CodeRabbit, Git e SHA em uma única matriz | Consolidado nesta revisão |

### 2.2 Pareceres advisory da rodada 3 (2026-11-09)

Os agentes disponíveis `@architect` e `@po` emitiram pareceres **favoráveis com condições** sobre a revisão anterior desta candidata. Eles não alteraram a proposta nem a política; o mecanismo oficial de greeting AIOX não iniciou porque o ambiente não contém `js-yaml`. Os pareceres apontaram: perfil de assets estáticos independente de Node; mecânica comprovável de JaCoCo multimódulo; scanner denylist que não leia untracked do filesystem; separação entre adaptação Maven mínima e endurecimento HIGH do CodeRabbit; status RF-13 sem transição cosmética; e associação completa do conjunto/range ao SHA. Esta revisão 4 incorpora esses pontos para uma segunda revisão; ainda não é consenso.

### 2.3 Pareceres advisory da revisão 4 (2026-11-09)

`@architect` e `@po` voltaram a ser **favoráveis com condições**, sem consenso. `@architect` considerou resolvidos no desenho os perfis de assets, denylist por blobs, divisão CRITICAL/HIGH e proteção de SHA; ainda exige POC multimódulo, CLI CodeRabbit para committed range e fixture untracked que produza finding real. `@po` considerou resolvidos perfil de assets, divisão de severidades e associação ao SHA; ainda exige decisão do status `Review`, bootstrap CodeRabbit concreto, autoridade de waivers e propagação explícita a `AGENTS.md`. A revisão 5 responde a esses pontos como proposta; nenhum mecanismo novo foi implementado.

## 3. Perfil por stack proposto

### 3.1 Detecção e seleção

1. Detectar Maven por `pom.xml`; exigir `mvnw`, `mvnw.cmd` e arquivos necessários em `.mvn/wrapper/`. O Wrapper é o executor canônico. Verificar URLs/versões explícitas, checksum do wrapper JAR e `distributionSha256Sum` da distribuição. Ausência, corrupção ou divergência bloqueia; não recorrer silenciosamente ao Maven global.
2. Detectar pacote Node apenas por manifesto/configuração real. Nunca criar `package.json` fictício para satisfazer o gate.
3. Detectar **assets estáticos** em perfil próprio, independente de pacote Node: arquivos `.html`, `.css` e `.js` sob raízes de produto configuradas (neste projeto, `src/main/resources/static/`). O relatório registra arquivos encontrados e a regra de detecção. Ausência de `package.json` torna N/A somente os checks de pacote/dependências Node; não torna N/A a validação dos assets existentes.
4. Perfil Maven executa o Wrapper, testes, JaCoCo e controles Maven aprovados. Perfil Node executa scripts e controles Node declarados. Perfil de assets estáticos aplica verificações sobre os arquivos HTML/CSS/JS, sem exigir inventar manifesto npm. Um repositório misto executa todos os perfis detectados.
5. Para JavaScript estático, `node --check` é o check de sintaxe já disponível no projeto; ele não é lint nem SAST. Lint/SAST de JS e validação de HTML/CSS serão obrigatórios somente após ferramenta, versão, configuração e regras serem aprovadas e testadas. Até a decisão, a implementação do perfil não pode marcar tais objetivos como PASS/N/A se forem obrigatórios pela política vigente: o estado é BLOCKED.
6. Qas é atualmente single-module. Suporte a Maven multimódulo fica fora desta adaptação mínima: detectar POM com módulos e bloquear com instrução para política/story específica, sem executar cobertura agregada como PASS. Se o escopo futuro incluir multimódulo, aprovar se a meta 80% linhas/70% branches será agregada ou por módulo e executar a POC JaCoCo descrita no cenário correspondente antes de ativar o perfil.
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

Os controles abaixo preservam os objetivos da tarefa atual. Ferramentas e versões são **candidatas para avaliação**, não equivalências já aprovadas. A adaptação mínima não instala scanners novos nem os declara aprovados por aparecerem nesta tabela. Antes de substituir ou ampliar um controle, revisores e responsáveis aprovam ferramenta, versão/digest, limiar, escopo, privacidade/armazenamento de dados, comportamento sem rede e código de saída; uma POC com fixtures demonstra o objetivo de controle. Até isso ocorrer, check de segurança atualmente obrigatório sem execução equivalente permanece BLOCKED. Atualizar a versão no momento da implementação exige nova validação; versão de referência não é pin final.

| Objetivo | Proposta Maven/produto | Limites e condição de gate |
|---|---|---|
| Segredos | Recomenda-se Gitleaks CLI `8.30.1` como candidata, fixada por release verificada e checksum; antes da adoção, executar fixtures sintéticas positivas/negativas para comprovar o comportamento do digest escolhido. Secretlint permanece alternativa a deliberar, não fallback automático. | Finding confirmado bloqueia. Falha/indisponibilidade bloqueia. Logs mostram regra e caminho/linha, nunca segredo ou excerpt. Falso positivo requer exceção estreita, revisão, responsável e expiração. |
| CVEs em dependências Java | Recomenda-se avaliar OWASP Dependency-Check Maven Plugin `13.0.0`, com cache NVD e versão fixada após teste de compatibilidade. | Proposta de limiar: Critical e High (CVSS >= 7.0 quando houver score CVSS confiável) bloqueiam; Medium/Low são reportados como WARN/INFO; findings sem score ficam em triagem bloqueante até classificação. Feed ausente/desatualizado ou relatório inválido bloqueia ambientalmente. Waiver fica ligado ao CVE, componente/versão, justificativa, responsável e prazo. SCA não detecta vulnerabilidades desconhecidas nem substitui SAST. |
| SAST Java | SpotBugs Maven Plugin + Find Security Bugs permanecem candidatos a POC em Java 21, Wrapper e Spring Boot 3.4.5; versões e ruleset não são aprovados por esta proposta. | Não há mapeamento de severidade adotado ainda. POC deve usar fixtures e a equipe deve aprovar uma tabela objetiva de severidade/confiança e waivers antes de tornar o scanner gate. Erro, classes ausentes ou relatório inválido bloqueiam se o check estiver aprovado como obrigatório. |
| JavaScript frontend | `node --check` para sintaxe; Semgrep CLI `1.182.0` como candidato SAST, com ruleset versionado por referência imutável. ESLint `10.14.0` é candidato a lint somente com config e dependências de desenvolvimento reais, nunca script fictício. Node `24.11.0` é candidato por ser a versão observada no ambiente da auditoria anterior; confirmar patch suportado no momento da implementação e fixá-lo. | `node --check` não equivale a lint nem SAST. Falha ou ausência de check aprovado obrigatório bloqueia. Semgrep community/configuração e regras precisam de POC e análise de false positives antes da adoção. |
| HTML e CSS frontend | Nu Html Checker em artefato com checksum fixado. Stylelint `18.0.0` é candidato a lint CSS, condicionado a config, compatibilidade do runtime Node e execução reprodutível. | Validação estrutural/estilo não é SAST. Não usar tag móvel `latest` sem digest; erros segundo política aprovada bloqueiam. Não enviar conteúdo do projeto a serviço remoto sem autorização. |
| Port denylist AIOX | Executar o scanner AIOX existente diretamente, conforme seção 5. | Continua bloqueante dentro do seu escopo; não conta como SAST Java/HTML/CSS. |

**Semântica de indisponibilidade:** todo check obrigatório aplicável que não executa, não consegue ler seu input ou não produz relatório verificável é `BLOCKED`, nunca `PASS` nem `N/A`. Falha de código/configuração e bloqueio ambiental são estados distintos; ambos bloqueiam. `N/A` só é permitido quando a stack/artefato não existe e a justificativa aparece no relatório. Findings só podem ser dispensados por waiver formal, específico, versionado, com responsável, justificativa, aprovador e validade; ferramenta ausente nunca é waiver. Quando thresholds ainda não foram aprovados, a proposta não inventa uma severidade: a implementação do gate aguarda decisão, e a ausência de política/check não produz PASS.

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

Manter os mesmos padrões do scanner real `.aiox-core/core/security/port-denylist.js` como bloqueantes. **Não chamar `scanProject({ projectRoot })` no workspace como gate de publicação:** a função percorre o filesystem e pode ler arquivo não rastreado dentro das raízes. O scanner exporta `scanContent`; a proposta é adicionar um adaptador pequeno que lê somente blobs Git rastreados no `headSha` avaliado e submete os conteúdos a essa função:

```js
const { execFileSync } = require('node:child_process');
const { scanContent, DEFAULT_SCAN_ROOTS } = require('./.aiox-core/core/security/port-denylist');
// Pseudocódigo do adaptador a implementar e testar; SHA deve ser validado como commit existente.
const paths = listTrackedBlobsAtCommit(headSha, DEFAULT_SCAN_ROOTS, approvedExtensions);
const findings = [];
for (const relativePath of paths) {
  const content = execFileSync('git', ['show', `${headSha}:${relativePath}`], { encoding: 'utf8' });
  findings.push(...scanContent(content, relativePath).map(({ id, description, line }) => ({ relativePath, id, description, line })));
}
emitRedactedSummary({ headSha, filesScanned: paths.length, findings });
if (findings.length) process.exitCode = 1;
```

O adaptador é proposta, não existe ainda. `listTrackedBlobsAtCommit` deve usar a árvore Git do SHA exato, restringir às raízes/extensões/exclusões atuais e nunca consultar conteúdo do working tree; erros de `git show`/blob bloqueiam. Não emitir o campo `excerpt` de `scanContent`, nem mensagem bruta de erro. O teste deve criar arquivo untracked contendo um marcador sintético único dentro de uma raiz varrida e provar que ele não é lido nem aparece na saída; também deve criar blobs commitados com e sem finding e provar a associação ao SHA. Assim preserva-se o scanner real sem ler arquivos pessoais locais.

O escopo padrão atual cobre superfícies de portabilidade AIOX: `.claude`, `.codex`, `.gemini`, `.grok`, partes de `.aiox-core`, `bin`, `packages` e `scripts`; extensões JS/TS, JSON, YAML, Markdown, shell e Dockerfile; há exclusões/allowlists definidos no módulo. Não percorre `src/main/java` nem os assets de produto. Em 2026-11-09, uma execução local do `scanProject({ projectRoot })` existente retornou `ok=true`, 1.094 arquivos e 0 findings, com saída redigida. Como essa função lê o filesystem, esse resultado é evidência do scan manual no checkout, não validação do adaptador proposto nem prova de que untracked nunca é lido. Também foi confirmado que o fixture sintético `const fixture = "sinkra_test_marker";` produz um finding `sinkra-prefix` em `scanContent`; ainda não foi testado um adaptador Git que ignore um arquivo untracked. A execução futura do gate deve registrar SHA, comando, escopo, contagem, resultado e findings redigidos.

## 6. CodeRabbit: recomendação e decisão formal necessária

As alternativas não são intercambiáveis sem decisão sobre quando o gate ocorre.

### A. CLI em ambiente compatível

Manter CLI como verificação pre-push obrigatória até existir alternativa formalmente aprovada. Antes de contar o resultado, provar por POC que a CLI revisou o range exato de commits pretendido e registra base SHA e head SHA completos. O comando vigente `-t uncommitted` não comprova revisão dos commits já criados; não presumir suporte a range/SHA sem teste com revisão identificável. A CLI não está disponível neste ambiente e nenhum relatório existe para `ded9437`, `94f1b6e`, `89d3c66` ou para o range completo; portanto, não há evidência de review utilizável para publicação.

Para a **adaptação mínima**, manter o limiar já escrito na Constitution: zero CRITICAL. HIGH mantém a semântica atualmente implementada (CONCERNS/aviso e decisão explícita conforme fluxo vigente), sem elevar silenciosamente HIGH a bloqueio. Tratar o endurecimento de HIGH para BLOCK como decisão separada D14. Em qualquer severidade, CLI ausente, não autenticada, timeout, erro genérico, saída não analisável ou SHA diferente resultam em `BLOCKED`; confirmação humana não converte falha operacional em review válida ou PASS.

### B. Integração GitHub como verificação pré-merge

Opção B é apenas uma alternativa futura, não selecionada nesta proposta. Ela exigiria uma nova decisão constitucional sobre mover o gate para pré-merge, autorização humana formal e configuração de branch protection confirmada. Não há bootstrap/exceção de primeiro push proposto; a integração GitHub não satisfaz o pre-push atual nem desbloqueia esta publicação.

- check/revisão CodeRabbit requerido no branch protection/ruleset e associado ao SHA exato do head do PR;
- escopo revisado cobrindo todo o diff pretendido e atualizações da branch/head;
- mapeamento de severidade coerente com a decisão aprovada: CRITICAL bloqueia conforme a Constitution vigente; HIGH segue a política vigente até que eventual endurecimento seja aprovado separadamente; waiver é formal e limitado; demais findings são registrados segundo regra aprovada;
- check ausente, pendente, falho, obsoleto ou em SHA diferente mantém merge bloqueado;
- publicação pré-push segue bloqueada enquanto o CLI obrigatório não estiver satisfeito, salvo se a emenda constitucional e a equipe aprovarem explicitamente a troca de etapa/mecanismo.

Para A e B, estados distintos — finding CRITICAL, finding HIGH, timeout, erro genérico, autenticação ausente, check pendente/ausente e resultado obsoleto — precisam de resultados separados no gate. Falha de infraestrutura nunca equivale a review limpa. A decisão de HIGH é independente da escolha CLI/GitHub e deve ser registrada. A semântica de erro genérico como possível `CONCERNS`/confirmação na tarefa atual deve ser substituída por estado bloqueante quando não houver relatório de revisão válido, se essa alteração for aprovada como requisito de integridade do gate.

Até essa aprovação e evidência técnica, B não substitui A. Não se presume integração, nome de check ou configuração de branch protection.

**Situação deste checkout:** a CLI `coderabbit` não está disponível no ambiente e não há relatório de review associado ao range/base/head SHA atual. Portanto, não existe evidência CodeRabbit aprovada para os commits `ded9437`, `94f1b6e` e `89d3c66`. A alternativa B também não foi aprovada e, por ocorrer no PR depois do push, não satisfaz o gate pre-push vigente. O requisito permanece bloqueante; este parágrafo registra estado, não concede exceção.

## 7. Arquivos não rastreados, limpeza e publicação

Resolver “working tree limpo” como **estado seguro de publicação**, sem apagar trabalho local:

1. O gate calcula o conjunto publicável a partir do range explícito de commits (`base..HEAD`) e do SHA que será enviado; não usa `git add .`/`-A`.
2. Alterações staged/unstaged em arquivos rastreados fora dos commits pretendidos bloqueiam até resolução, pois tornam ambíguo o objeto revisado.
3. Arquivos não rastreados são inventariados por estado/caminho relativo, não entram no conjunto CodeRabbit nem no push e nunca são staged automaticamente. Sua presença não exige apagar ou mover arquivos: recomenda-se definir “estado limpo para publicação” como ausência de alterações tracked fora dos commits e untracked comprovadamente fora do range.
4. A varredura de segredos cobre os commits/arquivos publicáveis e logs de ferramentas, com conteúdo redigido. Conteúdo pessoal fora do conjunto não é examinado por padrão; o relatório registra apenas estado Git/caminho relativo. Se um arquivo for selecionado para publicação, passa a todos os scans. Nenhuma leitura ou transferência externa de conteúdo local pessoal sem autorização do proprietário.
5. Antes do push, comparar o range e SHA dos commits de saída, `git diff --name-only base...HEAD`, `git diff --cached --name-only` e `git status --porcelain`. Alterações tracked fora do conjunto bloqueiam; untracked são preservados, inventariados e ficam fora do range. Nenhuma operação destrutiva é usada para “limpar” a árvore.
6. Qualquer divergência entre SHA revisado, SHA staged/publicado ou head do PR invalida os checks dependentes daquele SHA e exige reexecução/revisão.

Arquivos ignorados e binários seguem a mesma regra de escopo: um arquivo ignorado ou não rastreado não é lido, copiado, enviado ou examinado automaticamente. Arquivos já rastreados no range são examinados mesmo se também corresponderem a uma regra de ignore. Se um binário for selecionado para publicação e um scanner obrigatório não puder examiná-lo, o resultado é `BLOCKED` até existir método aprovado; limitação do scanner não vira `N/A`. Colisões de caminho entre itens locais e arquivos pretendidos devem ser identificadas antes de checkout/merge que possa substituí-los.

No estado atual verificado após o commit `89d3c66`, `.vscode/` continua preservada como não rastreada e fora dos commits. `01-especificacao-e-documentacao.md`, `02-roteiro-e-plano-de-commits.md` e esta proposta foram incluídos explicitamente no commit documental `89d3c66`; eles não pertencem ao range dos commits RF-13 `ded9437` e `94f1b6e`, mas passam a fazer parte do range total caso os três commits sejam publicados juntos. Qualquer gate deve analisar o conjunto exato de commits e SHAs que será publicado, não ignorar os documentos por uma allowlist presumida.

## 8. Texto constitucional proposto para o Artigo V

O Artigo V atual é explícito sobre comandos npm como MUST. A adaptação requer alterar esse texto; não basta alterar só a task. Redação candidata para revisão:

> **MUST:** Cada projeto executa todos os gates obrigatórios aplicáveis ao perfil de tecnologia detectado por descritores reais e/ou configuração aprovada. O perfil define comandos reproduzíveis de build, testes, cobertura, segurança e revisão. Em projetos multi-stack, todos os perfis presentes são verificados. Um check obrigatório aplicável não executado, falho ou sem evidência verificável bloqueia publicação/integração. Falha ambiental permanece bloqueante até execução válida ou exceção formal prevista nesta Constitution. Check exclusivo de stack/artefato ausente pode ser N/A apenas com justificativa rastreável e sem reduzir o objetivo de qualidade/segurança correspondente. Checks npm permanecem obrigatórios no perfil Node; os checks de story e revisão continuam obrigatórios.

Os checks npm atuais ficam descritos como perfil Node, não removidos do perfil Node. Maven, Node e outras stacks terão comandos e controles próprios aprovados. Para eliminar a contradição MUST/BLOCK/WARN, propõe-se incluir também esta regra interpretativa no Artigo V/Compliance: **todo MUST que constitua pré-condição de um gate obrigatório de publicação ou integração definido no Artigo V tem severidade BLOCK, inclusive falha ambiental, ausência de ferramenta ou evidência; WARN aplica-se somente a recomendações não bloqueantes que não sejam pré-condições MUST; INFO aplica-se a SHOULD.** A tabela global de severidade continua válida para demais princípios: MUST que não integra um gate de publicação explicitamente bloqueante pode permanecer WARN quando assim classificado, e nenhuma regra geral pode rebaixar um BLOCK explícito do Artigo V. Assim, a regra não converte automaticamente todos os MUST da Constitution em BLOCK nem rebaixa nenhum check obrigatório. `N/A` requer artefato/stack realmente ausente, razão registrada e nenhuma perda do objetivo de segurança/qualidade. Revisores devem aprovar ou propor redação alternativa. Esta interpretação é candidata, não vigente.

### Versionamento constitucional

Recomendação desta revisão: **MINOR — Constitution 1.2.0**. O Artigo V passa de comandos npm como regra geral para um sistema de perfis, mantendo npm obrigatório para Node e acrescentando regras Maven/multistack; isso amplia materialmente o alcance da política. PATCH seria adequado somente se os revisores justificarem que se trata de clarificação sem expansão significativa. A versão recomendada segue pendente de consenso e não altera a versão vigente 1.1.0.

## 9. Decisões ainda requeridas

| ID | Decisão | Recomendação para revisão |
|---|---|---|
| D1 | Texto final e alcance do Artigo V | Aprovar uma redação de gates obrigatórios por perfil; resolver MUST npm atual. |
| D2 | Detecção, wrapper, subdiretórios e multistack | Maven por POM + wrapper; casos ambíguos falham com instrução de configuração. |
| D3 | Maven multimódulo e cobertura | Fora do perfil mínimo atual; POM com módulos resulta em BLOCKED. Suporte futuro requer decisão de escopo, story e POC para JaCoCo aggregate ou critério por módulo. |
| D4 | Wrapper e integridade | Recomendar Wrapper como único executor, URLs HTTPS/versões explícitas e checksums do Wrapper JAR/distribuição; ausência/divergência bloqueia. |
| D5 | Ferramentas e limiares de dependências/SAST | Candidatos e versões exigem POC no Java 21; nenhuma ferramenta/versão/threshold fica aprovada só por constar aqui. Findings sem política de severidade aprovada mantêm o check pendente, não PASS. |
| D6 | Segredos e privacidade | Candidato Gitleaks 8.30.1, condicionado a checksum e fixtures; scanner selecionado varre o range exato e redige saída. Arquivos locais fora do range não são lidos/enviados sem autorização. |
| D7 | Assets frontend | Detectar assets estaticamente independente de `package.json`; `node --check` verifica sintaxe JS. Ferramentas HTML/CSS e lint/SAST são candidatas que exigem aprovação/POC; não confundir propósitos. |
| D8 | Port denylist | Preservar padrões/scanner bloqueante; definir adaptador por blobs Git do SHA para não ler untracked; saída sem excerpts. |
| D9 | CodeRabbit e momento do controle | Direcionamento recebido: opção A, CLI autenticada para o range committed e base/head SHA. B não foi escolhida; indisponibilidade da CLI bloqueia publicação. |
| D10 | Estado Git/arquivos não rastreados | Recomendar estado seguro: itens não rastreados/ignorados preservados e inventariados sem leitura de conteúdo; binário publicável não analisável bloqueia. |
| D11 | Emenda/versionamento e autoridade | Recomendar Constitution 1.2.0 MINOR; BLOCK para pré-condições obrigatórias do gate do Artigo V e WARN/INFO conforme escopo definido; registrar consenso de `@architect` + `@po` e aprovações adicionais da equipe. |
| D12 | Evidências e retenção | Relatórios por execução vinculados aos SHAs; manter retenção vigente de 30 dias, sem mudança nesta proposta. |
| D13 | Story RF-13 e compatibilidade do status | Manter a story em Review e propor que o gate aceite Review após emenda; até consenso, alteração e teste do parser, a regra vigente continua sem aceitar Review. Não alterar o status para contornar o gate. |
| D14 | Severidade CodeRabbit HIGH | Adaptação mínima conserva CRITICAL como bloqueador e semântica vigente para HIGH; tornar HIGH bloqueante é endurecimento separado que exige consenso e justificativa, não efeito colateral de Maven. |
| D15 | Waivers e autoridade | Direcionamento: @architect avalia arquitetura, @qa qualidade, @po escopo; responsável nominal do repositório só poderá autorizar waiver se uma emenda constitucional definir explicitamente esse poder, limites, prazo e registro. Até essa emenda, waiver/bypass de gate obrigatório não é permitido. Falta identificar o responsável. |
| D16 | JaCoCo multimódulo | Fora do escopo desta adaptação mínima. POM com módulos é detectado e bloqueado como perfil ainda não suportado, nunca N/A ou PASS silencioso. Suporte exige decisão de escopo, story e POC específica antes de implementação. |
| D17 | Bootstrap CodeRabbit B | Não selecionado. A proposta não autoriza bootstrap nem push excepcional. Reconsiderar B exigiria nova decisão formal sobre mudança do gate pre-push e aprovação constitucional; enquanto isso, CLI A é obrigatória. |

## 10. Testes de aceite propostos para a implementação

| Cenário | Resultado esperado |
|---|---|
| Maven puro; Node puro; Maven + Node; assets estáticos sem pacote Node | Detectar todos e somente os perfis reais. Checks sem artefato recebem N/A justificado; falha obrigatória nunca recebe PASS. |
| Maven multimódulo fora do escopo atual | Detecção do reactor com módulos bloqueia com status explícito de perfil não suportado. Só após decisão de escopo/story/POC a matriz passa a exigir a estratégia JaCoCo aprovada; não marcar N/A nem PASS silencioso. |
| Wrapper ausente/corrompido, checksum inválido, POM inválido/ambíguo ou execução em subdiretório | Raiz e perfil resolvidos deterministicamente ou gate bloqueia; não usa Maven global como fallback. |
| Build/teste/migration falha ou cobertura abaixo de 80%/70% | Bloqueia e vincula exit code, contagens e relatório ao SHA avaliado. |
| Docker, feed CVE, secret scanner, SCA, SAST, Nu ou CodeRabbit indisponível/timeout/sem relatório | `BLOCKED` ambiental para check obrigatório; nunca PASS/N/A silencioso. |
| Denylist encontra ocorrência, falha ao ler blob ou há arquivo untracked com marcador sob raiz AIOX | Bloqueia finding/erro; leitura do gate vem só de blobs Git no SHA; teste prova que o marcador untracked não é lido nem exposto; logs não contêm `excerpt`, conteúdo de origem ou stack trace sensível. |
| Secret scanner, SCA ou SAST encontra finding; falso positivo ou waiver | Findings bloqueantes impedem publicação; waiver de check obrigatório não existe na política atual. Qualquer futura exceção exige emenda constitucional, autoridade e campos definidos; saída redige valores secretos. |
| Node, HTML/CSS ou análise estática frontend falha | Check aplicável bloqueia; ferramenta ausente não vira PASS. Sintaxe não é apresentada como SAST. |
| CodeRabbit: finding CRITICAL; finding HIGH; timeout, auth ausente, erro genérico, `-t uncommitted`, range incompleto ou SHA divergente | CRITICAL bloqueia; HIGH segue a regra vigente, salvo aprovação explícita D14; falha operacional/resultado sem SHA/range válido bloqueia; nenhum relatório ausente vira PASS. |
| Opção B — GitHub CodeRabbit | Não selecionada nesta candidata; não satisfaz o gate pre-push e não desbloqueia os commits locais. Reconsideração exige nova emenda e autorização formal. |
| Arquivos não rastreados/ignorados, pessoais, binários ou colisão de caminho | Preservar e inventariar por caminho/estado sem leitura/upload automático. Gates vinculados a commits consomem blobs do SHA; arquivos selecionados entram nos scans somente por seleção explícita. Binário obrigatório não analisável ou risco de sobrescrita bloqueia. Nada é staged automaticamente. |
| Story RF-13 em Review | Direção proposta: preservar Review e aceitar esse estado após emenda. Antes da emenda e do teste atualizado do parser, regra atual continua bloqueando; não reescrever status. |
| Denylist e leitura local | Criar arquivo untracked sintético, ausente da árvore Git, sob uma raiz AIOX; seu conteúdo `const fixture = "sinkra_test_marker";` é comprovadamente detectado por `scanContent` nesse caminho. O adaptador do commit SHA deve ignorá-lo, não ler seu conteúdo e não imprimir o marcador. Finding commitado e erro de blob devem falhar com logs sanitizados. |
| CodeRabbit indisponível ou sem CLI autenticada | BLOCKED; não há bootstrap ou exceção de primeiro push nesta candidata. Resultado precisa cobrir o range committed e base/head SHA completos. |
| Waiver de gate obrigatório | Rejeitado pela política atual. Uma futura emenda, se aprovada, deve definir finding/risco, escopo, justificativa, checks compensatórios, responsável, aprovador humano nominal, expiração, registro e vínculo com SHA; waiver expirado ou sem esses campos não muda BLOCKED para PASS. |
| Assets estáticos HTML/CSS/JS sem `package.json` | Perfil de assets é detectado pela raiz/extensões; `node --check` é registrado apenas como syntax check; checks HTML/CSS/lint/SAST só passam com ferramentas/configuração aprovadas e relatório vinculado ao SHA. |
| SHA de build/teste/review diverge do SHA que será publicado | Invalida checks dependentes; repetir no SHA final antes de liberar publicação. |

Os testes do gate também devem verificar que achados falsos/waivers são explícitos, auditáveis, limitados e não imprimem segredos. Não enfraquecer os testes do produto para obter gate verde.

### Evidência mínima por execução

O relatório local/CI deve ser um artefato estruturado (por exemplo, JSON com `headSha`, `baseSha`, perfis, checks, comandos, estado, duração, resumo redigido, caminhos de relatórios e versão/digest das ferramentas). Não deve incluir segredos, tokens, payloads, excerpts de código ou conteúdo de arquivos pessoais. O mesmo SHA deve aparecer nos resultados de Maven, JaCoCo, scanners e revisão CodeRabbit. Recomenda-se manter a retenção vigente de 30 dias no quality-gate config; qualquer mudança exige justificativa e aprovação própria. O resumo pode permanecer na story/PR após a expiração do artefato, sem copiar logs sensíveis.

## 11. Revisores, aprovações e sequência

- **`@architect`:** arquitetura do detector/perfis, segurança e equivalência técnica dos controles.
- **`@po`:** escopo, prioridades, critérios verificáveis, impacto e dependências.
- **Consenso:** aprovação explícita de ambos é exigida pela Constitution. Parecer favorável condicionado não constitui consenso.
- **Autoridade humana:** o responsável nominal do repositório ainda deve ser identificado. Seu poder para autorizar waivers não existe na política vigente; só poderá ser considerado após emenda constitucional explícita. A designação da pessoa, sozinha, não cria autoridade para bypass.
- **Story existente:** a story docs/stories/1.6-commit-16-frontend-rf13.md permanece em Review. Direcionamento do solicitante e recomendação do PO: manter Review e propor que esse estado seja aceito após emenda. Até alterar formalmente e testar Constitution/gate/parser, a regra atual não aceita Review.
- **Mecanismo de ativação:** `generate-greeting.js` e o fallback `unified-activation-pipeline.js` falharam por ausência de `js-yaml`. As revisões por subagentes com skills `aiox-architect`/`aiox-po` são pareceres advisory; não serão apresentadas como aprovação formal automática nem como decisão humana.
- **Após consenso:** `@sm` ou `@po` cria/refina story com critérios/File List (Artigo III); então a política é implementada com versão e propagada aos artefatos dependentes. `@devops` conserva a autoridade exclusiva para push.
- **@qa:** parecer consultivo de qualidade e evidências; não substitui o consenso constitucional exigido entre @architect e @po.

### Sequência proposta para fechar decisões

1. `@architect` e `@po` aprovam ou revisam a precedência Artigo V BLOCK/WARN e justificativa PATCH/MINOR.
2. A candidata seleciona a CLI CodeRabbit A para pre-push; confirmar via POC que cobre range committed/base/head SHA. B fica não selecionada e só pode ser reaberta por nova emenda formal. A decisão de severidade HIGH permanece separada em D14.
3. `@po` resolve legitimamente o estado RF-13; não se altera status antes do parecer.
4. Aprovar perfil separado de assets estáticos, segurança/scanners e severidades; concluir POCs e versões fixas para controles novos.
5. Fechar os controles do escopo single-module desta adaptação: Wrapper integrity, adaptador denylist por blobs, tratamento de untracked e relatório por SHA. JaCoCo multimódulo só entra em nova etapa se a equipe aprovar expansão de escopo, story e POC.
6. Fechar matriz de aceite e registrar consenso explícito dos revisores. Parecer favorável com condições não conta como consenso.
7. Só então `@sm`/`@po` prepara/refina story; implementação e propagação seguem depois.

Prioridade recomendada: alta para desbloquear publicação segura dos commits, sem permitir push enquanto um gate atualmente obrigatório estiver insatisfeito e sem alternativa aprovada.

## 12. Artefatos candidatos à mudança após autorização

Somente após consenso e story aprovada, os artefatos mínimos incluem: `.aiox-core/constitution.md` (Artigo V/Compliance e versão); **`AGENTS.md`** (seus comandos npm são atualmente requisitos explícitos e precisam ser alinhados); tarefa `github-devops-pre-push-quality-gate.md`; tarefa `security-scan.md`; detector/perfil Maven e asset; runner denylist por blobs; parser e configuração `quality-gate-config.yaml`; checklist pre-push; referências/status nos agentes `devops.md`, templates e workflows que duplicam os comandos; testes de unidade/integração do detector e de cada decisão; story e documentação de evidência. A lista final deve ser verificada por busca de todos os comandos/status antigos antes de fechar a implementação. Não modificar esses artefatos nesta etapa.

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
- Rodada advisory 3 (2026-11-09): `@architect` e `@po` favoráveis com condições. Solicitaram perfil de assets estáticos, mecânica JaCoCo multimódulo, leitura denylist sem untracked, separação do limiar HIGH e decisão legítima do status RF-13.
- Revisão 4 (2026-11-09): candidata atualizada nesses pontos e submetida novamente. Ambos permaneceram favoráveis com condições; `@architect` pediu fixture untracked com padrão que realmente gere finding e esclarecimento de timeline; `@po` pediu decisão explícita do status, definição do bootstrap CodeRabbit B, autoridade de waivers e inclusão obrigatória de `AGENTS.md` na propagação.
- Revisão 5 (2026-11-09): incorpora as condições da revisão 4, define o bootstrap proposto como exceção única e auditável ainda dependente de aprovação, registra opções explícitas para `Review`/JaCoCo multimódulo e atribui à equipe a designação do aprovador de waivers. Aguarda nova revisão advisory; ainda não há consenso.

- Revisão 6 (2026-11-09): registra direcionamentos recebidos e POC sintética limitada de seleção por Git blobs; permanece candidata, sem consenso nem aprovação, conforme aditivo seção 15.
- Revisão 7 (2026-11-09): responde aos pareceres condicionais da revisão 6: waiver não é permitido pela política atual e depende de emenda explícita; multimódulo fica fora do escopo mínimo e bloqueia até suporte aprovado; RF-13 preserva Review; opção B/bootstrap removida da rota escolhida. Não há consenso.
- Revisão 8 (2026-11-09): alinha as referências residuais apontadas no parecer advisory da revisão 7: seção B marcada como alternativa futura não selecionada, passo multimódulo condicionado a nova expansão, e tabela de waiver esclarece que a autoridade só pode existir após emenda. Não declara consenso.
- Pareceres advisory: revisões 7 e 8 foram condicionais. Na revisão 10, @architect e @po deram consenso técnico explícito ao conteúdo substantivo após conferir as correções de waiver, D3/D16, severidades, B/bootstrap, QA e RF-13. Isso não é aprovação humana nem torna a emenda vigente; o processo constitucional e a story ainda são necessários.

**Estado:** revisão 10, conteúdo substantivo recebeu consenso técnico explícito de @architect e @po; ainda não é emenda aprovada nem implementada. Falta seguir o processo constitucional (story, aprovação formal aplicável, versionamento e propagação). Nenhuma política/gate/código foi alterado. CLI CodeRabbit autenticada e review por range/base/head SHA continuam ausentes; publicação bloqueada.

## 15. Direcionamentos recebidos e POC limitada (atualizado na revisão 10)

Este aditivo prevalece sobre recomendações incompatíveis das seções anteriores. As respostas do solicitante são direcionamentos para a candidata; não equivalem a consenso dos revisores nem à aprovação da emenda.

| Tema | Direcionamento | Estado formal |
|---|---|---|
| Aprovação | Consenso de @architect + @po; uma autoridade humana nominal só poderá aprovar waiver se a emenda constitucional criar esse poder e seus limites | Falta identificar a pessoa e obter consenso explícito sobre o texto |
| CodeRabbit | Opção A: CLI autenticada, review do range committed e associação a base/head SHA | CLI não disponível neste ambiente; POC e execução real continuam pendentes. Opção B não foi escolhida |
| Story RF-13 | Manter Review e propor que o gate aceite esse estado após emenda | Depende de consenso e alteração/teste do parser; a regra vigente continua bloqueando Review |
| POCs prioritárias | CodeRabbit range/SHA, denylist por Git blobs e scanners de segurança; JaCoCo multimódulo somente se o suporte multimódulo entrar no escopo | Somente a prova conceitual de denylist foi exercitada; as demais permanecem pendentes |
| Segurança e waiver | @architect avalia arquitetura, @qa qualidade, @po escopo; o responsável nominal do repositório só poderá autorizar waiver se uma emenda constitucional criar explicitamente esse poder e seus limites | Falta identificar o responsável e obter a emenda/consenso; a política atual não permite waiver de gate obrigatório |

### POC denylist por Git blobs

Em ambiente sintético e separado, sem alterar o repositório Qas, um harness Node enumerou arquivos committed com git ls-tree/git show e passou o conteúdo para scanContent em .aiox-core/core/security/port-denylist.js.

Na revisão sintética d37d5312f5fabbb3a6f7d7c7e7cda00c13ce5fbf, um fixture rastreado produziu um finding sinkra-prefix. No parent eb5d66927eba88fcbd4f799893ed54ab6f5458fd, o arquivo correspondente não produziu findings. Um fixture não rastreado permaneceu fora da lista enumerada por git ls-tree.

Esta POC comprova somente a viabilidade de selecionar blobs committed e deixar arquivos untracked fora do conjunto. Não valida adaptador de produção, renames/deletions, limites de tamanho, binários, submódulos, SHA inválido, relatórios sanitizados completos nem todos os roots. Esses cenários permanecem critérios da POC de implementação.

### Estado consolidado

O conteúdo da revisão 10 recebeu consenso técnico explícito de @architect e @po, mas continua não aprovado como emenda nem implementado. Constitution, tarefas, gates, agentes, templates, código de produto e configuração SonarCloud não foram alterados. Não houve build/teste Maven nesta atualização documental. A POC de blobs é conceitual, não valida adaptador de produção. CLI CodeRabbit autenticada e review por range/base/head SHA permanecem ausentes; publicação bloqueada.

O consenso técnico de @architect e @po sobre o conteúdo da revisão 10 foi registrado. Ainda não existe emenda constitucional vigente: é necessário criar/refinar a story por @sm/@po, cumprir a autorização formal aplicável, atualizar versão e propagar a regra; somente depois implementar e validar. Waivers continuam proibidos pela política atual. O push permanece reservado a @devops e bloqueado enquanto CLI CodeRabbit e demais gates obrigatórios não passarem.