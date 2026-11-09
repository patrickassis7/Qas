# Sequência: duas reservas concorrentes

O fluxo mostra duas requisições autenticadas tentando reservar o mesmo recurso no mesmo intervalo. A proteção é feita na aplicação com uma transação e lock pessimista (`PESSIMISTIC_WRITE`) da linha do recurso, selecionada em ordem crescente de ID. A segunda transação aguarda; depois do commit da primeira, sua consulta de agenda encontra a reserva e a API responde HTTP 409.

```mermaid
sequenceDiagram
    autonumber
    actor A as Solicitante A
    actor B as Solicitante B
    participant Api as ReservationController
    participant Service as ReservationService
    participant ResourceRepo as ResourceRepository
    participant DB as PostgreSQL
    participant Agenda as AgendaConflictChecker
    participant ReservationRepo as ReservationRepository
    participant Audit as AuditService
    participant Errors as GlobalExceptionHandler

    par Requisições iniciadas em paralelo
        A->>Api: POST /api/v1/reservations, recurso R e intervalo T
        Api->>Service: create(ator A, pedido)
        Service->>DB: iniciar transação A
        Service->>ResourceRepo: findAllByIdForUpdate([R])
        ResourceRepo->>DB: SELECT do recurso R FOR UPDATE
        DB-->>ResourceRepo: concede lock à transação A
        ResourceRepo-->>Service: recurso bloqueado
    and
        B->>Api: POST /api/v1/reservations, mesmo recurso R e intervalo T
        Api->>Service: create(ator B, pedido)
        Service->>DB: iniciar transação B
        Service->>ResourceRepo: findAllByIdForUpdate([R])
        ResourceRepo->>DB: SELECT do recurso R FOR UPDATE
        Note right of DB: A linha R está bloqueada pela transação A e B aguarda
    end

    Service->>Agenda: verificar sobreposição de R no intervalo T
    Agenda->>DB: consultar reservas em estados que ocupam agenda
    DB-->>Agenda: nenhum conflito para A
    Agenda-->>Service: agenda livre para A
    Service->>ReservationRepo: salvar reserva A e vínculo com R
    Service->>DB: gravar auditoria da criação
    opt R é recurso comum
        Service->>Service: SOLICITADA para APROVADA automaticamente
        Service->>DB: gravar auditoria da aprovação automática
    end
    Service->>DB: flush e commit da transação A
    DB-->>ResourceRepo: liberar lock de R
    Service-->>Api: ReservationResponse A
    Api-->>A: 201 Created

    DB-->>ResourceRepo: concede lock de R à transação B
    ResourceRepo-->>Service: recurso bloqueado para B
    Service->>Agenda: verificar sobreposição de R no intervalo T
    Agenda->>DB: consultar reservas em estados que ocupam agenda
    DB-->>Agenda: reserva A ocupa R no intervalo T
    Agenda-->>Service: conflito
    Service->>Audit: registrarIndependente(CONFLITO_EVITADO)
    Audit->>DB: commit do evento em transação REQUIRES_NEW
    Service-->>Errors: ReservationConflictException
    Errors-->>B: 409 RESERVATION_CONFLICT
    Note over Service,DB: transação B sofre rollback e não deixa reserva nem vínculo órfão
```

## Garantias e limites

- `ResourceRepository.findAllByIdForUpdate` usa `PESSIMISTIC_WRITE` e ordena os recursos por ID para reduzir deadlocks quando uma reserva contém vários recursos.
- A consulta de sobreposição considera os estados `SOLICITADA`, `APROVADA` e `EM_USO`, que ocupam agenda. A migration não define uma constraint de exclusão temporal; a serialização acontece pelo lock das linhas dos recursos antes da consulta.
- `ReservationService` é transacional. A tentativa conflitante grava `CONFLITO_EVITADO` em transação `REQUIRES_NEW`, lança `ReservationConflictException` e a camada HTTP mapeia essa exceção para `409 RESERVATION_CONFLICT`.
- `ReservationConcurrencyIntegrationTest` verifica uma criação `201`, as demais respostas `409` e ausência de reservas órfãs, inclusive para pedidos multi-recurso em ordem cruzada.
