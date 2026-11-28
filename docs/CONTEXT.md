# Contexto del proyecto

## Objetivo
> Inferido del código; pendiente de confirmar por el autor.

API REST para gestionar un servicio de entrenamiento de calistenia:
- Usuarios con rol (ADMIN, COACH, CLIENT) y autenticación con JWT.
- Catálogo de ejercicios (categorías, regiones del cuerpo) y rutinas (tipos de rutina, ejercicios por rutina).
- Asignación de rutinas a clientes y relación coach–cliente.
- Evaluaciones de progreso del cliente (repeticiones o peso máximo por ejercicio).
- Membresías (planes, membresía del cliente) y pagos.

## Arquitectura
**Tecnologías:** Java 17, Spring Boot 3.3.2, Spring Web, Spring Data JPA, MySQL, Spring Security + JWT (jjwt 0.12.5), Bean Validation, Lombok, Maven (`mvnw`).

**Capas** (`src/main/java/com/calisthenics/calisthenics/`):

| Paquete | Responsabilidad |
|---|---|
| `controller/` | Recibe las peticiones HTTP y delega en el servicio. |
| `service/` | Lógica de negocio; busca entidades relacionadas y lanza excepciones. |
| `repository/` | Interfaces Spring Data JPA (acceso a la BD). |
| `model/` | Entidades JPA; `model/catalogs/` contiene los enums. |
| `dto/` | Objetos de entrada/salida `XxxRequest` / `XxxResponse`. |
| `exception/` | `GlobalExceptionHandler`, `ResourceNotFoundException`, `ConflictException`, `ErrorResponse`. |
| `config/` | `SecurityConfig` (cadena de filtros, BCrypt, AuthenticationManager). |
| `security/` | `JwtAuthenticationFilter`, `CustomUserDetailsService`. |

**Recorrido de una petición:**
1. Llega la petición → `JwtAuthenticationFilter` lee el header `Authorization: Bearer <token>`, lo valida con `JwtService` y registra al usuario (su id) en el contexto de seguridad.
2. `SecurityConfig`: `/auth/**` es público; el resto exige estar autenticado.
3. El controlador recibe el `@RequestBody` (DTO o entidad) y llama al servicio.
4. El servicio busca las entidades relacionadas en los repositorios (`findById(...).orElseThrow(ResourceNotFoundException)`), arma la entidad y la guarda.
5. Se devuelve un DTO `XxxResponse` o la entidad directamente.
6. Los errores los traduce `GlobalExceptionHandler`: `ResourceNotFoundException` → 404, `IllegalArgumentException` → 400, `ForbiddenOperationException` y `AccessDeniedException` → 403, `ConflictException` → 409, `DataIntegrityViolationException` → 409 (mensaje neutro). `IllegalStateException` no capturada → 500.

**Configuración:** `application.properties` (excluido de git) con MySQL en `localhost:3306/calisthenics`, `ddl-auto=update` (Hibernate crea/actualiza las tablas solo). Plantilla versionada: `src/main/resources/application-example.properties`. JWT: `jwt.secret` (obligatoria, ≥32 bytes; se toma de la variable de entorno `JWT_SECRET`) y `jwt.expiration-ms` (opcional, 3600000 por defecto).

## Controladores y endpoints
| Controlador | Ruta base | Endpoints | Entrada / salida |
|---|---|---|---|
| `AuthController` | `/auth` | `POST /register`, `POST /login` | `RegisterRequest` → `UserResponse` (sin `passwordHash`); `LoginRequest` → `LoginResponse` {token, role, userId} |
| `BodyRegionController` | `/api/v1/body-regions` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `BodyRegionRequest` → `BodyRegionResponse` |
| `ClientController` | `/api/v1/clients` | POST, GET (ADMIN todos; resto solo el suyo), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` | `ClientProfileRequest` → `ClientProfileResponse`; escrituras solo ADMIN; 409 si el usuario ya tiene perfil |
| `UserController` | `/api/v1/users` | GET lista, GET `/{id}`, POST (201), PUT `/{id}`, DELETE `/{id}` (baja lógica, 204); solo ADMIN | `UserRequest` / `UserUpdateRequest` → `UserResponse` (sin `passwordHash`); 409 por username o email duplicado; ADMIN no puede desactivarse ni quitarse el rol ADMIN |
| `CoachClientController` | `/api/v1/coach-clients` | POST (201, ADMIN), DELETE `/{id}` (204, ADMIN), GET `?coachId=` o `?clientId=` (exactamente uno) | `CoachClientRequest` → `CoachClientResponse`; COACH ve solo sus clientes; CLIENT solo sus coaches; ADMIN todo |
| `ClientRoutineController` | `/api/v1/client-routine` | POST, GET (ADMIN todas; COACH y CLIENT solo las suyas en la lista), GET `/{id}` (COACH cualquiera; CLIENT solo las suyas), PUT `/{id}`, DELETE `/{id}` (escrituras solo COACH) | `ClientRoutineRequest` → `ClientRoutineResponse` |
| `EvaluationController` | `/api/v1/evaluations` | POST, GET (ADMIN todas; resto solo las suyas), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` | `EvaluationRequest` → `EvaluationResponse`; escrituras solo ADMIN |
| `ExerciseCategoryController` | `/api/v1/exercise-category` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `ExerciseCategoryRequest` → `ExerciseCategoryResponse` |
| `ExerciseController` | `/api/v1/exercises` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `ExerciseRequest` → `ExerciseResponse` (incluye `isWeighted` y `weighted`) |
| `RoutineController` | `/api/v1/routines` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `RoutineRequest` → `RoutineResponse` |
| `RoutineExerciseController` | `/api/v1/routine-exercise` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `RoutineExerciseRequest` → `RoutineExerciseResponse` |
| `TypeRoutineController` | `/api/v1/type-routine` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `TypeRoutineRequest` → `TypeRoutineResponse` |
| `ClientPaymentController` | `/api/v1/client-payments` | POST, GET (`?userId=` opcional), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` | `ClientPaymentRequest` → siempre `ClientPaymentResponse` |
| `MembershipPlanController` | `/api/v1/membership-plans` | POST, GET, GET `/{id}`, PUT `/{id}`, DELETE `/{id}` | `MembershipPlanRequest` → siempre `MembershipPlanResponse` |
| `ClientMembershipController` | `/api/v1/client-memberships` | POST, GET (`?userId=` opcional), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` | `ClientMembershipRequest` → siempre `ClientMembershipResponse` |

Notas:
- Todos los controladores reciben y devuelven DTOs (`XxxRequest` / `XxxResponse`); ninguno expone entidades JPA.
- Matriz de permisos vigente (aplicada en `SecurityConfig`, la primera regla que coincide gana):
  - Catálogos (`body-regions`, `exercise-category`, `type-routine`, `exercises`, `routines`, `routine-exercise`, `membership-plans`) y `evaluations`: escrituras solo ADMIN.
  - `clients`: PUT lo puede hacer CLIENT/COACH solo sobre su propio perfil (ajeno → 403; el dueño no se puede cambiar); POST y DELETE solo ADMIN.
  - `client-payments` y `client-memberships`: escrituras solo ADMIN.
  - `client-routine`: escrituras solo COACH (ADMIN recibe 403). Una asignación solo puede hacerse a un usuario con rol CLIENT (400 si no); el cliente de una asignación existente no se puede cambiar (400).
  - Lecturas: ADMIN ve todo; CLIENT y COACH solo lo suyo en pagos, membresías, perfiles y asignaciones. Evaluaciones: COACH ve las de sus clientes asignados (vía `coach_client`); CLIENT solo las suyas. `GET /api/v1/evaluations?userId=` filtra por cliente (id del User cliente), con las mismas reglas por rol; un COACH con cliente no asignado recibe 403. `client-routine/{id}` lo puede leer COACH sobre cualquier asignación. Id inexistente → 404; ajeno → 403.
  - Sin token → 401; sin permiso → 403 en JSON (`JsonAccessDeniedHandler`).
- Errores en JSON (`ErrorResponse`): validación (`MethodArgumentNotValidException`) → 400 con campo `errors`; `IllegalArgumentException` → 400; `ForbiddenOperationException` (extiende `AccessDeniedException`) y `AccessDeniedException` → 403 por un único handler, mismo formato. Regla de estilo: los errores de permiso lanzan una de estas dos excepciones; `DataIntegrityViolationException` → 409 (mensaje neutro, sin SQL). `IllegalStateException` no capturada → 500.
- Login: un usuario con `active` false o null no obtiene token (`CustomUserDetailsService` lo marca deshabilitado → 401 JSON).
- Endpoints de `client-routine`: POST, GET (lista), GET `/{id}`, PUT `/{id}` y DELETE `/{id}` (COACH).
- Las rutas mezclan plural (`/exercises`) y singular (`/exercise-category`).

## Base de datos y entidades
| Entidad (tabla) | Campos principales | Relaciones |
|---|---|---|
| `User` (`users`) | name, lastName, username, email, phone, passwordHash, role, active, createdAt | — |
| `ClientProfile` (`client_profile`) | birthDate, initialWeight, initialHeight, createdAt | 1:1 → `User` |
| `CoachClient` | — | N:1 → `User` (coach), N:1 → `User` (client) |
| `BodyRegion` (`body_region`) | name | — |
| `ExerciseCategory` (`exercise_category`) | name | 1:N → `Exercise` |
| `Exercise` (`exercise`) | name, description, isWeighted | N:1 → `ExerciseCategory` |
| `TypeRoutine` (`type_routine`) | name | 1:N → `Routine` |
| `Routine` (`routine`) | name | N:1 → `TypeRoutine`; 1:N → `ClientRoutine` |
| `RoutineExercise` (`routine_exercise`) | sets, reps, restSeconds | N:1 → `Routine`, `Exercise`, `BodyRegion` |
| `ClientRoutine` (`client_routine`) | startDate, endDate, observations, active | N:1 → `User`, `Routine` |
| `Evaluation` (`evaluation`) | maxReps, maxWeight, registerDate, notes | N:1 → `User`, `Exercise` |
| `MembershipPlan` (`membership_plan`) | name, price, durationDays, createdAt | 1:N → `ClientMembership` |
| `ClientMembership` (`client_membership`) | startDate, endDate, status, createdAt | N:1 → `User`, `MembershipPlan` |
| `ClientPayment` (`client_payment`) | paymentDate, amount, paymentMethod, status, reference, notes, createdAt | N:1 → `User`, `ClientMembership` |

**Enums** (`model/catalogs/`, guardados como texto):
- `Role`: ADMIN, COACH, CLIENT
- `Status`: ACTIVE, INACTIVE, SUSPENDED
- `MembershipStatus`: ACTIVE, EXPIRED, CANCELLED
- `PaymentStatus`: PAID, PENDING

## Funcionalidades pendientes
Detectadas con evidencia en el código:
- **Membresías sin expiración automática:** una membresía ACTIVE con `endDate` pasada sigue ACTIVE (y bloquea crear otra) hasta cambiarla a mano a EXPIRED.
- **Regla de una sola ACTIVE sin protección en BD:** se comprueba en el servicio (comprobar y guardar, sin restricción única ni `@Transactional`); dos peticiones simultáneas podrían crear dos ACTIVE.
- **Formato de errores 400:** `IllegalArgumentException` devuelve un `String` plano, mientras que 404 y 409 devuelven `ErrorResponse`.
- **Serialización de entidades LAZY:** resuelta en todos los controladores (devuelven DTOs). Pendiente solo verificar en ejecución real contra MySQL con tokens reales (hoy solo MockMvc).
- **Contradicción de requisitos (perfil de cliente):** al inicio se dijo que el CLIENT puede editar su información personal; después se decidió que en `/api/v1/clients` solo escribe ADMIN. Hoy un CLIENT no puede editar su propio perfil. Confirmar.
- **Pendientes de decisión:**
  - Cómo se crea el perfil de cliente del propio CLIENT (`POST /api/v1/clients` solo ADMIN).
  - Inconsistencia de lectura: en `client-routine`, la lista de COACH muestra solo las suyas, pero `GET /{id}` le deja leer cualquiera.
  - Pagos y membresías: ADMIN gestiona; si el registro se hizo antes con otro criterio, revisar datos existentes.
  - Datos existentes: asignaciones de rutina antiguas con dueño no-CLIENT dan 400 al editarlas.
- **Token vigente tras cambios:** un token ya emitido sigue válido hasta caducar aunque se desactive al usuario o se le cambie el rol (`JwtAuthenticationFilter` no consulta `active`). Decisión de negocio.
- **Perfil propio del CLIENT:** `POST /api/v1/clients` sigue solo para ADMIN; falta decidir cómo se crea el perfil del propio CLIENT.
- **Un ADMIN puede editar o degradar a otro ADMIN** sin restricción (comportamiento actual, confirmar si es lo deseado).
- **Registro público y CRUD de usuarios:** ambos responden 409 en username o email duplicado y comparten el límite de username (4-50, igual que la columna).
- **Pruebas de servicio:** `EvaluationServiceTest.update_guardaYDevuelveDto` no tiene `verify(save)` explícito; falta prueba directa de `EvaluationService.save` (userId/exerciseId).
- **`isWeighted` vs `weighted`:** `ExerciseResponse` serializa ambas claves (campo `isWeighted` y getter). `ExerciseRequest` usa `weighted`. El frontend debe elegir una; unificar es tarea aparte.
- **Validación incompleta:** `ExerciseController`, `RoutineController` y `RoutineExerciseController` no tienen `@Valid`; un id nulo termina en 500 en vez de 400. `ExerciseRequest`, `RoutineRequest` y `RoutineExerciseRequest` no tienen restricciones.
- **Cascadas peligrosas:** `ExerciseCategory.exercises`, `TypeRoutine.routines` y `Routine.clientRoutines` usan `cascade = ALL` y `orphanRemoval = true`: borrar una categoría, un tipo o una rutina borra en cascada sus hijos. Sin decidir.
- **Antes de añadir `@Transactional` al borrado de catálogos o rutinas:** revisar las cascadas `cascade = ALL` y `orphanRemoval = true` de `ExerciseCategory.exercises`, `TypeRoutine.routines` y `Routine.clientRoutines`. Hoy borrar una categoría, un tipo o una rutina borra sus hijos; la transacción no debe ocultar ni ampliar ese borrado sin decidirlo antes.
- **Supuestos de roles pendientes de confirmar:** ADMIN escribe solo en catálogos (`body-regions`, `exercise-category`, `type-routine`, `exercises`, `routines`, `routine-exercise`, `membership-plans`). `client-routine` se trata como no catálogo: nadie escribe por la API. Pagos y membresías: nadie escribe por la API (POST/PUT/DELETE → 403); ADMIN solo los consulta. Si alguno de estos supuestos no es el deseado, cambiar la matriz en `SecurityConfig`.
- **`/api/v1/clients/**` y `/api/v1/evaluations/**`:** siguen abiertos a cualquier autenticado en escritura. Un CLIENT podría escribir ahí; fuera de la matriz actual.
- **Orden 404 antes de 403:** en pagos y membresías se busca el registro antes de comprobar el dueño; un CLIENT puede saber qué ids existen. Decisión del usuario, pendiente de registrar como aceptada o cambiar.
- **`EvaluationService.update`** no llama a `save` ni usa `@Transactional`: probablemente no persiste los cambios. Sin verificar en ejecución.
- **Coach–cliente:** `CoachClientService` y `CoachClientController` implementados (`/api/v1/coach-clients`), con validación de rol y de perfil de cliente (404 si no tiene perfil), duplicado 409.
- **Usuarios:** CRUD completo en `UserController` (solo ADMIN), con BCrypt, baja lógica y autoprotección del ADMIN. `User.passwordHash` lleva `@JsonIgnore`.
- **`ClientRoutineController`:** ya tiene GET `/{id}`, PUT y DELETE (COACH). Pendiente: la regla de lectura de la lista (solo las suyas para COACH) difiere de `GET /{id}`.
- **Roles aplicados solo en `/api/v1/users/**`:** `JwtAuthenticationFilter` registra la autoridad `ROLE_<role>` del token y `SecurityConfig` exige ADMIN en esa ruta. El resto de rutas solo exige estar autenticado.
- **Pendiente:** `GET /api/v1/clients` (y demás endpoints) está abierto a cualquier usuario autenticado.
- **Pendiente:** el rol sigue dentro del JWT hasta que caduque; cambiar el rol de un usuario no invalida tokens ya emitidos.
- **Validación:** Bean Validation solo se usa en `LoginRequest`, `RegisterRequest`, pagos, membresías y clientes (`ClientProfileRequest`). `UserRequest` y `UserUpdateRequest` validan campos y tamaños de columna.
- **Pruebas:** pagos, membresías, clientes (`ClientControllerTest`, `ClientProfileServiceTest`) y usuarios (`UserControllerTest`) tienen pruebas. `CalisthenicsApplicationTests` (`contextLoads`) necesita MySQL en ejecución.
- **Carrera en `ClientProfileService.save`:** comprueba `existsByUserId` y luego guarda; con dos POST simultáneos para el mismo usuario puede saltar `DataIntegrityViolationException` (unique de `user_id`) y responder 500 en vez de 409.
- **Errores de validación sin formato propio:** `GlobalExceptionHandler` no captura `MethodArgumentNotValidException`; el 400 usa el cuerpo por defecto de Spring, sin indicar qué campo falló.
- **`RegisterRequest.role` ignorado:** `AuthService.register` fuerza `Role.CLIENT`; el campo `role` sigue en el DTO pero no se usa. Conviene quitarlo o documentarlo para el frontend.
- **Login con `role` null:** `AuthService.login` llama a `user.getRole().name()` sin comprobar null (NPE si el rol no se guardó).
- **Prueba de regresión para `/error`:** no hay prueba que impida quitar `/error` de `permitAll`; sin ella, un 400 con token válido volvería a salir como 401 (solo se comprobó a mano con servidor real).

## Decisiones técnicas
- Autenticación **stateless con JWT**; el token guarda id de usuario y rol. Contraseñas con **BCrypt**.
- **Clave JWT fuera del código (Fase 4):** `JwtService` la recibe por constructor (`@Value`), valida ≥32 bytes al arrancar (si no, la app no arranca) y construye la `SecretKey` una sola vez. `isTokenValid` devuelve `false` ante tokens caducados, manipulados o mal formados (la petición sigue sin autenticar → 401). La clave antigua (hardcodeada) se considera comprometida.
- **CORS (Fase 5):** `config/CorsConfig` define el `CorsConfigurationSource`; `SecurityConfig` lo activa con `.cors(...)`. Orígenes en `cors.allowed-origins` (por defecto `http://localhost:5173`, varios separados por coma). Métodos GET, POST, PUT, DELETE, OPTIONS; cabeceras `Authorization` y `Content-Type`; sin credenciales (autenticación solo Bearer).
- **401 sin token o con token inválido (Fase 5):** `security/JsonAuthenticationEntryPoint` responde 401 con `ErrorResponse` en JSON. `/auth/**` y `/error` son públicos; `/error` debe serlo para que los errores de la app (400, 404) no se conviertan en 401.
- **Login (Fase 5):** devuelve `LoginResponse` {token, role, userId} en JSON, para que el frontend conozca el rol sin decodificar el JWT.
- **Inyección de dependencias por constructor** (campos `private final`).
- Entidades con **Lombok**; relaciones `FetchType.LAZY`; enums con `EnumType.STRING`.
- Errores centralizados en `GlobalExceptionHandler`.
- Esquema de BD generado por Hibernate (`ddl-auto=update`), sin migraciones.
- `application.properties` excluido de git por contener credenciales.
- **Seed de arranque (`config/DataSeeder`, `config/SeedProperties`):** desactivado por defecto. Con `APP_SEED_ENABLED=true` crea, si no existen, un ADMIN (solo si no hay ningún ADMIN), un COACH y un CLIENT con su perfil de cliente. Variables obligatorias cuando está activo: `APP_SEED_{ADMIN,COACH,CLIENT}_{USERNAME,EMAIL,PASSWORD,NAME}`; opcionales: `_LAST_NAME` y `_PHONE`. Las contraseñas se guardan con BCrypt y nunca se escriben en logs ni en el código. Si falta una variable o su email/username ya lo usa otro usuario, la app no arranca y el mensaje nombra la variable. Para el primer arranque: definir las variables, levantar la app una vez y desactivar `APP_SEED_ENABLED`. No crea membresías, pagos, evaluaciones ni asignaciones.
- Desarrollo asistido por agentes de Claude Code (`.claude/agents/`): `explorer` (solo lectura), `developer` (implementa y escribe pruebas) y `verifier` (revisa y ejecuta build/pruebas, no modifica archivos). Modelos: explorer → haiku, developer → sonnet, verifier → opus. El orquestador es la sesión principal.
- Pruebas de controlador con `@WebMvcTest` deben incluir `@Import(SecurityConfig.class)` y `@MockBean` de `JwtService`, `CustomUserDetailsService` y `UserRepository`; sin el import se aplica la seguridad por defecto (CSRF activo) y POST/PUT/DELETE devuelven 403.

- **Membresías (Fase 3):** `endDate = startDate + durationDays - 1` (inclusivo), lo calcula el sistema y se recalcula en update; cambiar `durationDays` de un plan no recalcula membresías existentes (foto del momento de compra). `status` opcional (ACTIVE por defecto; null en update no sobrescribe). El usuario debe ser CLIENT (400). Una sola membresía ACTIVE por usuario (400). Borrar un plan con membresías o una membresía con pagos → 409. Reasignar a otro usuario una membresía con pagos → 409. En pagos, la membresía debe pertenecer al `userId` del pago (400). Las dependencias se comprueban en el servicio, no por excepción de BD.

## Último avance
- **2026-11-27 (estilo 403 y registro, última ronda):** `ForbiddenOperationException` extiende `AccessDeniedException` y un único handler responde 403; `/auth/register` responde 409 en username o email duplicado (antes 400); `RegisterRequest` con los mismos límites que `UserRequest` (username 4-50, email 100, password 8-72 sin solo espacios, nombre 100, teléfono 20). Suite final: 401 pruebas, 0 fallos, incluido `CalisthenicsApplicationTests`. Verifier: ⚠️ menor, corregido.
- **2026-11-27 (cierre de permisos, usuarios y coach-clients):** matriz de permisos final por rol en `SecurityConfig`; `client-routine` con GET `/{id}`, PUT y DELETE (COACH) y asignación solo a CLIENT con perfil (404 si no tiene); `CoachClientService` y `CoachClientController` (`/api/v1/coach-clients`); CRUD de usuarios (`UserController`, solo ADMIN, BCrypt, baja lógica, autoprotección del ADMIN); login de usuario inactivo bloqueado; `ForbiddenOperationException` como excepción de negocio y `IllegalStateException` sin mapeo (500); errores de validación y de integridad en JSON; `EvaluationRequest` validado. Verificaciones: ⚠️ sin hallazgos críticos en cada ronda, con los menores corregidos. Suite final: 389 pruebas, 0 fallos, incluido `CalisthenicsApplicationTests` con MySQL.
- **2026-11-27 (seguridad de membresías y pagos + serialización de catálogos):** (1) Membresías y pagos por rol: ADMIN ve todo; CLIENT y COACH solo los suyos. Listado sin `userId` ya no devuelve todo (no ADMIN recibe solo los suyos; `userId` ajeno → 403). GET/PUT/DELETE de id ajeno → 403; id inexistente → 404. POST con `userId` ajeno → 403. Update no permite reasignar a otro usuario. `CurrentUser` (record en `security/`) lleva el id y el flag admin al servicio. `AccessDeniedException` → 403 con `ErrorResponse`. `/auth/register` fuerza CLIENT. (2) Catálogos y rutinas: GET/PUT/POST devuelven DTOs (`from()` en los Response); nuevos `BodyRegion`, `ExerciseCategory`, `TypeRoutine` Request/Response; `@Valid` en catálogos y client-routine; `ClientRoutineRequest` con `@NotNull`. Pruebas nuevas de controlador (JSON sin relaciones anidadas) y de servicio. Verifier: ⚠️ con observaciones (sin hallazgos críticos; ver pendientes). (3) Matriz de permisos completa: escrituras de catálogos, evaluaciones y clientes solo ADMIN; pagos, membresías y client-routine sin escrituras por API (denyAll); `JsonAccessDeniedHandler` para 403 en JSON; lecturas de evaluaciones, clientes y client-routine limitadas al propio usuario para CLIENT/COACH; `GET /api/v1/evaluations/{id}` y `GET /api/v1/clients/{id}` con 404/403. Verifier: ⚠️ sin hallazgos críticos. Suite final: 264 pruebas, 0 fallos, incluido `CalisthenicsApplicationTests` con MySQL.
- **2026-11-26 (clientes y usuarios):** DTOs de clientes (`ClientProfileRequest`/`ClientProfileResponse`, validación y 409 por duplicado), fuga de `passwordHash` cerrada con `@JsonIgnore` en `User`, `GET /api/v1/users` solo ADMIN (el filtro JWT ahora registra `ROLE_<role>`). 167 tests: 166 OK; `contextLoads` pendiente de MySQL.
- **2026-11-25:** agentes migrados de OpenCode a Claude Code (`.claude/agents/explorer.md`, `.claude/agents/developer.md`, `.claude/settings.json`, `CLAUDE.md`). Documentación inicial del proyecto (Fase 0) y convenciones de código añadidas a `AGENTS.md`.
- **2026-11-25 (Fase 1):** endpoints de pagos `/api/v1/client-payments` (nuevo `ClientPaymentController`; `ClientPaymentService` devuelve siempre DTO, añade `getById`, `delete`, filtro por usuario; `ClientPaymentRepository.findByUserId`). Decisiones: membresía obligatoria, `status` lo envía el cliente (PAID si no viene), borrado físico.
- **2026-11-25 (Fase 2):** nuevo agente `verifier` (`.claude/agents/verifier.md`). Primera verificación de pagos: veredicto OK, 25/25 pruebas (13 de servicio con Mockito, 12 de controlador con `@WebMvcTest` + `@Import(SecurityConfig.class)`). Ejecutar: `./mvnw test -Dtest=ClientPaymentServiceTest,ClientPaymentControllerTest`.
- **2026-11-25 (Fase 2, ciclo developer → verifier):** validación en pagos (`@NotNull` en userId, clientMembershipId, amount, paymentDate; `@Positive` en amount; `@Valid` en create/update). 37/37 pruebas (12 nuevas de 400 Bad Request).
- **2026-11-25 (Fase 3):** CRUD de membresías: `MembershipPlanController`/`Service`/`Repository` y `ClientMembershipController`/`Service` con sus DTOs; `MembershipPlan` pasa a Lombok y `@Table(name = "membership_plan")`; nueva `ConflictException` (409); validación membresía↔usuario en `ClientPaymentService`. Ciclo developer → verifier: ⚠️ (reasignación con pagos y límites de columna en `MembershipPlanRequest`: `@Size(max=100)`, `@Digits(6,2)`) → corregido → ✅. 122/122 pruebas. Ejecutar: `./mvnw test -Dtest="MembershipPlan*Test,ClientMembership*Test,ClientPayment*Test"`.
- **2026-11-25 (Fase 4):** clave JWT sacada del código (`jwt.secret` / `JWT_SECRET`), expiración configurable (`jwt.expiration-ms`), tokens inválidos → `false` en vez de excepción. `application-example.properties` versionado (excepción en `.gitignore`), README con cómo generar y definir `JWT_SECRET`. Ciclo developer → verifier: ⚠️ (README recomendaba `Get-Random`, no seguro → corregido con `RandomNumberGenerator`). 129/129 pruebas (7 nuevas en `JwtServiceTest`).
- **2026-11-26 (Fase 5, preparación para el frontend):** CORS configurable (`config/CorsConfig`), login en JSON `LoginResponse` {token, role, userId}, 401 en JSON para peticiones sin token o con token inválido (`security/JsonAuthenticationEntryPoint`), `UserResponse` sin `passwordHash` en `/auth/register`, `@Valid` en login y register. Ciclo developer → verifier: ❌ (`/error` no era público: un 400 con token válido salía como 401; faltaban pruebas de CORS y de token inválido) → corregido → ⚠️ no bloqueante (falta prueba de regresión para `/error`). Pruebas nuevas: `AuthControllerTest` (8) y `AuthServiceTest` (3). Suite: 141 pruebas, 140 OK; `CalisthenicsApplicationTests.contextLoads` falla solo porque MySQL no está levantado. Ejecutar: `./mvnw test -Dtest="AuthControllerTest,AuthServiceTest"`.
