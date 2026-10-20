# Requisitos funcionais preliminares

> **Status: rascunho prematuro, fora de sequência e não aprovado.** Este conteúdo foi produzido antes da validação formal das jornadas; não é baseline aprovado e não deve orientar arquitetura ou código. Após a aprovação da jornada, os requisitos funcionais e não funcionais serão elaborados em conjunto, e este arquivo deverá ser revisto ou refeito. Nenhum requisito não funcional é acrescentado nesta versão.

**Status anterior, não vigente:** base preliminar produzida para validação e arquitetura. Não é baseline aprovado nem especificação técnica e não deve orientar decisões antes da revisão prevista no fluxo atual.

Este documento deriva exclusivamente do escopo e das personas em [`README.md`](README.md), dos documentos das personas e das [jornadas de uso](jornadas-de-uso.md). Como jornadas e personas são rascunhos, os requisitos abaixo distinguem capacidades sustentadas diretamente pelas fontes de propostas ainda sujeitas a validação. A inclusão de uma proposta não confirma sua adoção.

## Requisitos diretamente sustentados

Os requisitos desta seção refletem objetivos ou cenários declarados nas fontes. As questões que afetam seu recorte ou comportamento permanecem listadas em “Questões em aberto”.

### RF-01 — Consultar próximos confrontos

- **Descrição:** O aplicativo deve permitir ao torcedor consultar os próximos confrontos do time de interesse.
- **Persona:** Torcedor.
- **Sustentação:** Direta.
- **Origem/trilha:** Escopo em [`README.md`](README.md), “Escopo conhecido”; [persona Torcedor](personas/torcedor.md), “Objetivos” e “Cenário de uso”; [jornadas](jornadas-de-uso.md), “Torcedor”, etapas 1–2.
- **Critério de aceite observável:** Dado um time com próximo confronto registrado nos dados disponíveis, ao consultar os confrontos desse time, o próximo adversário é identificável na informação apresentada.

### RF-02 — Consultar posição na competição

- **Descrição:** O aplicativo deve permitir ao torcedor consultar a posição do time de interesse na competição.
- **Persona:** Torcedor.
- **Sustentação:** Direta.
- **Origem/trilha:** Escopo em [`README.md`](README.md), “Escopo conhecido”; [persona Torcedor](personas/torcedor.md), “Objetivos” e “Cenário de uso”; [jornadas](jornadas-de-uso.md), “Torcedor”, etapas 1–2.
- **Critério de aceite observável:** Dado um time com posição disponível para a competição consultada, a consulta apresenta a posição associada a esse time.

### RF-03 — Consultar resultados recentes

- **Descrição:** O aplicativo deve permitir ao torcedor consultar resultados recentes dos jogos do time de interesse.
- **Persona:** Torcedor.
- **Sustentação:** Direta.
- **Origem/trilha:** Escopo em [`README.md`](README.md), “Escopo conhecido”; [persona Torcedor](personas/torcedor.md), “Objetivos” e “Cenário de uso”; [jornadas](jornadas-de-uso.md), “Torcedor”, etapas 3–4.
- **Critério de aceite observável:** Dada uma partida recente do time com resultado disponível, a consulta permite identificar o resultado associado à partida.

### RF-04 — Consultar desempenho ou score dos jogadores

- **Descrição:** O aplicativo deve permitir ao torcedor consultar o desempenho ou score dos jogadores, sem que este requisito estabeleça metodologia, fonte ou interpretação do score.
- **Persona:** Torcedor.
- **Sustentação:** Direta para a consulta; definição e interpretação do score pendentes.
- **Origem/trilha:** Escopo em [`README.md`](README.md), “Escopo conhecido”; [persona Torcedor](personas/torcedor.md), “Objetivos” e “Cenário de uso”; [jornadas](jornadas-de-uso.md), “Torcedor”, etapa 3.
- **Critério de aceite observável:** Quando houver informação de desempenho ou score disponível para jogadores, a consulta permite identificar a informação associada aos jogadores apresentados.

### RF-05 — Consultar acessos associados aos times

- **Descrição:** O aplicativo deve permitir ao investidor consultar os acessos associados aos times.
- **Persona:** Investidor.
- **Sustentação:** Direta para consulta por time; definição da unidade de acesso e dos períodos pendente.
- **Origem/trilha:** Escopo em [`README.md`](README.md), “Escopo conhecido”; [persona Investidor](personas/investidor.md), “Objetivos” e “Cenário de uso”; [jornadas](jornadas-de-uso.md), “Investidor”, etapas 1–2.
- **Critério de aceite observável:** Para cada time com dados de acesso disponíveis, a consulta permite identificar a que time os dados apresentados estão associados.

### RF-06 — Consultar desempenho dos times na competição

- **Descrição:** O aplicativo deve permitir ao investidor consultar o desempenho dos times no campeonato.
- **Persona:** Investidor.
- **Sustentação:** Direta para consulta; indicadores, fonte e referência temporal pendentes.
- **Origem/trilha:** Escopo em [`README.md`](README.md), “Escopo conhecido”; [persona Investidor](personas/investidor.md), “Objetivos” e “Cenário de uso”; [jornadas](jornadas-de-uso.md), “Investidor”, etapas 3–4.
- **Critério de aceite observável:** Para cada time com informação de desempenho disponível, a consulta permite identificar a que time a informação apresentada se refere.

### RF-07 — Investigar atualização de estatísticas e dados afetados

- **Descrição:** No cenário de falha de atualização de estatísticas, o administrador deve poder verificar o estado da operação e identificar quais dados foram afetados, dentro das informações disponíveis.
- **Persona:** Administrador.
- **Sustentação:** Direta para o cenário descrito; dados apresentados e forma de acesso ainda não definidos.
- **Origem/trilha:** [persona Administrador](personas/administrador.md), “Cenário de uso” e “Necessidades”; [jornadas](jornadas-de-uso.md), “Administrador”, gatilho e etapas 1–2.
- **Critério de aceite observável:** No cenário de uma atualização de estatísticas com falha, o administrador consegue consultar o estado conhecido da operação e os dados identificados como afetados, sem que este requisito determine como tratar informação ausente ou inconclusiva.

### RF-08 — Executar ações administrativas autorizadas

- **Descrição:** No cenário de falha de atualização, o administrador deve poder executar as ações administrativas que lhe forem permitidas para buscar restaurar a consistência dos dados.
- **Persona:** Administrador.
- **Sustentação:** Direta para ações permitidas no cenário; catálogo de ações e permissões pendentes.
- **Origem/trilha:** [persona Administrador](personas/administrador.md), “Cenário de uso”; [jornadas](jornadas-de-uso.md), “Administrador”, objetivo e etapas 3–4.
- **Critério de aceite observável:** Para uma ação administrativa definida e autorizada, o administrador consegue solicitá-la e observar o resultado conhecido da ação, sem executar por este requisito ações fora das permissões que vierem a ser definidas.

## Requisitos sujeitos a validação

Os itens abaixo são propostas derivadas de necessidades ou expectativas marcadas para validação nas fontes. Não devem ser tratados como comportamento confirmado até que as decisões correspondentes sejam tomadas.

### RF-09 — Comparar acessos entre times

- **Descrição proposta:** Avaliar se o investidor deve poder comparar acessos de diferentes times usando a mesma definição de acesso e o mesmo período de referência.
- **Persona:** Investidor.
- **Sustentação:** A validar; a necessidade de comparabilidade aparece na persona, mas métrica, períodos e filtros não estão definidos.
- **Origem/trilha:** [persona Investidor](personas/investidor.md), “Necessidades”, “Dificuldades a evitar” e “Sinais de sucesso”; [jornadas](jornadas-de-uso.md), “Investidor”, etapa 2 e decisões a validar.
- **Critério de aceite observável, se aprovado:** Em uma comparação aprovada para validação, os times comparados são apresentados sob a mesma definição de acesso e referência temporal. Este critério não define filtros ou opções de período.

### RF-10 — Identificar referências de competição, partida e período

- **Descrição proposta:** Avaliar se as consultas do torcedor devem apresentar referência suficiente para identificar a competição, partida ou período a que cada dado se refere.
- **Persona:** Torcedor.
- **Sustentação:** A validar; a necessidade de contexto consta na persona, mas a temporada coberta e o tratamento histórico não foram decididos.
- **Origem/trilha:** [persona Torcedor](personas/torcedor.md), “Necessidades” e “Dificuldades a evitar”; [jornadas](jornadas-de-uso.md), “Torcedor”, etapas 1–4 e decisões a validar.
- **Critério de aceite observável, se aprovado:** Em uma consulta aprovada, uma pessoa consegue associar cada dado apresentado à referência de competição, partida ou período definida para aquele dado.

### RF-11 — Consultar indicador de desempenho com contexto

- **Descrição proposta:** Avaliar se as informações de desempenho esportivo consultadas pelo investidor devem apresentar a fonte e a referência temporal validadas para cada indicador.
- **Persona:** Investidor.
- **Sustentação:** A validar; a persona solicita contexto temporal e fonte identificável, mas os indicadores e as fontes ainda não estão definidos.
- **Origem/trilha:** [persona Investidor](personas/investidor.md), “Necessidades”; [jornadas](jornadas-de-uso.md), “Investidor”, etapas 3–4 e decisões a validar.
- **Critério de aceite observável, se aprovado:** Para cada indicador aprovado e apresentado, a pessoa consegue identificar a fonte e a referência temporal escolhidas para esse indicador.

### RF-12 — Verificar consistência após ação administrativa

- **Descrição proposta:** Avaliar se, após ação administrativa, o administrador deve consultar quais dados tiveram consistência confirmada e quais permanecem sem confirmação.
- **Persona:** Administrador.
- **Sustentação:** A validar; a jornada descreve essa expectativa, sem definir evidências, estados ou processo de confirmação.
- **Origem/trilha:** [persona Administrador](personas/administrador.md), “Objetivos” e “Sinais de sucesso”; [jornadas](jornadas-de-uso.md), “Administrador”, etapas 3–4 e exceções.
- **Critério de aceite observável, se aprovado:** Após uma ação administrativa definida, a consulta distingue os dados cuja consistência foi confirmada dos que não puderam ser confirmados, conforme regra que venha a ser aprovada.

## Questões em aberto

Estas decisões não são resolvidas por requisitos deste documento. As respostas podem alterar o escopo, a redação ou a testabilidade dos RFs relacionados.

| ID | Questão | RFs relacionados | Trilha nas fontes |
|---|---|---|---|
| OQ-01 | O que conta como “acesso” por time: visualização, usuário único, sessão ou outra unidade? | RF-05, RF-09 | [`README.md`](README.md), “Decisões pendentes”; [persona Investidor](personas/investidor.md), “Pontos a validar”. |
| OQ-02 | Quais filtros e períodos de referência estarão disponíveis para consulta e comparação de acessos? | RF-05, RF-09 | [`README.md`](README.md), “Decisões pendentes”; [persona Investidor](personas/investidor.md), “Pontos a validar”. |
| OQ-03 | Qual a fonte e a metodologia do score de jogador, e como será explicado ao torcedor? | RF-04 | [`README.md`](README.md), “Decisões pendentes”; [persona Torcedor](personas/torcedor.md), “Necessidades” e “Pontos a validar”. |
| OQ-04 | Qual edição/temporada da Série A será coberta e haverá dados históricos? Como distinguir temporada atual e histórica? | RF-01, RF-02, RF-03, RF-06, RF-10, RF-11 | [`README.md`](README.md), “Decisões pendentes”; [personas Torcedor](personas/torcedor.md) e [Investidor](personas/investidor.md), “Pontos a validar”. |
| OQ-05 | Como o torcedor escolhe ou identifica o time de interesse? | RF-01, RF-02, RF-03, RF-04, RF-10 | [persona Torcedor](personas/torcedor.md), “Pontos a validar”; [jornadas](jornadas-de-uso.md), “Torcedor”, gatilho e contexto. |
| OQ-06 | Quais funções administrativas e ações existem, quem pode executá-las e qual é o escopo do acesso relacionado ao banco de dados? | RF-07, RF-08, RF-12 | [`README.md`](README.md), “Decisões pendentes”; [persona Administrador](personas/administrador.md), “Pontos a validar”; [jornadas](jornadas-de-uso.md), “Administrador”. |
| OQ-07 | Quais permissões se aplicam ao administrador e ao investidor, e quais ações exigem registro, aprovação ou recuperação? | RF-05, RF-06, RF-08, RF-12 | [personas Administrador](personas/administrador.md) e [Investidor](personas/investidor.md), “Pontos a validar”; [jornadas](jornadas-de-uso.md), “Administrador” e “Investidor”. |
| OQ-08 | Quais são as fontes dos dados de calendário, classificação, resultados, jogadores, desempenho esportivo e acessos? | RF-01 a RF-07, RF-11 | [`README.md`](README.md), “Decisões pendentes”; [jornadas](jornadas-de-uso.md), decisões a validar de cada persona. |
| OQ-09 | Como e quando cada conjunto de dados é atualizado, e como serão identificados dados atrasados, incompletos, indisponíveis ou inconsistentes? | RF-01 a RF-07, RF-11, RF-12 | [persona Administrador](personas/administrador.md), “Necessidades”; [jornadas](jornadas-de-uso.md), exceções e estados relevantes; [`README.md`](README.md), “Decisões pendentes”. |
| OQ-10 | Quais indicadores definem “desempenho do time” para o investidor? | RF-06, RF-11 | [persona Investidor](personas/investidor.md), “Pontos a validar”. |
| OQ-11 | Os dados disponíveis ao investidor serão somente agregados ou haverá outros níveis de acesso? | RF-05, RF-06, RF-09 | [persona Investidor](personas/investidor.md), “Necessidades” e “Pontos a validar”; [`README.md`](README.md), “Decisões pendentes”. |
| OQ-12 | Quais estados e evidências permitem afirmar que uma operação ou dado está consistente após uma ação administrativa? | RF-07, RF-08, RF-12 | [persona Administrador](personas/administrador.md), “Sinais de sucesso”; [jornadas](jornadas-de-uso.md), “Administrador”, etapas 2–4 e exceções. |

## Limites deste documento

Este documento não define requisitos não funcionais, tecnologias, arquitetura, infraestrutura, interfaces técnicas, métricas não decididas, fórmulas, prazos de atualização, autenticação, favoritos, notificações ou comportamentos de erro. Situações não resolvidas pelas fontes permanecem como questões em aberto, sem resposta normativa.