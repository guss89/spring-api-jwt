# Paquete de diseño del frontend — Calisthenics

Paquete de archivos para que Claude Code diseñe e implemente la interfaz web del servicio de entrenamiento de calistenia, sobre el front que ya existe en `CalisthenicsApp/apps/web` (React 19 + Vite + Mantine 9 + react-router 8 + i18next).

## Cómo usarlo

1. Copia la carpeta `diseno-frontend` dentro del repositorio `CalisthenicsApp` (por ejemplo en `docs/diseno-frontend/`), o deja los archivos a mano para dárselos a Claude Code.
2. Abre Claude Code en la raíz de `CalisthenicsApp`.
3. Pega el contenido de `00-prompt-maestro.md` como primer mensaje.
4. Trabaja módulo a módulo: pide primero el sistema de diseño (`01`), después la navegación por roles (`02`) y luego cada módulo (`03` a `06`).

## Índice

| Archivo | Para qué sirve |
|---|---|
| `00-prompt-maestro.md` | Instrucciones de partida para Claude Code: rol, stack, reglas y flujo de trabajo. |
| `01-sistema-de-diseno.md` | Tokens, tema de Mantine, tipografía, color, espaciado, componentes base, estados y accesibilidad. |
| `02-arquitectura-de-informacion-y-roles.md` | Mapa de pantallas, navegación por rol (ADMIN, COACH, CLIENT) y flujos principales. |
| `03-modulo-autenticacion-y-usuarios.md` | Login, registro, perfil y gestión de usuarios (solo ADMIN). |
| `04-modulo-clientes-membresias-pagos.md` | Perfil de cliente, planes de membresía, membresías y pagos. |
| `05-modulo-catalogos-rutinas.md` | Regiones del cuerpo, categorías, ejercicios, tipos de rutina, rutinas, ejercicios de rutina y asignación a clientes. |
| `06-modulo-evaluaciones-progreso.md` | Evaluaciones de progreso (repeticiones o peso máximo) y su visualización. |

## Fuentes de verdad

- Endpoints, DTOs y reglas de negocio: `docs/CONTEXT.md` y el código de `src/main/java/.../controller`, `dto` y `service` de la API.
- Textos e idioma: `apps/web/src/i18n/resources.ts` (el español es la fuente de verdad).
- Stack y tema actual: `apps/web/src/main.tsx`, `apps/web/package.json`.

## Limitaciones conocidas de la API que afectan al diseño

- Algunos GET/PUT devuelven entidades en lugar de DTO (catálogos, rutinas, evaluaciones). Pueden faltar campos o venir con relaciones anidadas. Diseña para el DTO y marca como pendiente lo que no esté.
- Hay endpoints sin control de rol: cualquier usuario autenticado puede llamarlos. El diseño debe ocultar opciones por rol, pero no puede asumir que la API lo impide.
- `CoachClientService` no tiene controlador: la relación coach–cliente se diseña como “pendiente de API”.
- Las rutas mezclan plural y singular (`/exercises`, `/exercise-category`). Usa los nombres tal cual en la capa de datos.
- No hay paginación ni filtros en el servidor; las listas son completas.
