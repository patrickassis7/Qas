# ADR 0003: Representação de competências dos recursos PROFESSOR

- **Status:** Aceita
- **Data:** 2026-11-08
- **Escopo:** Commit 05 — modelo de recursos

## Contexto

O modelo de recursos inclui o tipo `PROFESSOR`. A especificação exige competências para permitir filtros por conhecimento, especialidade ou área de atuação. Para o escopo acadêmico, a representação deve permanecer simples.

## Decisão

Armazenar as competências no campo `resources.skills`, como texto associado ao recurso. A entidade `ResourceEntity` mapeia esse campo; `ResourceValidator` exige que ele seja informado para recursos do tipo `PROFESSOR`.

## Consequências

- A estrutura não cria tabela separada de competências nem catálogo normalizado.
- A busca de recursos pode usar o texto de `skills` sem introduzir um serviço externo.
- O modelo não declara validação semântica ou vocabulário controlado para cada competência.
