# Persona: Responsável

**Perfil:** pessoa que aprova solicitações especiais, valida a alocação de docentes e acompanha retiradas e devoluções de material.

## Objetivos

- Aprovar ou rejeitar solicitações de recursos restritos.
- Validar a alocação de docentes sem sobreposição na agenda do professor.
- Registrar retirada e devolução de materiais e equipamentos.

## Necessidades

- Fila de solicitações pendentes com o contexto necessário para decidir.
- Visão da agenda do professor para identificar sobreposições.
- Registro simples de retirada e devolução, com data e hora.
- Histórico das decisões tomadas.

## Dificuldades a evitar

- Aprovar uma solicitação que gera conflito.
- Material sem registro de quem o retirou e quando foi devolvido.
- Decisões sem rastro auditável.

## Cenário de uso

Chega uma solicitação de equipamento restrito. O responsável confere a agenda, aprova (`APROVADA`), registra a retirada no início do uso (`EM_USO`) e a devolução ao final (`CONCLUÍDA`).

## Sinais de sucesso

- Toda aprovação ou rejeição fica registrada com autor e data.
- Nenhuma retirada ou devolução fica sem registro.
- Só pessoas com este perfil aprovam recursos restritos.

## Origem

Perfil **Responsável** do enunciado do projeto.
