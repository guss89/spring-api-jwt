# 03 · Módulo: autenticación y usuarios

## Contratos de API
| Acción | Método y ruta | Entrada | Salida |
|---|---|---|---|
| Registro | `POST /auth/register` | `RegisterRequest`: name, lastName, username (4–20), email, phone, password (≥8), role | `UserResponse` (sin contraseña) |
| Login | `POST /auth/login` | `LoginRequest` (email, password) | `LoginResponse`: token, role, userId |
| Listar usuarios | `GET /api/v1/users` | — (solo ADMIN) | `UserResponse[]` |

Errores relevantes: 400 (validación; hoy llega como texto plano en algunos casos), 401 (credenciales o token), 403 (rol insuficiente en `/api/v1/users`).

> Pendiente de API: el registro permite elegir `role`, incluido ADMIN. En el diseño, el selector de rol **no aparece** en `/register` (el rol por defecto es CLIENT); se gestiona desde administración. Confirmar con negocio.

## Pantallas

### 3.1 Login (`/login`) — ya existe, revisar
- Campos: correo, contraseña (con botón mostrar/ocultar).
- Botón principal “Entrar”. Enlace secundario a registro.
- Error genérico “Correo o contraseña incorrectos.” (ya en i18n). No revelar si el correo existe.
- Tras entrar: redirigir a la ruta que se pidió antes de login, o a `/`.
- Estado de carga en el botón; deshabilitar el formulario mientras se envía.

### 3.2 Registro (`/register`) — ya existe, revisar
- Campos: nombre, apellido, usuario, correo, teléfono, contraseña.
- Ayuda bajo cada campo con la regla (p. ej. “Entre 4 y 20 caracteres”, “Mínimo 8 caracteres”).
- Validación en cliente con las mismas reglas que la API.
- Tras registrar: informar y pasar a login (no iniciar sesión automáticamente, salvo decisión de negocio).

### 3.3 Usuarios (`/usuarios`, solo ADMIN)
- **Listado:** tabla con nombre, usuario, correo, teléfono, rol (`Badge`) y estado activo.
- Filtro por rol (`SegmentedControl` o `Select`) y búsqueda por nombre o correo (en cliente, la API no filtra).
- Sin acciones de alta, edición ni borrado: la API actual solo expone listado. Diseña esos botones como **pendiente de API** (no los implementes).
- Fila clicable → panel lateral (`Drawer`) de solo lectura con los datos del usuario.

### 3.4 Acceso denegado
- Si un usuario sin rol ADMIN entra en `/usuarios`: pantalla con mensaje “No tienes permiso para ver esta página” y enlace a inicio. Nunca una pantalla en blanco.

## Componentes
- `PasswordInput` con indicador de fuerza simple (longitud); no inventes reglas que la API no tenga.
- `Badge` por rol con color distinto: ADMIN (`red`), COACH (`indigo`), CLIENT (`teal`).
- `Badge` de estado: activo (`green`) / inactivo (`gray`).

## Criterios de aceptación
- Login, registro y listado tienen todos los estados (carga, vacío, error).
- Ningún campo de contraseña se muestra en claro salvo que el usuario lo pida.
- Textos en i18n en ambos idiomas.
- Funciona en móvil (320 px de ancho) sin scroll horizontal.
