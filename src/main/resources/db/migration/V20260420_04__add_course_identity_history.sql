create table if not exists curso_codigo_historial (
  id bigserial primary key,
  curso_id bigint not null references curso(id) on delete cascade,
  codigo text not null,
  es_actual boolean not null default false,
  first_seen_at timestamptz not null default now(),
  last_seen_at timestamptz not null default now(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(curso_id, codigo)
);

create index if not exists idx_curso_codigo_historial_codigo on curso_codigo_historial(lower(codigo));
create index if not exists idx_curso_codigo_historial_actual on curso_codigo_historial(curso_id, es_actual);

create table if not exists curso_nombre_historial (
  id bigserial primary key,
  curso_id bigint not null references curso(id) on delete cascade,
  nombre text not null,
  es_actual boolean not null default false,
  first_seen_at timestamptz not null default now(),
  last_seen_at timestamptz not null default now(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(curso_id, nombre)
);

create index if not exists idx_curso_nombre_historial_nombre on curso_nombre_historial(lower(nombre));
create index if not exists idx_curso_nombre_historial_actual on curso_nombre_historial(curso_id, es_actual);

insert into curso_codigo_historial(curso_id, codigo, es_actual)
select c.id, c.codigo, true
from curso c
on conflict (curso_id, codigo) do update set
  es_actual = true,
  last_seen_at = now(),
  updated_at = now();

insert into curso_nombre_historial(curso_id, nombre, es_actual)
select c.id, c.nombre, true
from curso c
on conflict (curso_id, nombre) do update set
  es_actual = true,
  last_seen_at = now(),
  updated_at = now();
