# Reconciliação documental após integração dos PRs #1 e #2

**Atualização:** 2026-11-09. **Branch documental:** `feat/docs-arquitetura-final`. **Revisão integrada validada localmente:** `d4b02bb`.

## RTM — resolvida nesta entrega

- Os PRs #2 (merge `87903e1`) e #1 (merge `8e5fb34`) estão incorporados em `develop`; ambos também foram trazidos à branch documental pelo merge `d4b02bb`.
- `docs/RTM.md` é a única matriz oficial e contém RF-01 a RF-14 com risco, teste/evidência localizável e status. Também reúne RN-01 a RN-08 e os RNFs preservados das matrizes anteriores.
- A informação útil da RTM raiz foi incorporada: riscos/casos propostos, subconjunto originalmente marcado crítico e requisitos RNF-01 a RNF-07/RNF-09. Status antigos e o resumo preliminar `1 / 9` não foram tratados como resultados.
- RF-13 agora registra frontend implementado e integrado, com validação visual completa nos seis tamanhos ainda pendente. SonarCloud, SLO de latência e evidência revisável de TDD/BDD não foram declarados concluídos.
- `RTM.md` foi mantido como encaminhamento para `docs/RTM.md`; não há duas matrizes independentes.
- Validação executada no commit `d4b02bb`: `clean verify`, 202 testes, 0 falhas/erros/ignorados; JaCoCo 93,64% linhas e 78,57% branches. A execução JMeter e seus artefatos sanitizados estão identificados separadamente na RTM e em `docs/quality/evidencias/jmeter-commit15-2026-11-09.md`.

Esta reconciliação registra o que foi resolvido, sem converter cobertura de código em percentual de requisitos atendidos e sem alegar aprovação global.

## README — atualizado

O README principal foi revisado contra Compose, configuração Spring, segurança, controllers, RTM, ADRs e diagramas. Agora aponta para `docs/RTM.md` como matriz oficial, mantém `RTM.md` como compatibilidade, registra instruções para PowerShell e Bash e separa o que foi executado do que continua pendente. Nenhuma credencial foi adicionada. As limitações visual, SonarCloud e SLO permanecem abertas.

## Limitações ainda abertas

- A matriz visual completa do frontend em 320, 375, 390, 768, 1024 e 1440 px não foi comprovada.
- SonarCloud é responsabilidade de outro integrante e continua sem execução comprovada.
- O docente ainda não acordou um SLO/meta de latência.
- RF-14 tem documentação existente, mas a revisão completa de README/contratos segue para a entrega documental subsequente.
- Resultados de CI citados na RTM são checks dos heads dos PRs #1 e #2; não se afirma execução de CI para o commit documental `d4b02bb`.
