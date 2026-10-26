# Persona: Administrador

**Perfil:** pessoa que mantém o cadastro e a disponibilidade dos recursos e dos usuários do sistema.

## Objetivos

- Gerenciar salas, professores, materiais e usuários.
- Criar bloqueios e períodos de manutenção.
- Manter o cadastro correto para que a detecção de conflitos funcione.

## Necessidades

- Cadastro e edição de recursos com validação de dados.
- Bloqueio de um recurso em manutenção, impedindo novas reservas no período.
- Gestão de usuários e perfis (Solicitante, Responsável, Administrador).
- Consulta ao histórico auditável e aos relatórios de utilização.

## Dificuldades a evitar

- Recurso em manutenção sendo reservado.
- Permissões excessivas para quem não precisa delas.
- Alterações de cadastro sem registro de quem as fez.

## Cenário de uso

Um projetor vai para manutenção. O administrador cria o bloqueio para o período e o sistema passa a recusar novas reservas dele nesse intervalo.

## Sinais de sucesso

- Recursos bloqueados nunca são reservados.
- Cadastros consistentes e rastreáveis.
- Relatórios de utilização, carga horária e conflitos evitados disponíveis.

## Origem

Perfil **Administrador** do enunciado do projeto.
