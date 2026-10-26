# PRD do produto

## Fluxo de documentação

1. **Personas:** [Solicitante](personas/solicitante.md), [Responsável](personas/responsavel.md) e [Administrador](personas/administrador.md), derivadas do enunciado.
2. **Requisitos e regras:** [requisitos-funcionais.md](requisitos-funcionais.md) reúne RFs, regras de negócio (RN) e requisitos não funcionais (RNF).
3. **Rastreabilidade:** [RTM.md](../RTM.md) liga requisito, risco, teste e evidência.
4. **Jornadas e arquitetura:** a construir (semana 35: C4, utility tree, riscos e ADRs), sem presumir regras além das do enunciado.
5. **Implementação:** acompanha as semanas do cronograma do [README](../README.md).

> Os artefatos antigos do domínio de futebol (Foot Fanatics) foram removidos deste repositório.

## Escopo conhecido

- A aplicação deve organizar a alocação de salas, professores e materiais sem conflitos de horário.
- A avaliação do projeto deve apresentar evidências objetivas de qualidade, segurança, rastreabilidade e desempenho.
- Perfis definidos pelo enunciado do projeto: **Solicitante** (professor ou coordenador que consulta disponibilidade e cria, altera ou cancela reservas), **Responsável** (aprova solicitações especiais, valida a alocação de docentes e acompanha retiradas e devoluções) e **Administrador** (gerencia salas, professores, materiais, usuários, bloqueios e manutenção).
- Regras críticas definidas: término posterior ao início; sem sobreposição de reservas do mesmo recurso (sala, material ou professor); uma única reserva aceita em solicitações simultâneas; recursos em manutenção não são reserváveis; somente responsáveis aprovam recursos restritos; reservas iniciadas não podem ser apagadas; toda mudança de estado gera auditoria.
- Ciclo de vida da reserva: `SOLICITADA → APROVADA → EM_USO → CONCLUÍDA`, com estados alternativos rejeitada, cancelada e não compareceu.
- Tecnologia definida pelo enunciado: Java 21 e Spring Boot 3.x.
- Metas mensuráveis: 80% de cobertura de linhas, 70% de branches, 100% dos requisitos críticos na RTM e 0 bugs ou vulnerabilidades críticas.

A lista completa de requisitos funcionais, a estratégia de qualidade e os entregáveis estão no [README da raiz](../README.md).

## Decisões pendentes

- Como detalhar as jornadas de frontend e de retaguarda a partir dos perfis acima, e validá-las explicitamente.
- Quais recursos serão classificados como "restritos" (exigem aprovação do Responsável).
- Granularidade e unidade de tempo das reservas (por exemplo, janelas mínimas e máximas).
- Quais integrações externas ou notificações simuladas serão adotadas.
- Quais limites de desempenho (tempo de resposta, vazão) serão metas do teste de carga com JMeter.
- Quais políticas de segurança específicas (além de autorização por perfil e validação de entrada) serão aplicadas.

## Premissas de leitura

O enunciado do projeto é a fonte de verdade para escopo, perfis, regras críticas e metas. Os requisitos, as regras e a RTM atuais foram derivados diretamente dele; riscos e testes da RTM são propostas da equipe, sujeitas a revisão. As jornadas e a arquitetura ainda serão construídas e validadas. Nenhum artefato do domínio de futebol deve ser usado como referência.
