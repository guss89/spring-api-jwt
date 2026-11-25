# Instrucciones generales del proyecto

## Contexto
Este proyecto es una API REST desarrollada con Java y SpringBoot.

## Regla de trabajo
- Antes de proponer cambios, revisa el código relacionado.
- Respeta la estrcutura y convenciones existentes.
- Explica los conceptos técnicos con lenguaje sencillo.
- No inventes funcionalidades que no hayas comprobado.
- No modifiques archivos sin que la tarea lo requiera.
- Cuando hagas cambios, explica qué archivos modificaste y por qué.
- Si falta información, pregunta antes de asumir.

## Documentación
Consulta docs/CONTEXT.md cuando necesites
conocer la arquitectura, las decisiones y los
avances registrados del proyecto.

## Convenciones de código
- Paquetes por capa en `com.calisthenics.calisthenics`: `controller`, `service`, `repository`, `model` (enums en `model/catalogs`), `dto`, `exception`, `config`, `security`.
- Rutas REST con el formato `/api/v1/<recurso-en-kebab-case>`; la autenticación vive en `/auth`.
- Inyección de dependencias por constructor, con campos `private final`.
- DTOs `XxxRequest` (entrada) y `XxxResponse` (salida) en `dto/`. Los endpoints nuevos deben devolver DTOs, no entidades.
- Recurso no encontrado: lanzar `ResourceNotFoundException` (responde 404). Validaciones de negocio: `IllegalArgumentException` (responde 400).
- Entidades con Lombok, `@Table(name = "snake_case")`, relaciones `FetchType.LAZY` y enums con `@Enumerated(EnumType.STRING)`.
- Las pruebas van en `src/test/java`, en el mismo paquete que la clase probada.

## Agentes
Los agentes de Claude Code están en `.claude/agents/`:
- `explorer`: analiza el proyecto sin modificar archivos.
- `developer`: implementa funcionalidades, corrige bugs y escribe las pruebas siguiendo estas convenciones.
- `verifier`: revisa los cambios (requisitos, bugs, seguridad, checklist del proyecto) y ejecuta build y pruebas. No modifica archivos; solo reporta.

Flujo: explorer (informe) → aprobación del usuario → developer (implementa + pruebas) → verifier (✅/⚠️/❌; si es ❌ vuelve al developer con los hallazgos) → actualizar docs/CONTEXT.md. Ver también CLAUDE.md.