# 04 · Módulo: clientes, membresías y pagos

Es el núcleo administrativo del negocio. Parte ya existe (clientes en `ClientsPage.tsx`).

## Contratos de API

### Perfil de cliente — `/api/v1/clients`
| Acción | Método y ruta | Entrada | Salida |
|---|---|---|---|
| Listar | `GET` | — | `ClientProfileResponse[]` |
| Crear | `POST` | `ClientProfileRequest`: userId (CLIENT, sin perfil previo), birthDate (pasada), initialWeight (kg, ≤3 enteros y 2 decimales), initialHeight (cm) | `ClientProfileResponse` |
| Editar | `PUT /{id}` | Igual; el `userId` no puede cambiar | `ClientProfileResponse` |
| Eliminar | `DELETE /{id}` | — | — |

Errores: 404 perfil no encontrado; 409 ya tiene perfil; 400 validación o usuario que no es CLIENT.

### Planes de membresía — `/api/v1/membership-plans`
| Acción | Ruta | Entrada | Salida |
|---|---|---|---|
| Listar / detalle | `GET`, `GET /{id}` | — | `MembershipPlanResponse` |
| Crear / editar | `POST`, `PUT /{id}` | `MembershipPlanRequest`: name (≤100), price (>0, hasta 6 enteros y 2 decimales), durationDays (>0) | `MembershipPlanResponse` |
| Eliminar | `DELETE /{id}` | — | 409 si tiene membresías |

### Membresías de cliente — `/api/v1/client-memberships`
| Acción | Ruta | Entrada | Salida |
|---|---|---|---|
| Listar | `GET` (`?userId=` opcional) | — | `ClientMembershipResponse[]` |
| Detalle | `GET /{id}` | — | `ClientMembershipResponse` |
| Crear / editar | `POST`, `PUT /{id}` | `ClientMembershipRequest`: userId (CLIENT), membershipPlanId, startDate, status (opcional, por defecto ACTIVE) | `ClientMembershipResponse` |
| Eliminar | `DELETE /{id}` | — | 409 si tiene pagos |

Reglas que el diseño debe reflejar:
- `endDate` lo calcula el sistema: `startDate + durationDays − 1` (inclusivo). Muéstralo como resultado calculado, no como campo editable.
- Una sola membresía ACTIVE por cliente (400 si ya hay una).
- Estados: ACTIVE, EXPIRED, CANCELLED.
- Cambiar la duración de un plan no cambia las membresías ya vendidas.

### Pagos — `/api/v1/client-payments`
| Acción | Ruta | Entrada | Salida |
|---|---|---|---|
| Listar | `GET` (`?userId=` opcional) | — | `ClientPaymentResponse[]` |
| Detalle | `GET /{id}` | — | `ClientPaymentResponse` |
| Crear / editar | `POST`, `PUT /{id}` | `ClientPaymentRequest`: userId, clientMembershipId (debe ser del mismo userId), amount (>0), paymentDate (obligatoria), paymentMethod, status (PAID o PENDING; PAID si no viene), reference, notes | `ClientPaymentResponse` |
| Eliminar | `DELETE /{id}` | — | Borrado físico |

Los importes van en `BigDecimal`: en el front trabaja con cadenas o con `number` redondeado a 2 decimales y muestra con `Intl.NumberFormat`.

## Pantallas

### 4.1 Clientes (`/clientes`) — ADMIN y COACH
- Tabla: nombre, correo, peso inicial, altura inicial, membresía activa (`Badge`), acciones.
- Acciones de fila: ver detalle, editar, eliminar (con confirmación).
- Botón “Nuevo cliente”: modal existente `ClientFormModal`. Revisar: el selector de usuario debe mostrar solo usuarios CLIENT sin perfil (la API responde 409 si no).
- Búsqueda por nombre (en cliente).

### 4.2 Detalle de cliente (`/clientes/:id`) — ADMIN, y COACH en modo lectura
Cabecera con nombre, correo, edad (calculada desde birthDate), estado de membresía y pagos pendientes. Pestañas:
1. **Datos:** perfil (peso y altura iniciales, fecha de nacimiento) con botón editar.
2. **Membresía:** membresía activa con fechas, días restantes y botón “Nueva membresía”. Histórico en tabla.
3. **Pagos:** tabla con fecha, importe, método, estado (`Badge`), referencia. Botón “Registrar pago”.
4. **Rutinas:** rutinas asignadas (ver módulo 05).
5. **Evaluaciones:** ver módulo 06.

Si el cliente no tiene perfil, la cabecera muestra “Sin perfil de cliente” con acción “Crear perfil”.

### 4.3 Planes (`/planes`) — ADMIN
- Tarjetas o tabla: nombre, precio, duración en días, número de membresías asociadas (calculado en cliente si hay datos; si no, pendiente de API).
- Alta/edición en modal. Duración en días con texto auxiliar de equivalencia (“30 días ≈ 1 mes”).
- Borrado bloqueado si tiene membresías: deshabilitar el botón con tooltip explicativo y no esperar a que la API responda 409.

### 4.4 Membresías (`/membresias`) — ADMIN
- Tabla: cliente, plan, inicio, fin (calculado), estado (`Badge`: ACTIVE verde, EXPIRED gris, CANCELLED rojo), acciones.
- Filtro por cliente (`Select` searchable) y por estado.
- Aviso visual para membresías ACTIVE cuya `endDate` ya pasó: “Caducada, sin actualizar”. La API no caduca sola; el diseño debe ofrecer “Marcar como caducada”.
- Alta: `Stepper` de dos pasos (cliente y plan → fechas y confirmación con fin calculado).

### 4.5 Pagos (`/pagos`) — ADMIN
- Tabla: fecha, cliente, importe (alineado a la derecha), método, estado, referencia.
- Resumen superior: total cobrado y total pendiente del periodo visible (calculado en cliente).
- Alta: modal con selección de membresía (solo las del cliente elegido) y fecha de pago por defecto hoy.
- Borrado: confirmación explícita “Esta acción borra el pago de forma definitiva.”

### 4.6 Mi membresía (`/mi-membresia`) — CLIENT
- Tarjeta con plan, fechas, días restantes y estado.
- Lista de pagos propios (solo lectura).
- Si no hay membresía: estado vacío con texto “No tienes una membresía activa. Consulta con tu coach.” (sin botón de compra: no hay pasarela).

## Componentes específicos
- `Badge` de estado de pago: PAID (`green`), PENDING (`yellow`).
- `Badge` de estado de membresía: ACTIVE (`green`), EXPIRED (`gray`), CANCELLED (`red`).
- `Stat` o tarjeta de cifra para totales.
- `NumberInput` con `prefix`/`suffix` para importes y medidas; `decimalScale={2}`.

## Criterios de aceptación
- Ningún importe se formatea a mano: siempre `Intl`.
- Todas las fechas de inicio se eligen con `DateInput`; la fecha de fin aparece como texto calculado.
- Las acciones destructivas piden confirmación y dicen qué se borra.
- Un cliente sin membresía activa no puede registrar pagos sin elegir una membresía (campo obligatorio).
- Textos en i18n.
