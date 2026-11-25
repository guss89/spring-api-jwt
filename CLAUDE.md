@AGENTS.md

## Flujo de trabajo con subagentes
Para cualquier tarea de código:
1. Delega al `explorer` para reunir contexto.
2. Delega al `developer` la implementación, pasándole el resumen del explorer.
3. Delega al `verifier` la revisión. Si el veredicto es ❌, regresa al developer con los hallazgos y repite.
