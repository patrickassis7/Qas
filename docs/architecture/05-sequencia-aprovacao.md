# Sequência: aprovação de reserva restrita

O responsável pelo recurso ou um administrador decide uma reserva no estado `SOLICITADA` pelo endpoint `POST /api/v1/reservations/{id}/approve`. `ApprovalService` bloqueia a reserva, valida a autorização contextual, registra a decisão e a auditoria, persiste a transição e tenta notificar o solicitante via HTTP.

```mermaid
sequenceDiagram
    autonumber
    actor Responsible as RESPONSAVEL do recurso
    participant Web as Cliente web / API
    participant Security as Spring Security
    participant Jwt as JwtDecoder e ActiveUserJwtConverter
    participant Controller as ApprovalController
    participant Service as ApprovalService
    participant Reservations as ReservationRepository
    participant Approvals as ApprovalRepository
    participant Audit as AuditService
    participant DB as PostgreSQL
    participant Notify as NotificationPort / HttpNotificationAdapter
    participant Remote as Serviço HTTP de notificação
    participant Errors as GlobalExceptionHandler

    Responsible->>Web: POST /api/v1/reservations/{id}/approve + JWT Bearer
    Web->>Security: requisição autenticada
    Security->>Jwt: validar assinatura, issuer e expiração
    Jwt-->>Security: usuário ativo e authorities atuais
    alt JWT inválido ou usuário inativo
        Security-->>Web: 401 padronizado
    else autenticação válida
        Security->>Controller: encaminhar principal
        Controller->>Service: approve(id, ator, isAdmin, nota)
        Service->>Reservations: findByIdForUpdate(id)
        Reservations-->>Service: reserva e recursos vinculados
        Note over Service,Reservations: lock pessimista da reserva durante a decisão
        alt ator é solicitante da própria reserva
            Service--x Controller: AccessDeniedException
            Controller->>Errors: mapear erro de autorização
            Errors-->>Web: 403 padronizado, sem transição de estado
        else ator não é ADMINISTRADOR nem responsável de todos os recursos restritos
            Service--x Controller: AccessDeniedException
            Controller->>Errors: mapear erro de autorização
            Errors-->>Web: 403 padronizado, sem transição de estado
        else ator autorizado
            Service->>Service: validar transição SOLICITADA para APROVADA
            alt estado não permite aprovação
                Service--x Controller: InvalidReservationTransitionException
                Controller->>Errors: mapear conflito de estado
                Errors-->>Web: 409 padronizado
            else transição válida
                Service->>Approvals: salvar ApprovalEntity(APROVADA, decisor, nota)
                Service->>Audit: registrar RESERVA_APROVADA (from, to, nota)
                Service->>Reservations: saveAndFlush(reserva APROVADA)
                Reservations-->>Service: estado persistido na transação
                Service->>Notify: send(RESERVA_APROVADA)
                Notify->>Remote: POST /notifications
                alt endpoint aceita a notificação
                    Remote-->>Notify: resposta HTTP 2xx
                else erro de transporte ou resposta não 2xx
                    Remote--x Notify: RestClientException
                    Notify->>Notify: captura exceção e registra aviso
                    Note over Notify,Service: erro externo não é propagado nem reverte a aprovação
                end
                Service->>DB: commit da transação de negócio
                Service-->>Controller: ReservationResponse APROVADA
                Controller-->>Web: 200 OK
            end
        end
    end
```

## Regras representadas

- `/api/v1/**` exige autenticação. O conversor JWT consulta o usuário ativo e monta as authorities com os papéis atuais.
- A regra contextual é aplicada em `ApprovalService`: o solicitante não pode decidir a própria reserva; um administrador pode decidir; um responsável só pode decidir quando é responsável por todos os recursos restritos da reserva.
- A reserva é carregada com lock pessimista. A máquina de estados permite a transição `SOLICITADA → APROVADA`; uma transição inválida é tratada como HTTP 409.
- `ApprovalEntity`, `AuditEventEntity` e a mudança de estado são persistidas na transação de negócio. A migration mantém uma única decisão por reserva.
- A notificação é uma tentativa HTTP síncrona pelo `HttpNotificationAdapter`. Falha de conexão ou resposta HTTP não 2xx é capturada e registrada como aviso; não há fila nem rollback da decisão por falha externa.
- No código atual, a tentativa HTTP acontece depois de `saveAndFlush`, mas antes do commit final da transação de negócio.
