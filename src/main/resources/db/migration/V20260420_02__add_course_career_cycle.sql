create table if not exists curso_carrera_ciclo (
  curso_id bigint not null references curso(id) on delete cascade,
  carrera_id bigint not null references carrera(id) on delete cascade,
  campus_id bigint null references campus(id) on delete set null,
  ciclo_referencial int not null check (ciclo_referencial between 1 and 20),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  primary key (curso_id, carrera_id, campus_id)
);

create index if not exists idx_curso_carrera_ciclo_carrera on curso_carrera_ciclo(carrera_id, ciclo_referencial, curso_id);
