# Persona: Administrador

**Perfil:** pessoa responsável pela administração operacional do aplicativo, com atribuições administrativas e acesso autorizado aos dados e ao banco de dados.

## Objetivos

- Manter a aplicação e os dados necessários ao produto operacionais e consistentes.
- Administrar as informações e configurações sob sua responsabilidade.
- Investigar problemas que afetem a disponibilidade ou a qualidade dos dados.

## Necessidades

- Uma visão clara do estado operacional e de eventuais falhas de atualização de dados.
- Ferramentas administrativas adequadas às tarefas que lhe forem atribuídas.
- Acesso ao banco de dados compatível com suas responsabilidades e com as regras de segurança do produto.
- Contexto suficiente para identificar a origem e o impacto de alterações nos dados.

## Dificuldades a evitar

- Não conseguir distinguir falha da aplicação de falha ou atraso na fonte de dados.
- Alterar dados sem visibilidade sobre o impacto da mudança.
- Ter permissões insuficientes para tarefas legítimas ou permissões excessivas sem necessidade operacional.

## Cenário de uso

Uma atualização de estatísticas falha. O administrador verifica o estado da operação, identifica quais dados foram afetados e toma as ações administrativas permitidas para restaurar a consistência.

## Sinais de sucesso

- Consegue identificar problemas operacionais e dados afetados sem investigação manual desnecessária.
- Consegue executar suas tarefas autorizadas com clareza sobre o resultado das alterações.
- Os dados exibidos no produto permanecem consistentes após ações administrativas.

## Pontos a validar

- Quais funções administrativas são necessárias e quem pode executá-las.
- Se o acesso ao banco de dados será direto ou mediado por ferramentas administrativas.
- Quais operações exigem registro de auditoria, aprovação ou recuperação.