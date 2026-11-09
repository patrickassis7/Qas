# Sequência: criação de reserva

O fluxo começa no `POST /api/v1/reservations`. O filtro de segurança valida o JWT e o usuário ativo; o controller valida o DTO; `ReservationService` verifica período, recursos, agenda e manutenção dentro de uma transação. Recursos comuns são aprovados automaticamente; quando há recurso restrito, a reserva fica solicitada para decisão posterior.

```mermaid
sequenceDiagram
    autonumber
    actor Solicitante
    participant Web as Cliente web / API
    participant Security as Spring Security
    participant Jwt as JwtDecoder e ActiveUserJwtConverter
    participant Controller as ReservationController
    participant Service as ReservationService
    participant Users as UserRepository
    participant Resources as ResourceRepository
    participant Agenda as AgendaConflictChecker
    participant Reservations as ReservationRepository
    participant Audit as AuditService
    participant Notify as NotificationPort / HTTP adapter

    Solicitante->>Web: POST /api/v1/reservations + JWT Bearer
    Web->>Security: requisição autenticada
    Security->>Jwt: validar assinatura, issuer e expiração
    Jwt->>Users: buscar usuário ativo e papéis atuais
    Users-->>Jwt: usuário ativo / inválido
    alt JWT inválido ou usuário inativo
        Security-->>Web: 401 padronizado
    else identidade válida
        Security->>Controller: encaminhar principal autenticado
        Controller->>Controller: @Valid valida CreateReservationRequest
        alt corpo inválido
            Controller-->>Web: 400 padronizado
        else corpo válido
            Controller->>Service: create(ator, pedido)
            Service->>Service: exigir endAt posterior a startAt
            Service->>Users: localizar solicitante
            Service->>Resources: bloquear recursos ativos por ID crescente
            Note over Service,Resources: PESSIMISTIC_WRITE mantém os locks até o fim da transação
            Service->>Agenda: verificar reservas e bloqueios de manutenção sobrepostos
            alt período indisponível
                Agenda-->>Service: conflito
                Service->>Audit: registrarIndependente(CONFLITO_EVITADO)
                Service-->>Controller: ReservationConflictException
                Controller-->>Web: 409 RESERVATION_CONFLICT
            else agenda livre
                Service->>Reservations: salvar reserva em SOLICITADA
                Service->>Audit: registrar(RESERVA_CRIADA)
                alt todos os recursos são comuns
                    Service->>Service: transicionar SOLICITADA para APROVADA
                    Service->>Audit: registrar(RESERVA_APROVADA_AUTOMATICAMENTE)
                    Service->>Notify: enviar notificação de aprovação
                    opt endpoint HTTP indisponível
                        Notify-->>Service: falha capturada, operação continua
                    end
                else há recurso restrito
                    Note over Service: permanece SOLICITADA para decisão posterior
                end
                Service->>Reservations: saveAndFlush
                Reservations-->>Service: reserva persistida
                Service-->>Controller: ReservationResponse
                Controller-->>Web: 201 Created
            end
        end
    end
```

## Observações

- O filtro verifica JWT, issuer e expiração; `ActiveUserJwtConverter` busca o usuário no PostgreSQL em cada autenticação e deriva as authorities dos papéis atuais.
- A busca de recursos usa lock pessimista em ordem crescente de UUID. A disponibilidade é verificada por `AgendaConflictChecker`, que consulta reservas em estados que ocupam agenda e bloqueios de manutenção ativos.
- A auditoria de uma criação válida participa da transação de negócio. O evento `CONFLITO_EVITADO` é registrado com `REQUIRES_NEW`, por isso sobrevive ao rollback da tentativa conflitante.
- `HttpNotificationAdapter` tenta o POST configurado e captura exceções do cliente HTTP. A chamada é best effort; não há fila ou publicação assíncrona nesse fluxo.
