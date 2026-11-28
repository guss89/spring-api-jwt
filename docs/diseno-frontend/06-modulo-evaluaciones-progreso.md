# 06 · Módulo: evaluaciones y progreso

Registra cuánto mejora el cliente en cada ejercicio: repeticiones máximas o peso máximo. Es la pieza que da sentido a todo lo demás para el cliente.

## Contratos de API — `/api/v1/evaluations`
| Acción | Ruta | Entrada | Salida actual |
|---|---|---|---|
| Crear | `POST` | `EvaluationRequest`: userId, exerciseId, maxReps, maxWeight, registerDate, notes | `EvaluationResponse` |
| Listar | `GET` | — | Entidad `Evaluation` |
| Editar | `PUT /{id}` | Igual | Entidad `Evaluation` |
| Eliminar | `DELETE /{id}` | — | — |

Notas para el diseño:
- Ten en cuenta que GET y PUT devuelven entidades: el front debe leer `user` y `exercise` anidados y pueden faltar campos. Verifica en Swagger o con una petición real antes de fijar el modelo de datos.
- `EvaluationService.update` no siempre persiste los cambios (revisión pendiente en la API). No confíes en la edición hasta comprobarlo: diseña “editar” con estado de confirmación explícito.
- No hay filtro por cliente en el servidor: el front filtra la lista completa.

## Pantallas

### 6.1 Evaluaciones del cliente — COACH y ADMIN (`/clientes/:id`, pestaña Evaluaciones)
- Selector de ejercicio (`Select` searchable) arriba para ver la evolución de uno.
- **Gráfica**: línea con fecha en el eje X y repeticiones o peso máximo en el Y. Usa Mantine Charts o una librería ya aprobada; pregunta antes de añadir dependencias.
- **Tabla** debajo: fecha, repeticiones máximas, peso máximo, notas. Orden por fecha descendente.
- Botón “Nueva evaluación”: modal con ejercicio, fecha (por defecto hoy), repeticiones o peso (al menos uno obligatorio), notas.

Reglas de presentación:
- Si el ejercicio es con peso (`weighted` en la API), la gráfica muestra peso; si no, repeticiones. Así se evita un campo vacío engañoso.
- Un valor nuevo que supera al anterior se resalta con un icono y el texto “Nuevo récord”. Es un cálculo en cliente: no lo presentes como dato del servidor.

### 6.2 Mi progreso (`/mi-progreso`) — CLIENT
- Resumen superior: tres tarjetas con (1) último récord, (2) ejercicio más evolucionado, (3) número de evaluaciones del último mes.
- Gráfica por ejercicio, igual que en 6.1 pero en modo lectura.
- Historial en lista de tarjetas (en móvil) o tabla (en escritorio).
- Un cliente no edita ni borra evaluaciones. Registrar su propia marca queda como **pendiente de API y de decisión de negocio** (¿lo registra el coach?).

### 6.3 Inicio de coach — resumen
- Clientes con evaluación en los últimos 14 días y clientes sin evaluación reciente (lista para revisar). Calculado en cliente.

## Estados específicos
- Sin evaluaciones: “Aún no hay evaluaciones. Registra la primera marca para empezar a ver el progreso.” con botón.
- Un solo punto en la gráfica: mostrar el valor con texto, no una línea.
- Error al cargar: mantener la tabla vacía y mostrar el `Alert` con reintento.

## Accesibilidad de la gráfica
- Cada gráfica tiene una tabla equivalente accesible (puede estar oculta visualmente y disponible para lectores de pantalla).
- No uses solo color para distinguir series: añade marcadores o leyenda con texto.

## Criterios de aceptación
- La gráfica se entiende sin leer la tabla y la tabla sin ver la gráfica.
- El cliente nunca ve datos de otros clientes.
- Las fechas se muestran en formato local y se guardan en ISO.
- Pendiente de API y marcado en el diseño: edición fiable y filtro por cliente en el servidor.
