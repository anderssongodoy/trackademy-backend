create table if not exists silabo_migration_run (
  id bigserial primary key,
  curso_id bigint not null references curso(id) on delete cascade,
  periodo_id bigint null references periodo(id) on delete set null,
  silabo_origen_id bigint null references silabo(id) on delete set null,
  silabo_destino_id bigint not null references silabo(id) on delete cascade,
  trigger_tipo text not null default 'manual' check (trigger_tipo in ('manual','job','api')),
  estado text not null default 'running' check (estado in ('running','success','partial','failed')),
  usuarios_detectados int not null default 0,
  usuarios_actualizados int not null default 0,
  evaluaciones_creadas int not null default 0,
  evaluaciones_actualizadas int not null default 0,
  evaluaciones_obsoletas int not null default 0,
  summary_json jsonb null,
  started_at timestamptz not null default now(),
  finished_at timestamptz null
);

create index if not exists idx_silabo_migration_run_curso on silabo_migration_run(curso_id, started_at desc);

create table if not exists silabo_migration_item (
  id bigserial primary key,
  silabo_migration_run_id bigint not null references silabo_migration_run(id) on delete cascade,
  usuario_periodo_id bigint null references usuario_periodo(id) on delete set null,
  usuario_periodo_curso_id bigint not null references usuario_periodo_curso(id) on delete cascade,
  silabo_origen_id bigint null references silabo(id) on delete set null,
  silabo_destino_id bigint not null references silabo(id) on delete cascade,
  estado text not null default 'updated' check (estado in ('updated','skipped','warning','failed')),
  detail_json jsonb null,
  created_at timestamptz not null default now()
);

create index if not exists idx_silabo_migration_item_run on silabo_migration_item(silabo_migration_run_id);

alter table if exists usuario_periodo_evaluacion
  add column if not exists estado_migracion text not null default 'activa'
    check (estado_migracion in ('activa','obsoleta','reemplazada'));

alter table if exists usuario_periodo_evaluacion
  add column if not exists updated_at timestamptz not null default now();
