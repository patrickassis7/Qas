# Jornadas de uso do aplicativo

**Status:** jornada atual em rascunho e artefato ativo do fluxo de produto. Abrange a experiência de frontend e a retaguarda em nível conceitual, sem definir tecnologias. A aprovação explícita desta jornada vem antes da elaboração conjunta de requisitos funcionais e não funcionais.

Este documento descreve jornadas de uso para as personas de administrador, investidor e torcedor, incluindo a experiência visível no aplicativo e as atividades de retaguarda em nível conceitual. As jornadas organizam os cenários e objetivos já registrados; não confirmam funcionalidades, políticas ou processos ainda não definidos.

## Como ler as jornadas

- **Fato existente:** informação declarada em `prd/README.md` ou nos documentos das personas.
- **Hipótese de narrativa:** ligação necessária para descrever uma sequência completa, ainda sem confirmação nos artefatos do produto.
- **Ação do usuário:** iniciativa ou decisão da pessoa usuária.
- **Resposta visível:** informação ou retorno que a pessoa percebe no aplicativo; quando ainda não confirmado, está marcado como expectativa a validar.
- **Retaguarda conceitual:** atividade de negócio ou tratamento de dados que pode ser necessário para a jornada, sem especificar tecnologia, arquitetura ou implementação.

As descrições de estado (disponível, atrasado, incompleto, inconsistente ou indisponível) servem para tornar visíveis os casos que precisam ser discutidos. Não definem regras, prazos, limites ou políticas de tratamento. Login, favoritos, notificações, personalização, permissões e mecanismos administrativos não são presumidos.

## 1. Administrador

### Objetivo

Investigar uma falha na atualização de estatísticas, entender quais dados podem ter sido afetados e executar somente as ações administrativas que vierem a ser autorizadas, buscando restabelecer a consistência.

### Gatilho e contexto

**Fato existente:** uma atualização de estatísticas falha; o administrador verifica o estado da operação e os dados afetados e toma ações administrativas permitidas para restaurar a consistência.

**Hipótese de narrativa:** a pessoa chega a um contexto de consulta operacional relacionado à falha. A existência, o nome e o modo de acesso a esse contexto ainda não foram definidos.

### Etapas

| Etapa | Ação do usuário | Resposta visível do sistema | Retaguarda conceitual |
|---|---|---|---|
| 1. Investigar a ocorrência | Procura o estado da atualização que falhou. | Expectativa a validar: apresenta o estado conhecido da operação e deixa claro quando não há informação suficiente para confirmá-lo. | Identificar o registro da operação e distinguir falha confirmada de estado desconhecido, se esses dados estiverem disponíveis. |
| 2. Delimitar o impacto | Consulta quais estatísticas ou dados estão relacionados à ocorrência. | Expectativa a validar: indica o escopo conhecido do impacto e diferencia dados confirmados, incompletos ou sem estado verificável. | Verificar quais dados foram afetados pela operação e se há divergências relevantes entre o estado esperado e o disponível. |
| 3. Avaliar a ação possível | Decide como prosseguir dentro das atribuições que lhe forem concedidas. | Expectativa a validar: informa se a ação escolhida pode ser executada e apresenta o resultado, sem sugerir permissões ainda não definidas. | Avaliar a ação solicitada conforme as atribuições que o produto vier a definir; registrar o resultado operacional quando aplicável, decisão ainda pendente. |
| 4. Conferir o estado após a ação | Verifica se os dados relacionados voltaram a apresentar um estado consistente. | Expectativa a validar: mostra o que pôde ser confirmado e sinaliza qualquer impacto que permaneça sem resolução conhecida. | Reavaliar a consistência dos dados envolvidos e manter identificável o que não pôde ser confirmado. |

### Resultado esperado

O administrador identifica a falha e seu impacto conhecido, entende o resultado das ações que lhe forem permitidas e consegue distinguir dados consistentes de dados ainda incertos. A jornada não presume uma ação específica de recuperação nem garante que toda falha possa ser corrigida pelo aplicativo.

### Exceções e estados relevantes

- Não há informação suficiente para localizar ou confirmar a operação que falhou.
- A atualização afetou apenas parte dos dados, ou o alcance do impacto não pode ser determinado.
- Os dados disponíveis apresentam divergências ou não permitem confirmar consistência.
- A ação desejada não está entre as atribuições da pessoa, ou as atribuições ainda não foram definidas.
- A tentativa de recuperação não pode ser confirmada como concluída.

Em qualquer desses casos, a narrativa não presume que o sistema corrija dados automaticamente nem que o administrador possa contornar uma restrição. O resultado deve permanecer descrito como desconhecido ou pendente de decisão, até validação do processo e das responsabilidades.

## 2. Investidor

### Objetivo

Comparar os acessos associados aos times e consultar o desempenho desses times na competição para contextualizar a avaliação do alcance do aplicativo.

### Gatilho e contexto

**Fato existente:** durante o campeonato, a pessoa avalia o alcance do aplicativo comparando acessos por time e consultando o desempenho dos times.

**Hipótese de narrativa:** a pessoa encontra no aplicativo uma área de consulta que reúne ou permite consultar essas informações. A forma de entrada, o nível de acesso e a organização da área ainda não foram definidos.

### Etapas

| Etapa | Ação do usuário | Resposta visível do sistema | Retaguarda conceitual |
|---|---|---|---|
| 1. Consultar a audiência | Procura os dados de acesso associados aos times. | Expectativa a validar: apresenta os dados disponíveis e sua definição e referência temporal, se essas informações tiverem sido estabelecidas. | Organizar os dados de audiência disponíveis por time e preservar a definição e o período a que se referem, quando conhecidos. |
| 2. Comparar os times | Compara os acessos entre times sob uma mesma definição e referência temporal. Se o produto oferecer filtros, usa condições equivalentes para a comparação. | Expectativa a validar: deixa visíveis diferenças de cobertura, período ou definição que possam tornar a comparação inválida. | Verificar se os dados comparados são compatíveis quanto à definição de acesso e ao período; a métrica e os filtros ainda são decisões pendentes. |
| 3. Consultar o desempenho esportivo | Consulta os indicadores de desempenho dos times na competição. | Expectativa a validar: apresenta os indicadores disponíveis com o contexto temporal e a fonte, quando definidos. | Disponibilizar os dados esportivos associados aos times e à referência de competição conhecida, sem presumir fonte ou método de cálculo. |
| 4. Interpretar em conjunto | Usa audiência e desempenho como contexto para sua avaliação, sem concluir que um causou o outro. | Expectativa a validar: mantém claros o contexto e as limitações dos dados para evitar comparação entre referências incompatíveis. | Preservar as referências de cada conjunto de dados e sinalizar lacunas ou incompatibilidades conhecidas. |

### Resultado esperado

O investidor consegue interpretar e comparar os acessos por time usando uma definição e um período comuns, consultar o desempenho esportivo com contexto e fazer sua própria avaliação sem depender de informações pessoais de torcedores. A definição de acesso, os indicadores e as referências temporais permanecem por validar.

### Exceções e estados relevantes

- A definição de “acesso” não está estabelecida ou varia entre os dados apresentados.
- Há dados ausentes, atrasados ou com períodos de referência diferentes.
- Um time não tem dados comparáveis de audiência ou desempenho.
- Os dados de desempenho esportivo estão indisponíveis ou não identificam claramente a competição ou o período.
- A apresentação conjunta pode induzir a uma relação causal entre desempenho e acessos que os dados não estabelecem.

Se a comparabilidade não puder ser confirmada, a jornada não presume ranking, preenchimento de lacunas, estimativas ou conclusões causais.

## 3. Torcedor

### Objetivo

Consultar os próximos confrontos do time, sua posição na competição, os resultados recentes e o desempenho ou score dos jogadores.

### Gatilho e contexto

**Fato existente:** antes de uma partida, o torcedor consulta o próximo adversário e a posição do time; depois do jogo, volta para conferir o resultado e os scores dos jogadores.

**Hipótese de narrativa:** a pessoa consegue chegar ao contexto do time que deseja acompanhar. Como o time é escolhido ou identificado não está definido; esta jornada não pressupõe login, favorito ou personalização.

### Etapas

| Etapa | Ação do usuário | Resposta visível do sistema | Retaguarda conceitual |
|---|---|---|---|
| 1. Chegar ao contexto do time | Acessa as informações do time de interesse. | Expectativa a validar: identifica claramente a qual time e referência de competição as informações exibidas pertencem. | Relacionar as informações disponíveis ao time e à competição correspondentes, quando essas referências forem conhecidas. |
| 2. Consultar antes da partida | Procura o próximo confronto e a posição do time na competição. | Expectativa a validar: apresenta os dados disponíveis com a referência de partida ou competição; deixa claro quando não há informação confirmada. | Disponibilizar calendário e classificação conforme os dados existentes e suas referências; não presumir frequência de atualização ou temporada coberta. |
| 3. Voltar após o jogo | Retorna para conferir o resultado recente e o desempenho ou score dos jogadores. | Expectativa a validar: apresenta resultado e informações de jogadores associados à partida correta, ou informa que ainda não estão disponíveis. | Associar os dados disponíveis à partida e aos jogadores correspondentes; a fonte e a metodologia do score permanecem pendentes. |
| 4. Interpretar os dados | Usa a referência de partida e período para entender o resultado e o score. | Expectativa a validar: fornece contexto suficiente para a pessoa saber a que partida ou período os dados se referem; quando a interpretação do score não estiver definida, essa lacuna permanece explícita. | Preservar a associação entre dados, partida e período conhecidos e identificar informação cuja referência não possa ser confirmada. |

### Resultado esperado

O torcedor encontra os próximos jogos, a posição do time e os resultados recentes em um mesmo contexto de consulta, identifica a referência dos dados e consegue compreender o score dos jogadores quando sua metodologia tiver sido validada. A jornada não presume que o score já tenha critérios definidos.

### Exceções e estados relevantes

- Não há próximo confronto ou a informação de calendário disponível não está confirmada.
- A classificação ou o resultado está atrasado, indisponível ou diverge de outra informação disponível.
- O resultado está disponível, mas os dados de jogadores ou seus scores ainda não estão.
- O score não tem fonte, período ou explicação definidos.
- Não é possível confirmar que calendário, classificação, resultado e score se referem à mesma competição, partida ou período.
- O mecanismo para escolher ou identificar o time ainda não está definido.

Quando as referências forem incertas, a jornada não presume que o aplicativo escolha um dado como correto, complete valores ausentes ou atualize as informações em um prazo específico.

## Dúvidas e decisões a validar

Os pontos abaixo vêm das decisões pendentes do `prd/README.md` e das lacunas explicitadas nas personas. Eles não são decisões tomadas neste documento.

| Tema | Perguntas a validar |
|---|---|
| Acessos por time | O que conta como acesso (por exemplo, visualização, usuário único, sessão ou outra definição)? Quais períodos e filtros serão disponibilizados? Como indicar cobertura e comparabilidade? |
| Score de jogador | Qual é a fonte, como o score é calculado e quando é atualizado? Como será explicado e contextualizado para o torcedor? |
| Competição e histórico | Qual edição e temporada da Série A será coberta? Haverá dados históricos e como serão distinguidos das informações da edição corrente? |
| Administração | Quais tarefas, ações e permissões existem? Quem pode executá-las? O acesso relacionado ao banco de dados será direto ou mediado? Quais operações exigem registro, aprovação ou recuperação? |
| Identificação do time | Como o torcedor encontra ou seleciona o time de interesse? Não está decidido se haverá personalização, favoritos ou login. |
| Dados e referências | Quais fontes fornecem calendário, classificação, resultados e desempenho? Como serão comunicados dados ausentes, atrasados, incompletos ou inconsistentes? Há prazos de atualização a definir? |
| Acesso do investidor | O acesso será apenas agregado ou haverá outros níveis? Como preservar a necessidade de não expor informações pessoais? |
| Apresentação e entrada nas jornadas | Quais áreas, filtros e pontos de entrada existirão no aplicativo? As jornadas não pressupõem telas, navegação ou forma de acesso específicas. |

## Riscos de produto associados às lacunas

- Sem definição de acesso e período, comparações de audiência podem ser interpretadas de maneiras diferentes ou parecer equivalentes sem serem.
- Sem referência confiável de atualização e origem, dados atrasados ou divergentes podem reduzir a confiança nas consultas do torcedor e do investidor.
- Sem metodologia explicável, o score pode ser interpretado como avaliação objetiva mesmo quando sua origem e seu significado não forem conhecidos.
- Sem delimitação das atribuições administrativas, a pessoa pode não conseguir resolver uma ocorrência legítima ou pode receber atribuições mais amplas do que o necessário.

## Próxima etapa

Validar e aprovar explicitamente estas jornadas com responsáveis pelo produto e, quando possível, com representantes das personas. **Somente após essa aprovação serão elaborados em conjunto os requisitos funcionais e não funcionais.** Nenhum código está autorizado antes da aprovação da documentação aplicável. Este documento não define requisitos, políticas de acesso, métricas, fontes, fórmulas, prazos, tecnologias ou soluções de implementação.