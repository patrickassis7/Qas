# PRD do produto

## Personas

As personas deste diretório representam os principais perfis de uso identificados para o aplicativo de estatísticas do Campeonato Brasileiro Série A. Elas são rascunhos iniciais, derivados do escopo informado, e não representam pesquisa com usuários.

- [Administrador](personas/administrador.md): administra a aplicação e suas operações de dados.
- [Torcedor](personas/torcedor.md): acompanha seu time, partidas e estatísticas.
- [Investidor](personas/investidor.md): acompanha audiência por time e desempenho no campeonato.

## Escopo conhecido

- O aplicativo atende torcedores de times da Série A do Brasil.
- O torcedor consulta confrontos, posição na competição, desempenho de jogadores e resultados recentes.
- O investidor consulta acessos por time e desempenho dos times no campeonato.
- O administrador tem responsabilidades administrativas e acesso relacionado ao banco de dados.

## Decisões pendentes

- O que conta como “acesso por time”: visualizações, usuários únicos, sessões ou outra métrica; e quais filtros de período estarão disponíveis.
- Como o “score do jogador” é calculado, qual a fonte dos dados e quando é atualizado.
- Qual edição e temporada da Série A será coberta e como serão tratados dados históricos.
- Quais ações e permissões compõem o acesso administrativo e o acesso ao banco de dados.
- Se haverá login, favoritos, notificações ou personalização por time; esses itens não são requisitos confirmados neste rascunho.

## Premissas de leitura

Os perfis descrevem objetivos e necessidades, não especificações técnicas nem decisões finais de autorização. Informações demográficas, comportamentos observados e métricas quantitativas não foram presumidos. Os detalhes devem ser validados com usuários e responsáveis pelo produto antes de orientar implementação.