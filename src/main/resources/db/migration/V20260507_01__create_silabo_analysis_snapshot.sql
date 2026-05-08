create table if not exists silabo_analysis_snapshot (
  id bigserial primary key,
  silabo_id bigint not null references silabo(id) on delete cascade,
  hash_pdf text not null,
  resumen text not null,
  temas_json jsonb not null,
  recursos_json jsonb not null,
  model text not null,
  prompt_tokens int null,
  completion_tokens int null,
  generated_at timestamptz not null default now(),
  created_at timestamptz not null default now()
);

create unique index if not exists ux_silabo_analysis_snapshot_hash
  on silabo_analysis_snapshot(hash_pdf);

create index if not exists idx_silabo_analysis_snapshot_silabo
  on silabo_analysis_snapshot(silabo_id);
