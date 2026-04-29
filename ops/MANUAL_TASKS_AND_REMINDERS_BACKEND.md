# Manual Tasks And Reminders Backend

## Decision

- `usuario_tarea` is the real source of truth for manual student work.
- `recordatorio_evento` is an operational reminder layer linked to tasks.
- `recordatorios` should not be treated as a separate product surface with its own long-term page.
- Google Calendar sync should project manual tasks with due date.
- Google Calendar sync should **not** create separate reminder events, to avoid clutter and duplicates.

## What was implemented

### New backend capabilities

- Manual task CRUD under `/api/v1/me/tareas`
- Reminder listing under `/api/v1/me/recordatorios`
- Task reminders persisted through `recordatorio_evento`
- Google Calendar incremental sync extended with manual tasks

### New/updated persistence

- New entity: `UsuarioTareaEntity`
- New entity: `RecordatorioEventoEntity`
- New migration: `V20260429_01__extend_task_reminders.sql`
- `recordatorio_evento` now supports `usuario_tarea_id`

## API

### Tasks

- `GET /api/v1/me/tareas`
- `POST /api/v1/me/tareas`
- `PUT /api/v1/me/tareas/{tareaId}`
- `DELETE /api/v1/me/tareas/{tareaId}`

Task payload:

```json
{
  "usuarioPeriodoCursoId": 7,
  "titulo": "Preparar avance",
  "descripcion": "Corregir la sección de arquitectura",
  "tipo": "tarea",
  "prioridad": "alta",
  "estado": "pendiente",
  "fechaVencimiento": "2026-05-12T23:59:00-05:00",
  "fechaRecordatorio": "2026-05-11T18:00:00-05:00",
  "canalRecordatorio": "app"
}
```

### Reminders

- `GET /api/v1/me/recordatorios?from=2026-05-01&to=2026-05-31`

Returns pending reminder events linked to manual tasks.

## Calendar sync behavior

- A manual task is syncable only if:
  - it has `fechaVencimiento`
  - it is not `completada`
  - it is not `cancelada`
- Source key format:
  - `tarea:{id}`
- If the task is completed or cancelled, the next sync deletes the Google event.
- If the task due date or content changes, the next sync updates the Google event.
- If the task is deleted, the next sync deletes the Google event mapping.

## UX implication

- `Tareas` can now be repurposed around manual work created by the student.
- `Recordatorios` no longer needs to be a standalone planning page.
- A future frontend should treat reminders as secondary signals attached to tasks, schedule and evaluations.
