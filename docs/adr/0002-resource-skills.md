ADR 0002: Representação de competências dos recursos do tipo PROFESSOR
Status: Aceita
Data: 2026-11-08
Escopo: Commit 05 - Modelo de Recursos
Contexto
O modelo de recursos prevê o tipo PROFESSOR.

A especificação exige que professores possuam competências para permitir filtros e consultas futuras por conhecimento, especialidade ou área de atuação.

Como este é um projeto acadêmico de 4º período, a solução deve privilegiar simplicidade de implementação e facilidade de manutenção.

Decisão
As competências serão armazenadas no campo:

resources.skills