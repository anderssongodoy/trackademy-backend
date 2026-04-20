alter table if exists curso_carrera_ciclo
  add column if not exists id bigserial;

alter table if exists curso_carrera_ciclo
  drop constraint if exists curso_carrera_ciclo_pkey;

alter table if exists curso_carrera_ciclo
  alter column campus_id drop not null;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conrelid = 'curso_carrera_ciclo'::regclass
      and contype = 'p'
  ) then
    alter table curso_carrera_ciclo
      add constraint curso_carrera_ciclo_pkey primary key (id);
  end if;
end $$;

create unique index if not exists uq_curso_carrera_ciclo_global
  on curso_carrera_ciclo(curso_id, carrera_id)
  where campus_id is null;

create unique index if not exists uq_curso_carrera_ciclo_campus
  on curso_carrera_ciclo(curso_id, carrera_id, campus_id)
  where campus_id is not null;
