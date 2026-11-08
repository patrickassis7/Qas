# ADR 0002: Controle de concorrência na reserva de recursos

- **Status:** Aceita
- **Data:** 2026-11-08
- **Escopo:** Commits 09-11 - criação e alteração de reservas sem dupla reserva.

## Contexto

Duas requisições simultâneas para o mesmo recurso e horário podem passar juntas pela checagem de sobreposição (D1, D5) e gravar duas reservas. O banco é PostgreSQL em `READ COMMITTED`.

## Opções

| Opção | Como funciona | Prós | Contras |
|---|---|---|---|
| A. `EXCLUDE USING gist` com `btree_gist` | Restrição no banco sobre `(resource_id, tstzrange(start_at, end_at))` | Garantia no schema, vale para qualquer escrita | Exige desnormalizar `status` e `resource_id` em `reservation_resources`, extensão `btree_gist` e tradução de `SQLState 23P01` |
| B. Lock pessimista por recurso | `SELECT ... FOR UPDATE` nos recursos da reserva, em ordem de `id`, antes da checagem | Usa o modelo atual sem mudar o schema; todos os recursos são gravados na mesma transação | A garantia depende de todo caminho de escrita passar pelo serviço |

## Decisão

Opção B, a mais simples para o grupo.

- `ResourceRepository.findAllByIdForUpdate` trava as linhas de `resources` ordenadas por `id`. A ordem fixa impede deadlock entre reservas com conjuntos de recursos cruzados.
- A checagem de sobreposição (`AgendaConflictChecker`) roda depois do lock. Em `READ COMMITTED` cada comando vê as reservas já confirmadas pela transação que liberou o lock.
- Reserva, vínculos em `reservation_resources` e auditoria são gravados na mesma transação: tudo ou nada.
- O conflito vira 409 (`RESERVATION_CONFLICT`) e o evento `CONFLITO_EVITADO` é gravado em transação própria.

## Revisão

- **Corretude:** com o lock, a violação nunca chega ao banco, então não há exceção de restrição a traduzir. Revisitar a opção A se surgir outro caminho de escrita em `reservations`.
- **Risco:** o lock é por recurso, não por intervalo. Reservas em horários diferentes do mesmo recurso se serializam por alguns milissegundos, aceitável para o volume do projeto.
- **Conexões:** o evento `CONFLITO_EVITADO` usa uma segunda conexão enquanto a primeira segura o lock. Com o pool padrão (10), picos acima disso exigem aumentar o pool.
- **Evidência:** `ReservationConcurrencyIntegrationTest` dispara 8 threads sincronizadas, repetido 20 vezes, com um e com vários recursos.
