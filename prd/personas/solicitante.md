# Persona: Solicitante

**Perfil:** professor ou coordenador que precisa de sala, professor ou material em um horário específico.

## Objetivos

- Consultar a disponibilidade de salas, professores e materiais.
- Criar, alterar e cancelar as próprias reservas.
- Saber o estado atual de cada solicitação.

## Necessidades

- Pesquisar recursos por tipo, capacidade, localização, competência e disponibilidade.
- Receber mensagens de erro compreensíveis quando o horário conflita, o recurso está em manutenção ou a reserva não pode ser alterada.
- Ser notificado sobre aprovação, rejeição ou cancelamento.
- Usar o sistema também em tela pequena.

## Dificuldades a evitar

- Descobrir o conflito só depois de planejar a aula.
- Reserva aceita para duas pessoas ao mesmo tempo (dupla reserva).
- Não entender por que uma solicitação foi rejeitada.

## Cenário de uso

A professora pesquisa um laboratório com 30 lugares para quinta, 14h às 16h, e pede também um projetor. O sistema recusa um dos horários por sobreposição, ela escolhe outro e a reserva fica `SOLICITADA` até a aprovação, por se tratar de recurso restrito.

## Sinais de sucesso

- Cria a reserva sem conflito na primeira ou segunda tentativa.
- Entende a causa de qualquer recusa pela mensagem exibida.
- Nunca vê uma reserva sua perdida por concorrência.

## Origem

Perfil **Solicitante** do enunciado do projeto.
