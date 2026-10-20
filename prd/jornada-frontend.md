# Jornada de experiência no frontend

**Status:** complemento detalhado da jornada principal em [Jornadas de uso](jornadas-de-uso.md), não uma fonte de verdade autônoma. Sua validação ocorre junto à jornada principal.

Este documento detalha somente ações da pessoa usuária e informações ou retornos percebidos no frontend da jornada principal. As personas e a jornada principal são suas fontes; referências a RFs no documento prematuro de [Requisitos funcionais](requisitos-funcionais.md) não são fonte de comportamento confirmado nem substituem a validação das jornadas. Não define telas, navegação, controles, conteúdo textual, dados, regras de acesso ou comportamento interno.

Quando uma apresentação é necessária para narrar um fluxo, ela é identificada como **ponto de experiência conceitual a validar**, não como tela ou requisito aprovado. Estados citados são situações reconhecidas pelas jornadas de origem; sua representação e tratamento visual continuam pendentes.

## Torcedor

**Objetivo:** acompanhar os próximos confrontos, a posição do time na competição, resultados recentes e desempenho ou score dos jogadores.

**Gatilho e contexto:** antes de uma partida, consulta o próximo adversário e a posição do time; depois do jogo, retorna para conferir o resultado e os scores dos jogadores. A forma de identificar o time de interesse não está definida.

| Etapa | Ação da pessoa | Informação ou retorno percebido no frontend | Próximo passo |
|---|---|---|---|
| 1. Antes da partida | Consulta os próximos confrontos e a posição do time de interesse. | Informações disponíveis sobre próximo confronto e posição do time. **Ponto de experiência conceitual a validar:** como apresentar os dados e suas referências sem presumir uma tela ou mecanismo para encontrar o time. (RF-01, RF-02) | Acompanhar o confronto e retornar após o jogo. |
| 2. Após a partida | Volta para consultar o resultado recente. | Resultado disponível associado ao time e à partida. A apresentação e a referência visível são pontos conceituais a validar; a jornada não define texto ou formato. (RF-03) | Consultar o desempenho ou score dos jogadores relacionado ao jogo. |
| 3. Consultar jogadores | Consulta o desempenho ou score dos jogadores. | Informação disponível associada aos jogadores. **Ponto de experiência conceitual a validar:** contexto para interpretar o score, pois sua definição não está aprovada. (RF-04) | Interpretar a informação somente na medida em que seu contexto vier a ser definido. |

**Estados reconhecidos pela jornada de origem:** próximo confronto inexistente ou não confirmado; classificação ou resultado atrasado, indisponível ou divergente; dados de jogadores ou score ainda indisponíveis; referências que não permitem confirmar que as informações pertencem à mesma competição, partida ou período.

**Estados e decisões pendentes:** não estão definidos a identificação do time, o recorte de temporada e histórico, a fonte e metodologia do score, nem como o frontend apresentará dados incertos ou sem contexto. A jornada não aprova seleção, favoritos, personalização ou uma resposta visual específica para esses casos.

**Critério de encerramento:** a pessoa consultou as informações disponíveis do time antes da partida e, após o jogo, pôde consultar o resultado e o desempenho ou score disponível. A jornada não depende de dados completos nem presume que o score já seja interpretável.

**Rastreabilidade:** [Persona Torcedor](personas/torcedor.md), objetivos, necessidades e cenário; [Jornadas de uso](jornadas-de-uso.md), seção Torcedor; [Requisitos funcionais](requisitos-funcionais.md), RF-01 a RF-04.

## Investidor

**Objetivo:** avaliar o alcance do aplicativo por time e acompanhar o desempenho dos times na competição. Comparar acessos é um objetivo explícito da persona, mas ainda não é comportamento aprovado no frontend.

**Gatilho e contexto:** durante o campeonato, consulta acessos associados aos times e informações sobre o desempenho deles. A jornada prevê comparação como intenção, enquanto a definição de acesso e de período permanece pendente.

| Etapa | Ação da pessoa | Informação ou retorno percebido no frontend | Próximo passo |
|---|---|---|---|
| 1. Consultar acessos | Consulta os acessos associados aos times. | Informações de acesso disponíveis, associadas aos times. **Ponto de experiência conceitual a validar:** como apresentar a associação e as referências conhecidas, sem presumir formato ou período. (RF-05) | Avaliar se os dados permitem comparação; a definição de acesso e dos períodos precisa ser validada primeiro. |
| 2. Comparar acessos | Tem o objetivo de comparar os acessos entre times sob uma definição e um período comuns. | Não há comportamento nem retorno de comparação aprovado para especificar. RF-09 consta como requisito sujeito a validação; unidade de acesso e períodos continuam indefinidos. Qualquer apresentação comparativa é apenas ponto de experiência conceitual a validar, não requisito confirmado. | Validar unidade, períodos e status de RF-09 antes de especificar interação ou resultado de comparação. |
| 3. Consultar desempenho | Consulta o desempenho dos times no campeonato. | Informações de desempenho disponíveis associadas aos times. **Ponto de experiência conceitual a validar:** apresentação dos indicadores e de seu contexto, pois indicadores e referências ainda não foram definidos. (RF-06) | Interpretar os dados em conjunto apenas se as referências forem compatíveis e sem inferir causalidade. |

**Estados reconhecidos pela jornada de origem:** definição de acesso ausente ou variável; dados ausentes, atrasados ou referidos a períodos diferentes; time sem dados comparáveis; desempenho indisponível ou sem competição/período identificável; risco de interpretar associação entre desempenho e acessos como causalidade.

**Estados e decisões pendentes:** significado de “acesso”, períodos, filtros ou formas de comparação, indicadores de desempenho, contexto temporal e nível de agregação. Em particular, a comparação continua não especificável como comportamento aprovado enquanto RF-09 estiver sujeito a validação e acesso/períodos não estiverem definidos. Esta jornada não presume filtros, dashboards, dados pessoais ou uma forma visual específica.

**Critério de encerramento:** a pessoa consultou os acessos por time e o desempenho disponível para embasar sua avaliação. Comparar acessos só poderá ser considerado encerramento de uma jornada aprovada depois da validação de RF-09 e das definições de acesso e período; não é condição confirmada nesta versão.

**Rastreabilidade:** [Persona Investidor](personas/investidor.md), objetivos, necessidades e cenário; [Jornadas de uso](jornadas-de-uso.md), seção Investidor; [Requisitos funcionais](requisitos-funcionais.md), RF-05 e RF-06. RF-09 é citado somente por seu status pendente, não como base de comportamento aprovado.

## Administrador

**Objetivo:** investigar falha de atualização, entender quais dados podem ter sido afetados e executar somente ações administrativas permitidas, buscando restabelecer a consistência.

**Gatilho e contexto:** uma atualização de estatísticas falha. A pessoa verifica o estado conhecido da operação e os dados afetados; quais ações são permitidas e como chega a esse contexto ainda não foram definidos.

| Etapa | Ação da pessoa | Informação ou retorno percebido no frontend | Próximo passo |
|---|---|---|---|
| 1. Investigar a ocorrência | Consulta o estado conhecido da atualização que falhou. | Estado conhecido da operação ou impossibilidade de confirmá-lo. **Ponto de experiência conceitual a validar:** como distinguir informação confirmada de informação inconclusiva. (RF-07) | Delimitar quais dados são identificados como afetados. |
| 2. Delimitar o impacto | Consulta quais dados estão relacionados à ocorrência. | Escopo conhecido do impacto, ou indicação conceitual de que não foi possível determiná-lo. (RF-07) | Avaliar como prosseguir dentro das ações que forem definidas como permitidas. |
| 3. Avaliar e agir | Decide se executa uma ação administrativa permitida para buscar restaurar a consistência. | Resultado conhecido da ação solicitada, se houver. **Ponto de experiência conceitual a validar:** quais ações aparecem à pessoa e como o resultado é apresentado; o catálogo de ações e as permissões não estão definidos. (RF-08) | Conferir o que pode ser confirmado sobre os dados relacionados à ocorrência. |
| 4. Conferir o resultado | Verifica se a consistência dos dados relacionados pôde ser confirmada. | O que pôde ser confirmado e o que permanece sem confirmação. **Ponto de experiência conceitual a validar:** a jornada não define evidências nem rótulos visuais para esses estados. | Encerrar se o impacto e o resultado conhecido da ação estiverem compreendidos; caso contrário, manter a ocorrência como não resolvida na jornada. |

**Estados reconhecidos pela jornada de origem:** falha de atualização; impacto parcial ou indeterminado; informação ausente ou inconclusiva; divergência ou consistência não confirmada; ação fora das atribuições ainda não definidas; resultado de recuperação não confirmado.

**Estados e decisões pendentes:** forma de acesso ao contexto operacional; ações administrativas existentes e quem pode executá-las; distinção visível entre estado desconhecido, dados incompletos e inconsistência; e evidência necessária para considerar a consistência confirmada. A jornada não presume correção automática, ação específica, permissão ou sucesso da recuperação.

**Critério de encerramento:** o administrador identificou o impacto conhecido e compreendeu o resultado observável de uma ação permitida, ou ficou explícito que o impacto ou resultado não pôde ser confirmado. Encerrar a consulta não equivale a afirmar que os dados foram recuperados.

**Rastreabilidade:** [Persona Administrador](personas/administrador.md), objetivos, necessidades e cenário; [Jornadas de uso](jornadas-de-uso.md), seção Administrador; [Requisitos funcionais](requisitos-funcionais.md), RF-07 e RF-08. RF-12 permanece proposta sujeita a validação e não fundamenta comportamento neste documento.

## Decisões de UX pendentes

- Como a pessoa torcedora identifica o time de interesse e como a interface contextualiza competição, partida e período.
- Como apresentar o score dos jogadores de modo interpretável, após validação de sua metodologia e contexto.
- Qual recorte de temporada e histórico será apresentado e como suas referências serão distinguidas.
- Como definir e apresentar acessos por time e períodos; se RF-09 for aprovado, como a comparação poderá ser compreendida sem sugerir equivalência ou causalidade não sustentada.
- Quais indicadores e referências de desempenho serão apresentados ao investidor e qual nível de agregação é adequado.
- Qual contexto de consulta, quais ações e quais resultados estarão visíveis ao administrador, depois de definidas as atribuições e permissões.
- Como representar informação disponível, ausente, atrasada, incompleta, divergente ou não confirmada sem inventar mensagens ou estados de produto.

As decisões acima são pendências para validação; não ampliam RF-01 a RF-08 nem transformam expectativas das jornadas em requisitos confirmados.
