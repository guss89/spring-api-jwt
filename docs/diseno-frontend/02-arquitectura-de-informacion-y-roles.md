# 02 · Arquitectura de información y roles

## Roles
| Rol | Quién es | Qué necesita ver |
|---|---|---|
| **ADMIN** | Gestiona el negocio | Todo: usuarios, catálogos, clientes, planes, pagos. |
| **COACH** | Entrenador | Clientes, rutinas, evaluaciones. Sin usuarios ni pagos (propuesta, a validar). |
| **CLIENT** | Persona que entrena | Su rutina activa, su progreso, su membresía y sus pagos. |

Ver `01-sistema-de-diseno.md` para el patrón de cada pantalla. Los roles vienen en `LoginResponse.role`.

> Pendiente de decisión de negocio: qué puede ver el COACH de pagos y membresías. Diseña la versión conservadora y deja un comentario en el código.

## Mapa de pantallas

### Públicas
- `/login` · Iniciar sesión
- `/register` · Crear cuenta

### Autenticadas (todos los roles)
- `/` · Inicio (según rol, ver abajo)
- `/perfil` · Datos propios *(pendiente de API: `GET /api/v1/users` es solo ADMIN, así que no hay endpoint de “mi perfil”)*

### ADMIN
- `/usuarios` · Listado de usuarios (solo lectura en API actual)
- `/clientes` · Clientes (perfil de cliente) — ya existe
- `/clientes/:id` · Detalle de cliente: datos, membresía activa, pagos, rutinas, evaluaciones
- `/planes` · Planes de membresía (CRUD)
- `/membresias` · Membresías de clientes (CRUD, filtro por cliente)
- `/pagos` · Pagos (CRUD, filtro por cliente)
- `/catalogos/regiones` · Regiones del cuerpo
- `/catalogos/categorias` · Categorías de ejercicio
- `/catalogos/ejercicios` · Ejercicios
- `/catalogos/tipos-rutina` · Tipos de rutina
- `/rutinas` · Rutinas
- `/rutinas/:id` · Detalle de rutina y sus ejercicios

### COACH
- `/clientes` · Sus clientes (lista ya existe; el alcance real lo decide la API, que hoy devuelve todos)
- `/clientes/:id` · Detalle con rutinas y evaluaciones
- `/rutinas` y `/rutinas/:id` · Consulta y edición
- `/evaluaciones` · Evaluaciones de sus clientes

### CLIENT
- `/` · Hoy: rutina activa y acceso rápido a progreso
- `/mi-rutina` · Rutina activa y detalle de ejercicios
- `/mi-progreso` · Evaluaciones por ejercicio (gráfica y tabla)
- `/mi-membresia` · Membresía actual, fechas y pagos

## Navegación
- Barra lateral con enlaces visibles según rol (ya existe en `AppLayout.tsx` con `navItems`; amplíala con grupos: “Operación”, “Catálogos”, “Mi cuenta”).
- Cabecera: marca, idioma, cerrar sesión.
- Móvil: barra lateral en cajón con `Burger`.
- El enlace activo debe reflejar la ruta actual (ya usa `NavLink` de react-router).

## Inicio por rol
- **ADMIN:** resumen con clientes activos, membresías por caducar (próximos 7 días) y pagos pendientes. Datos calculados en el front a partir de los listados; marca como pendiente cualquier métrica que requiera endpoint nuevo.
- **COACH:** clientes con rutina activa y evaluaciones recientes.
- **CLIENT:** rutina de hoy y última evaluación.

## Flujos principales

### 1. Alta de cliente (ADMIN)
1. `/usuarios` o `/clientes` → “Nuevo cliente”.
2. Elegir usuario con rol CLIENT (la API rechaza otros roles con 400).
3. Completar fecha de nacimiento, peso y altura iniciales.
4. Si el usuario ya tiene perfil, la API responde 409: mostrar “Este usuario ya tiene perfil de cliente” y enlazar al existente.

### 2. Contratar membresía y pagar (ADMIN)
1. `/clientes/:id` → “Nueva membresía”, elegir plan.
2. La API calcula la fecha de fin; mostrar la fecha calculada antes de guardar.
3. Si ya hay una membresía ACTIVE, la API responde 400: explicarlo y ofrecer cancelar o caducar la anterior.
4. Registrar pago asociado a la membresía (`clientMembershipId`).

### 3. Asignar rutina a cliente (COACH o ADMIN)
1. `/clientes/:id` → “Asignar rutina”.
2. Elegir rutina, fecha de inicio y fin, observaciones.
3. Marcar la asignación como activa.

### 4. Registrar evaluación (COACH)
1. `/clientes/:id` → pestaña “Evaluaciones” → “Nueva evaluación”.
2. Elegir ejercicio, registrar repeticiones o peso máximo y notas.
3. Mostrar la evolución del ejercicio en la misma pantalla.

### 5. Entrenar (CLIENT)
1. `/mi-rutina` → lista de ejercicios con series, repeticiones y descanso.
2. Registrar evaluación cuando el coach lo pida (pendiente de API para que el cliente registre sus propias marcas).

## Estados vacíos por rol
- Sin clientes: “Aún no hay clientes. Crea el primero.” (ADMIN) / “Aún no tienes clientes asignados.” (COACH)
- Sin rutina: “Tu coach aún no te ha asignado una rutina.” (CLIENT)
- Sin membresía: “No tienes una membresía activa.” (CLIENT)
