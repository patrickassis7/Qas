# Reconciliação documental após os PRs #1 e #2

**Registro:** 2026-11-09.
**Estado consultado:** PR #1 aberto como Draft em `f37e95854e6964d86ec5606aa9ffc1a6fdc1a0e8`; PR #2 aberto como Draft em `3705037666bb39ad0346de7e26fe4955084d13d9`. Ambos têm `develop` como destino.

Este registro não altera `README.md`, `RTM.md` ou `docs/RTM.md`. O primeiro é modificado pelo PR #1 e a matriz detalhada é modificada pelo PR #2. A consolidação antes da integração poderia substituir conteúdo paralelo ou deixar afirmações inconsistentes; retomar os três arquivos depois da decisão dos PRs.

## RTM — reconciliar após a integração do PR #2

### Resultado da auditoria comparativa

Auditoria realizada em 2026-11-09 na branch `feat/docs-arquitetura-final`, HEAD `e0daa27765196dbabcbc2312fda39ba048bce5e2`; `origin/feat/docs-arquitetura-final` estava sincronizada. Os PRs consultados permaneceram abertos como Draft: PR #1 em `f37e95854e6964d86ec5606aa9ffc1a6fdc1a0e8` e PR #2 em `3705037666bb39ad0346de7e26fe4955084d13d9`.

| Fonte | Conteúdo encontrado | Tratamento necessário |
|---|---|---|
| `RTM.md` na raiz desta branch | Contém linhas para RF-01 a RF-14, uma seleção de requisitos críticos, riscos/testes propostos, RN-01, RN-05, RN-07 e itens RNF-01 a RNF-07/RNF-09. O resumo `1 / 9` e os status são preliminares e não refletem as evidências do PR #2. | Preservar requisitos, riscos e conteúdo exclusivo útil; não transportar os status preliminares como resultados verificados. |
| `docs/RTM.md` nesta branch | Contém linhas para RF-01 a RF-14, testes localizáveis, RN-01 a RN-08 e parte dos RNFs. Ainda registra 193 testes, JaCoCo 93,52%/77,27%, diz complementar a matriz da raiz e afirma que a interface visual não foi encontrada. | Não editar enquanto o PR #2 estiver aberto: o próprio arquivo é alterado por esse PR e seus dados já estão desatualizados em relação ao head dele. |
| `docs/RTM.md` no head do PR #2 | Atualiza a suíte para 202 testes, JaCoCo para 93,64%/78,57%, incorpora resultados JMeter e evidências de segurança/paginação. Mantém as 14 linhas RF e a revisão humana. | Preservar esta versão e sua evidência ao integrar o PR #2; validar a integração antes de alterar a matriz canônica. |
| Story 1.6 / frontend no PR #1 | O frontend foi implementado no head `f37e95854e6964d86ec5606aa9ffc1a6fdc1a0e8`; a matriz visual completa continua pendente. | Após integrar o PR #1, atualizar RF-13 para “implementação existente, validação visual parcial/pendente”, sem marcá-lo como verificado. |

**Decisão nesta revisão:** não modificar `RTM.md` nem `docs/RTM.md`. Alterar somente a matriz raiz para um encaminhamento agora deixaria incorreta a frase de `docs/RTM.md` que diz complementá-la; editar a matriz detalhada agora se sobreporia às alterações ainda abertas do PR #2. Portanto, a RTM **não está consolidada nem finalizada**. A adoção efetiva de `docs/RTM.md` como canônica, a preservação das linhas exclusivas da raiz e a correção do estado RF-13 ficam condicionadas à integração/revisão dos PRs #1 e #2.

1. Comparar a matriz resumida da raiz (`RTM.md`) com a matriz detalhada (`docs/RTM.md`). Ambas contêm requisitos e estados; a raiz tem linhas e riscos próprios, enquanto `docs/RTM.md` possui evidências por RF/RN e recebe as atualizações recentes do PR #2.
2. Usar `docs/RTM.md` como artefato canônico, conforme a story 1.5, depois de copiar para ela todo conteúdo exclusivo útil da matriz raiz. Preservar requisitos, riscos, evidências, estados e histórico de revisão; não descartar linhas da raiz por inferência.
3. Atualizar a matriz canônica com os resultados de 202 testes, JaCoCo 93,64%/78,57% e JMeter, usando as evidências do head integrado. Não marcar RF-13 visual, SonarCloud ou requisitos sem evidência como concluídos.
4. Preservar `RTM.md` como arquivo de compatibilidade com uma explicação e link para `docs/RTM.md`; não apagar o arquivo nem manter duas matrizes divergentes.
5. Alinhar resumo, status de revisão humana, links de CI e a referência à carga em ambas as matrizes. Os resultados antigos de 193 testes e 93,52%/77,27% devem permanecer identificados como históricos, se ainda forem relevantes.

## README — reconciliar após os PRs #1 e #2

1. Partir do `README.md` que já contém a seção do frontend incluída pelo PR #1; preservar instruções de execução, limites das APIs e armazenamento de token dessa seção.
2. Trocar a referência de RTM para a matriz canônica `docs/RTM.md` e manter o caminho raiz como compatibilidade.
3. Atualizar o checklist dos entregáveis: os diagramas, relatório final e roteiro de demonstração passam a estar disponíveis quando esta branch documental integrar; atualizar também o estado do plano/suíte de testes e da carga com links verificáveis.
4. Corrigir o texto que diz que as metas JaCoCo só serão bloqueantes no Commit 14: o gate já está configurado. Informar os resultados atuais apenas com os SHAs e evidências da execução correspondente.
5. Revisar a declaração de 100% de requisitos críticos junto com a RTM canônica; não manter percentual que contradiga o estado das evidências.
6. Descrever SonarCloud como preparado/pendente até uma análise remota comprovada e manter a validação visual responsiva pendente enquanto não houver matriz completa de viewport.
7. Adicionar links para `docs/architecture/`, `docs/quality/relatorio-final.md` e `docs/apresentacao/roteiro-demo.md`, sem substituir o conteúdo funcional da seção de frontend.

## Arquivos e fronteira de conflito

| Arquivo | Alteração paralela observada | Ação nesta branch |
|---|---|---|
| `README.md` | PR #1 adiciona a seção de interface web e limitações dos endpoints | Não modificar até o PR #1 ser integrado; preservar a seção e reconciliar checklist/links depois. |
| `docs/RTM.md` | PR #2 atualiza estados, suíte e resultados de carga | Não modificar até o PR #2 ser integrado; consolidar depois sem substituir o conteúdo desse PR. |
| `RTM.md` | Não alterado pelos PRs consultados, mas diverge de `docs/RTM.md` e é citado pelo README | Não o converter em ponte isoladamente; reconciliar junto com `docs/RTM.md` para evitar referência contraditória. |
| `docs/quality/plano-de-testes.md` | Não alterado pelos PRs #1 ou #2 | Atualizado nesta branch com os resultados recentes rotulados como pertencentes ao PR #2; valores antigos foram mantidos como histórico. |

Não foi feito merge, cherry-pick ou rebase dos PRs. Este arquivo é uma lista de reconciliação, não uma afirmação de que os PRs foram aprovados ou integrados.
