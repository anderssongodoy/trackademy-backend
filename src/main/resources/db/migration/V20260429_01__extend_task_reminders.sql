alter table if exists recordatorio_evento
  add column if not exists usuario_tarea_id bigint null references usuario_tarea(id) on delete cascade;

create index if not exists idx_recordatorio_evento_tarea
  on recordatorio_evento(usuario_tarea_id, estado, fecha_envio);
