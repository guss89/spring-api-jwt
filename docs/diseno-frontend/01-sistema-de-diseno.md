# 01 · Sistema de diseño

Base visual para toda la aplicación. Se implementa como tema de Mantine (`MantineProvider` en `apps/web/src/main.tsx`) y tokens CSS en `index.css`.

## Principios
1. **Claridad para el entrenamiento:** el cliente debe entender en segundos qué le toca hoy y cuánto ha progresado.
2. **Datos primero:** tablas y tarjetas con cifras legibles (pesos, repeticiones, fechas, importes).
3. **Un solo patrón por tarea:** alta y edición en modal, listados en tabla con acciones en fila, detalle en página.
4. **Carga y error siempre visibles:** nada debe quedar en blanco.
5. **Idioma único por pantalla:** todo a través de i18n.

## Marca
- Nombre: **Calisthenics** (`common.appName`).
- Tono: directo, motivador sin ser infantil, sobrio en el panel de administración.
- Modo claro por defecto; propón modo oscuro como opción de Mantine (`defaultColorScheme="auto"`).

## Color (propuesta a validar)
Usa la escala de Mantine con un color de marca y uno de acento. Propuesta inicial:

| Token | Uso | Propuesta |
|---|---|---|
| `primary` | Botones principales, enlaces activos, foco | Azul índigo (escala `indigo`) |
| `accent` | Progreso, logros, destacados | Verde (escala `teal`) |
| `warning` | Pagos pendientes, membresía por caducar | `yellow` |
| `danger` | Errores, eliminar, membresía cancelada | `red` |
| `neutral` | Texto, bordes, fondos | Escala `gray` |

Reglas:
- Estados de pago y membresía usan color **y** texto (nunca solo color).
- Contraste mínimo 4.5:1 para texto normal, 3:1 para elementos de interfaz.

## Tipografía
- Familia: la de Mantine por defecto; propón una alternativa solo si se justifica.
- Escala: `Title order={2}` para cabeceras de página, `order={3}` para secciones, `Text size="sm"` para metadatos.
- Cifras en tablas con `fw={500}` y alineadas a la derecha.

## Espaciado y layout
- Usa los tamaños de Mantine (`xs`, `sm`, `md`, `lg`, `xl`) y `Stack`/`Group` en lugar de márgenes sueltos.
- Ancho de contenido: página completa dentro de `AppShell.Main`.
- Navegación lateral de 240 px en escritorio; se oculta en móvil tras `Burger`.

## Componentes base (de Mantine)
| Necesidad | Componente |
|---|---|
| Layout | `AppShell`, `Stack`, `Group`, `Grid`, `Paper` |
| Navegación | `NavLink`, `Tabs`, `Breadcrumbs` (solo si hay niveles de detalle) |
| Listados | `Table` (escritorio), `Card` o `Stack` de tarjetas (móvil) |
| Formularios | `TextInput`, `NumberInput`, `Select`, `DateInput`, `Textarea`, `PasswordInput` |
| Acciones | `Button`, `ActionIcon` con `aria-label`, `Menu` para acciones secundarias |
| Confirmación | `Modal` con botón destructivo claramente marcado |
| Feedback | `Alert`, `Notification`, `Loader`, `Skeleton` |
| Vacío | `Text c="dimmed"` con una acción principal |

## Patrones de interacción
- **Alta/edición:** modal (`ClientFormModal` es el modelo). Si el formulario tiene más de 8 campos, usa página.
- **Borrado:** confirmación con nombre del elemento y aviso de que es irreversible (la API borra físicamente en pagos; revisa cada módulo).
- **Errores de la API:** usa `errorMessage(error, fallback)` de `api/http.ts`. Para 409 muestra el motivo; para 400 indica el campo si la API lo da.
- **Sesión caducada (401):** redirige a `/login` y muestra aviso.

## Estados obligatorios
Para cada pantalla de datos:
1. **Cargando:** `Skeleton` o `Loader` centrado.
2. **Vacío:** mensaje y botón de alta.
3. **Error de carga:** `Alert` rojo con reintento.
4. **Error de acción:** `Alert` junto al formulario o encima de la tabla.
5. **Éxito:** notificación breve.
6. **Sin permiso:** pantalla explicativa, no página en blanco.

## Formularios
- Etiqueta siempre visible (no solo placeholder).
- Validación al salir del campo y al enviar.
- Campos obligatorios marcados con `*` y indicados en el pie del formulario.
- Números con unidades en la etiqueta: “Peso inicial (kg)”, “Altura (cm)”, “Importe (€)”. Revisa la moneda con el cliente antes de fijarla.
- Fechas en formato local, guardadas en ISO (`YYYY-MM-DD`).

## Accesibilidad
- Foco visible en todos los controles.
- `aria-label` en botones solo con icono.
- Tablas con cabeceras `Table.Th` y orden de tabulación lógico.
- Modales con foco atrapado y cierre con Escape (Mantine lo hace; no lo rompas).
- Respeta `prefers-reduced-motion`.

## Idioma
- Español como fuente de verdad; inglés con las mismas claves.
- Fechas, números y monedas con `Intl` según el idioma activo.

## Entregable de esta fase
- Tema de Mantine (`createTheme`) con colores, radios y tipografía.
- Lista de componentes propios (si los hay) con su props.
- Capturas o descripción de una pantalla de referencia (la de clientes).
