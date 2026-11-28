# 05 · Módulo: catálogos, rutinas y asignación

Dos áreas: **catálogos** (datos base que casi no cambian) y **rutinas** (lo que se entrena y se asigna a clientes).

## Contratos de API

### Catálogos
| Recurso | Ruta | Entrada | Salida actual |
|---|---|---|---|
| Regiones del cuerpo | `/api/v1/body-regions` | BodyRegion: name | Entidad `BodyRegion` |
| Categorías de ejercicio | `/api/v1/exercise-category` | ExerciseCategory: name | Entidad |
| Ejercicios | `/api/v1/exercises` | `ExerciseRequest`: name, description, weighted (boolean; “con peso”), exerciseCategoryId | `ExerciseResponse` en POST; entidad en GET/PUT |
| Tipos de rutina | `/api/v1/type-routine` | TypeRoutine: name | Entidad |

Todos tienen POST, GET, PUT `/{id}` y DELETE `/{id}`.

### Rutinas
| Recurso | Ruta | Entrada | Salida actual |
|---|---|---|---|
| Rutinas | `/api/v1/routines` | `RoutineRequest`: name, typeRoutineId | `RoutineResponse` en POST; entidad en GET/PUT |
| Ejercicios de rutina | `/api/v1/routine-exercise` | `RoutineExerciseRequest`: routineId, exerciseId, bodyRegionId, sets, reps, restSeconds (enteros) | `RoutineExerciseResponse` en POST; entidad en GET/PUT |
| Asignación a cliente | `/api/v1/client-routine` | `ClientRoutineRequest`: userId, routineId, startDate, endDate, observations, active | `ClientRoutineResponse` en POST; GET devuelve entidades. **Sin PUT ni DELETE.** |

> Pendiente de API: no se puede editar ni retirar una asignación de rutina. El diseño debe mostrar la acción “Desactivar” como **pendiente**; por ahora, solo “Asignar nueva”.

## Pantallas

### 5.1 Catálogos — ADMIN (edición) · COACH (consulta)
- Un solo módulo con navegación por pestañas: **Regiones · Categorías · Ejercicios · Tipos de rutina**.
- Cada pestaña es una tabla simple con nombre y acciones (editar, eliminar). Alta en modal de una sola columna.
- Ejercicios: tabla con nombre, categoría (`Badge`), si es con peso (`weighted`, icono + texto “Con peso”) y descripción truncada. Filtro por categoría.
- Eliminar categoría o tipo con rutinas/ejercicios asociados: la API puede devolver error; mostrar el motivo tal cual lo da la API y no ocultarlo.

### 5.2 Rutinas (`/rutinas`) — ADMIN y COACH
- Tabla: nombre, tipo de rutina, número de ejercicios (pendiente de API salvo que se calcule con `RoutineExercise`), acciones.
- Alta: modal con nombre y tipo.

### 5.3 Detalle de rutina (`/rutinas/:id`)
- Cabecera: nombre y tipo.
- Lista ordenada de ejercicios de la rutina. Cada fila: orden, ejercicio, región del cuerpo (`Badge`), series, repeticiones, descanso (formato “90 s”).
- Añadir ejercicio: modal con selectores de ejercicio y región, y numéricos para series, repeticiones y descanso.
- Reordenar: **pendiente de API** (no hay campo de orden). No lo diseñes como función activa.
- Editar y eliminar fila: la API no lo expone de forma clara en el listado; verificar antes de diseñarlo como definitivo.

### 5.4 Asignación de rutina a cliente — ADMIN y COACH
Desde `/clientes/:id` → pestaña Rutinas:
- Lista de asignaciones con rutina, fechas, estado (`Badge`: activa verde, inactiva gris) y observaciones.
- Botón “Asignar rutina”: modal con rutina, fecha de inicio, fecha de fin (opcional) y observaciones.
- Validación: fin posterior a inicio (en cliente; la API no lo garantiza).
- Vista de la rutina activa destacada arriba.

### 5.5 Mi rutina (`/mi-rutina`) — CLIENT
- Tarjeta con nombre de la rutina y fechas de vigencia.
- Lista de ejercicios en orden de lectura, en formato de “ficha”: ejercicio, series × repeticiones, descanso.
- Tono motivador sin texto de relleno. Ejemplo: “Hoy: 3 series de 10 · descanso 90 s”.
- Si no hay asignación activa: “Tu coach aún no te ha asignado una rutina.”

## Componentes
- `Select` searchable para ejercicios y rutinas (listas largas).
- `Badge` de región del cuerpo con color por región (escala consistente; define la paleta en `01`).
- `Timeline` o lista numerada para el orden de ejercicios en la vista de cliente.

## Criterios de aceptación
- Un COACH nunca ve botones de editar catálogos (solo consulta).
- La vista de cliente funciona con una mano en móvil (botones de al menos 44 px).
- Los campos numéricos (series, repeticiones, descanso) no aceptan negativos.
- Cualquier función sin endpoint aparece marcada como “pendiente de API” en la documentación del diseño, no como botón activo.
