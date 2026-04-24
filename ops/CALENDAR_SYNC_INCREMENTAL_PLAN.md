# Trackademy Calendar Sync Incremental Plan

## Objetivo

Sincronizar Trackademy hacia Google Calendar sin duplicados, sin eventos huerfanos y sin depender de un `full delete + recreate` en cada cambio.

La fuente de verdad siempre es Trackademy.
Google Calendar es una proyeccion externa del estado academico del usuario.

## Principios

1. `one-way sync`
   Trackademy -> Google Calendar.
   No se leeran ni reconciliaran cambios hechos manualmente en Google en esta etapa.

2. identidad estable por evento sincronizable
   Cada evento local debe tener una clave externa deterministica para poder decidir si crear, actualizar o borrar.

3. diff incremental
   Cada corrida compara:
   - eventos locales proyectados hoy
   - mappings sincronizados anteriormente

   Resultado:
   - `create`: existe localmente pero no en mapping
   - `update`: existe en ambos pero el contenido cambio
   - `delete`: existe en mapping pero ya no existe localmente
   - `noop`: existe en ambos y no cambio

4. resiliencia
   Si falla la vinculacion de calendario o una corrida de sync, el login no debe romperse.

5. extensibilidad
   La arquitectura debe soportar mas tipos de evento sin redisenar el motor:
   - horario
   - evaluacion
   - recordatorio
   - tarea
   - eventos de periodo

## Tipos iniciales de evento

### Horario

Clave externa:

`horario:{usuarioPeriodoCursoId}:{bloqueNro}:{yyyy-MM-dd}`

Razon:
- representa una ocurrencia concreta dentro del periodo
- evita recurrencias complejas en MVP
- simplifica update/delete cuando cambian bloques o dias

### Evaluacion

Clave externa:

`evaluacion:{usuarioPeriodoCursoId}:{evaluacionCodigo}`

### Periodo

Clave externa:

`periodo:{tipo}:{yyyy-MM-dd}`

### Futuro: Recordatorio / tarea

Claves previstas:

- `recordatorio:{recordatorioId}`
- `tarea:{tareaId}`

## Persistencia

Se agrega una tabla dedicada para mapping local <-> Google:

- `calendar_sync_event`

Campos principales:

- `calendar_sync_account_id`
- `source_key`
- `source_type`
- `source_hash`
- `google_event_id`
- `google_calendar_id`
- `estado`
- `last_synced_at`
- `last_seen_at`
- `error_message`

## source_hash

Hash deterministico del payload sincronizable.

Debe cambiar si cambia cualquiera de estos campos relevantes:

- titulo
- subtitulo / descripcion
- inicio
- fin
- all_day
- codigoCurso
- nombreCurso
- referenciaCodigo

No depende de metadata interna de BD que no afecte el evento visible.

## Flujo de planificacion

1. Proyectar eventos locales sincronizables para un rango
2. Cargar mappings existentes de la cuenta Google
3. Indexar ambos por `source_key`
4. Calcular operaciones:
   - local y sin mapping -> `create`
   - local y mapping con hash distinto -> `update`
   - mapping sin local -> `delete`
   - local y mapping con mismo hash -> `noop`

## Casos cubiertos

### Cambio de horario

- cambia el hash de las ocurrencias afectadas
- `update` o `delete + create` segun cambie la clave

### Cambio de evaluacion en silabo

- si solo cambia fecha/titulo -> `update`
- si desaparece -> `delete`

### Curso eliminado

- desaparecen todos sus `source_key`
- el plan devuelve `delete` para todos sus eventos sincronizados

### Curso agregado

- aparecen nuevos `source_key`
- el plan devuelve `create`

### Recordatorio/tarea eliminada

- `delete`

## Estrategia MVP

### Fase 1

- Vinculacion Google OAuth
- Tabla de mappings
- API de plan de sync
- Sync de:
  - horarios
  - evaluaciones
  - eventos de periodo

### Fase 2

- Ejecucion real contra Google Calendar
- Persistencia de resultados por evento
- manejo de `refresh token`
- desconexion de calendario

### Fase 3

- recordatorios
- tareas
- resincronizacion total
- resolucion automatica de drift

## Reglas operativas

1. Nunca borrar todo salvo `resync full` explicito
2. Nunca crear sin verificar `source_key`
3. Nunca asumir que Google es fuente de verdad
4. Registrar log por corrida y por evento si falla la operacion externa

## API inicial

`GET /api/v1/me/calendar-sync/google/plan?from=YYYY-MM-DD&to=YYYY-MM-DD`

Devuelve:

- cuenta conectada o no
- rango evaluado
- resumen por operacion
- lista de items con `create|update|delete|noop`

Esto permite validar la logica incremental antes de activar escrituras reales en Google Calendar.
