# PRD do produto

## Fluxo de documentação

1. **Personas e objetivos:** os candidatos a persona para a alocação de salas, professores e materiais estão registrados como rascunhos sujeitos a validação: [coordenação da alocação](personas/coordenacao-alocacao.md), [professor](personas/professor-candidato.md) e [gestão de salas e materiais](personas/gestao-salas-materiais-candidata.md). A participação dos papéis ainda não está confirmada. As personas antigas pertencem ao domínio de futebol e não devem ser presumidas como válidas para este produto.
2. **Jornadas:** depois de definir personas e objetivos, serão construídas novas jornadas que cubram frontend e retaguarda em nível conceitual, sem definir tecnologias.
3. **Validação das jornadas:** as jornadas precisam de validação e aprovação explícitas antes de derivar requisitos.
4. **Requisitos:** somente depois da aprovação das jornadas serão derivados, em conjunto, requisitos funcionais (RFs) e não funcionais (NFRs).
5. **Implementação:** nenhum código está autorizado antes da aprovação da documentação aplicável. Somente após essa aprovação será liberado o próximo passo de implementação.

## Artefatos históricos fora do contexto vigente

As personas, jornadas, requisitos e arquiteturas existentes foram produzidos para o domínio de estatísticas de futebol. Estão obsoletos e fora do contexto vigente deste produto. Os arquivos são preservados como registro histórico, mas não são fontes ativas nem autoridade para definir o novo produto. Não devem orientar requisitos, arquitetura ou implementação atuais.

- Personas históricas de futebol (obsoletas): [Administrador](personas/administrador.md), [Torcedor](personas/torcedor.md) e [Investidor](personas/investidor.md).
- Jornadas históricas de futebol (obsoletas): [Jornadas de uso](jornadas-de-uso.md) e [Jornada frontend](jornada-frontend.md).
- Requisitos históricos de futebol (obsoletos): [Requisitos funcionais](requisitos-funcionais.md).
- Arquiteturas históricas de futebol (obsoletas): [Arquitetura conceitual agnóstica](arquitetura-agnostica.md) e [Modelagem arquitetural](modelagem-arquitetural.md).

## Escopo conhecido

- A aplicação deve organizar a alocação de salas, professores e materiais sem conflitos de horário.
- A avaliação do projeto deve apresentar evidências objetivas de qualidade, segurança, rastreabilidade e desempenho.

## Decisões pendentes

- Se a pessoa coordenadora/responsável pela alocação é usuária do sistema e se os papéis de professor e responsável pela gestão de salas e materiais participam; validar os objetivos de cada papel.
- Como validar e definir a semântica de “conflito de horário”, sem pressupor regras ou critérios de conflito.
- Como disponibilidade e agendamento se aplicam ao contexto, sem presumir políticas ou comportamento.
- Quais serão as jornadas de frontend e de retaguarda conceitual, a construir depois da definição de personas e objetivos.
- Quais critérios e evidências serão usados para avaliar qualidade, segurança, rastreabilidade e desempenho.
- Quais políticas de segurança, prioridades de alocação, métricas, limites ou benchmarks de desempenho serão aplicáveis; nenhum foi definido.
- Quais tecnologias serão adotadas; nenhuma tecnologia foi definida neste documento.

## Premissas de leitura

O escopo confirmado limita-se à capacidade de organizar a alocação de salas, professores e materiais sem conflitos de horário e à expectativa de apresentar evidências objetivas de qualidade, segurança, rastreabilidade e desempenho na avaliação. A validade e o significado de “conflito” ainda precisam ser definidos. Não foram presumidos personas, jornadas, prioridades, regras, políticas, métricas, limites, benchmarks ou tecnologias. As jornadas deverão ser validadas explicitamente; RFs e NFRs serão elaborados juntos somente depois dessa validação, e a implementação depende de autorização documental.