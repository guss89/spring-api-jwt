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
6. Los errores los traduce `GlobalExceptionHandler`: `ResourceNotFoundException` → 404, `IllegalArgumentException` → 400, `IllegalStateException` → 403, `ConflictException` → 409.

**Configuración:** `application.properties` (excluido de git) con MySQL en `localhost:3306/calisthenics`, `ddl-auto=update` (Hibernate crea/actualiza las tablas solo). Plantilla versionada: `src/main/resources/application-example.properties`. JWT: `jwt.secret` (obligatoria, ≥32 bytes; se toma de la variable de entorno `JWT_SECRET`) y `jwt.expiration-ms` (opcional, 3600000 por defecto).

## Controladores y endpoints
| Controlador | Ruta base | Endpoints | Entrada / salida |
|---|---|---|---|
| `AuthController` | `/auth` | `POST /register`, `POST /login` | `RegisterRequest` → `UserResponse` (sin `passwordHash`); `LoginRequest` → `LoginResponse` {token, role, userId} |
| `BodyRegionController` | `/api/v1/body-regions` | POST, GET, PUT `/{id}`, DELETE `/{id}` | Entidad `BodyRegion` |
| `ClientController` | `/api/v1/clients` | POST, GET, PUT `/{id}`, DELETE `/{id}` | Entidad `ClientProfile` |
| `ClientRoutineController` | `/api/v1/client-routine` | POST, GET (sin PUT/DELETE) | `ClientRoutineRequest` → `ClientRoutineResponse`; GET devuelve entidades |
| `EvaluationController` | `/api/v1/evaluations` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `EvaluationRequest` → `EvaluationResponse`; GET/PUT devuelven entidad |
| `ExerciseCategoryController` | `/api/v1/exercise-category` | POST, GET, PUT `/{id}`, DELETE `/{id}` | Entidad `ExerciseCategory` |
| `ExerciseController` | `/api/v1/exercises` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `ExerciseRequest` → `ExerciseResponse`; GET/PUT devuelven entidad |
| `RoutineController` | `/api/v1/routines` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `RoutineRequest` → `RoutineResponse`; GET/PUT devuelven entidad |
| `RoutineExerciseController` | `/api/v1/routine-exercise` | POST, GET, PUT `/{id}`, DELETE `/{id}` | `RoutineExerciseRequest` → `RoutineExerciseResponse`; GET/PUT devuelven entidad |
| `TypeRoutineController` | `/api/v1/type-routine` | POST, GET, PUT `/{id}`, DELETE `/{id}` | Entidad `TypeRoutine` |
| `ClientPaymentController` | `/api/v1/client-payments` | POST, GET (`?userId=` opcional), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` | `ClientPaymentRequest` → siempre `ClientPaymentResponse` |
| `MembershipPlanController` | `/api/v1/membership-plans` | POST, GET, GET `/{id}`, PUT `/{id}`, DELETE `/{id}` | `MembershipPlanRequest` → siempre `MembershipPlanResponse` |
| `ClientMembershipController` | `/api/v1/client-memberships` | POST, GET (`?userId=` opcional), GET `/{id}`, PUT `/{id}`, DELETE `/{id}` | `ClientMembershipRequest` → siempre `ClientMembershipResponse` |

Notas:
- Hay mezcla de estilos: los catálogos simples reciben y devuelven la entidad; los recursos con relaciones usan DTO en POST pero devuelven entidades en GET/PUT.
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
- **`CoachClientService`** valida roles lanzando `RuntimeException` (responde 500, no 400).
- **Serialización de entidades LAZY:** los GET/PUT que devuelven entidades con relaciones LAZY (p. ej. `EvaluationController`, `ExerciseController`) probablemente fallan al convertir a JSON o exponen `User.passwordHash`. Sin verificar en ejecución.
- **`EvaluationService.update`** no llama a `save` ni usa `@Transactional`: probablemente no persiste los cambios. Sin verificar en ejecución.
- **Coach–cliente sin exponer:** `CoachClientService` (`assignCoachToClient`, `getClientsByCoach`, `getCoachesByClient`) no tiene controlador.
- **Usuarios sin exponer:** `UserService` tiene CRUD, pero no hay controlador.
- **`ClientRoutineController`** no tiene PUT ni DELETE.
- **Roles no aplicados:** `JwtAuthenticationFilter` extrae el rol del token, pero registra al usuario con `Collections.emptyList()` como permisos, así que cualquier usuario autenticado accede a todo.
- **Validación:** Bean Validation solo se usa en `LoginRequest`, `RegisterRequest`, pagos y membresías.
- **Pruebas:** solo pagos y membresías tienen pruebas. `CalisthenicsApplicationTests` (`contextLoads`) necesita MySQL en ejecución.
- **Errores de validación sin formato propio:** `GlobalExceptionHandler` no captura `MethodArgumentNotValidException`; el 400 usa el cuerpo por defecto de Spring, sin indicar qué campo falló.
- **Registro público permite elegir rol:** `RegisterRequest.role` se copia tal cual (`AuthService.register`), así que cualquiera puede registrarse como ADMIN en `/auth/register`. Pendiente de decidir: forzar CLIENT o restringir el rol.
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
- Desarrollo asistido por agentes de Claude Code (`.claude/agents/`): `explorer` (solo lectura), `developer` (implementa y escribe pruebas) y `verifier` (revisa y ejecuta build/pruebas, no modifica archivos). Modelos: explorer → haiku, developer → sonnet, verifier → opus. El orquestador es la sesión principal.
- Pruebas de controlador con `@WebMvcTest` deben incluir `@Import(SecurityConfig.class)` y `@MockBean` de `JwtService`, `CustomUserDetailsService` y `UserRepository`; sin el import se aplica la seguridad por defecto (CSRF activo) y POST/PUT/DELETE devuelven 403.

- **Membresías (Fase 3):** `endDate = startDate + durationDays - 1` (inclusivo), lo calcula el sistema y se recalcula en update; cambiar `durationDays` de un plan no recalcula membresías existentes (foto del momento de compra). `status` opcional (ACTIVE por defecto; null en update no sobrescribe). El usuario debe ser CLIENT (400). Una sola membresía ACTIVE por usuario (400). Borrar un plan con membresías o una membresía con pagos → 409. Reasignar a otro usuario una membresía con pagos → 409. En pagos, la membresía debe pertenecer al `userId` del pago (400). Las dependencias se comprueban en el servicio, no por excepción de BD.

## Último avance
- **2026-11-25:** agentes migrados de OpenCode a Claude Code (`.claude/agents/explorer.md`, `.claude/agents/developer.md`, `.claude/settings.json`, `CLAUDE.md`). Documentación inicial del proyecto (Fase 0) y convenciones de código añadidas a `AGENTS.md`.
- **2026-11-25 (Fase 1):** endpoints de pagos `/api/v1/client-payments` (nuevo `ClientPaymentController`; `ClientPaymentService` devuelve siempre DTO, añade `getById`, `delete`, filtro por usuario; `ClientPaymentRepository.findByUserId`). Decisiones: membresía obligatoria, `status` lo envía el cliente (PAID si no viene), borrado físico.
- **2026-11-25 (Fase 2):** nuevo agente `verifier` (`.claude/agents/verifier.md`). Primera verificación de pagos: veredicto OK, 25/25 pruebas (13 de servicio con Mockito, 12 de controlador con `@WebMvcTest` + `@Import(SecurityConfig.class)`). Ejecutar: `./mvnw test -Dtest=ClientPaymentServiceTest,ClientPaymentControllerTest`.
- **2026-11-25 (Fase 2, ciclo developer → verifier):** validación en pagos (`@NotNull` en userId, clientMembershipId, amount, paymentDate; `@Positive` en amount; `@Valid` en create/update). 37/37 pruebas (12 nuevas de 400 Bad Request).
- **2026-11-25 (Fase 3):** CRUD de membresías: `MembershipPlanController`/`Service`/`Repository` y `ClientMembershipController`/`Service` con sus DTOs; `MembershipPlan` pasa a Lombok y `@Table(name = "membership_plan")`; nueva `ConflictException` (409); validación membresía↔usuario en `ClientPaymentService`. Ciclo developer → verifier: ⚠️ (reasignación con pagos y límites de columna en `MembershipPlanRequest`: `@Size(max=100)`, `@Digits(6,2)`) → corregido → ✅. 122/122 pruebas. Ejecutar: `./mvnw test -Dtest="MembershipPlan*Test,ClientMembership*Test,ClientPayment*Test"`.
- **2026-11-25 (Fase 4):** clave JWT sacada del código (`jwt.secret` / `JWT_SECRET`), expiración configurable (`jwt.expiration-ms`), tokens inválidos → `false` en vez de excepción. `application-example.properties` versionado (excepción en `.gitignore`), README con cómo generar y definir `JWT_SECRET`. Ciclo developer → verifier: ⚠️ (README recomendaba `Get-Random`, no seguro → corregido con `RandomNumberGenerator`). 129/129 pruebas (7 nuevas en `JwtServiceTest`).
- **2026-11-26 (Fase 5, preparación para el frontend):** CORS configurable (`config/CorsConfig`), login en JSON `LoginResponse` {token, role, userId}, 401 en JSON para peticiones sin token o con token inválido (`security/JsonAuthenticationEntryPoint`), `UserResponse` sin `passwordHash` en `/auth/register`, `@Valid` en login y register. Ciclo developer → verifier: ❌ (`/error` no era público: un 400 con token válido salía como 401; faltaban pruebas de CORS y de token inválido) → corregido → ⚠️ no bloqueante (falta prueba de regresión para `/error`). Pruebas nuevas: `AuthControllerTest` (8) y `AuthServiceTest` (3). Suite: 141 pruebas, 140 OK; `CalisthenicsApplicationTests.contextLoads` falla solo porque MySQL no está levantado. Ejecutar: `./mvnw test -Dtest="AuthControllerTest,AuthServiceTest"`.
