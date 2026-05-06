create table if not exists academic_radar_snapshot (
  id bigserial primary key,
  usuario_id bigint not null references usuario(id) on delete cascade,
  usuario_periodo_id bigint not null references usuario_periodo(id) on delete cascade,
  input_hash text not null,
  radar_version text not null,
  model text null,
  ai_generated boolean not null default false,
  prompt_tokens int null,
  completion_tokens int null,
  payload_json jsonb not null,
  generated_at timestamptz not null default now(),
  valid_until timestamptz not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create unique index if not exists ux_academic_radar_snapshot_period_version
  on academic_radar_snapshot(usuario_periodo_id, radar_version);

create index if not exists idx_academic_radar_snapshot_user_period
  on academic_radar_snapshot(usuario_id, usuario_periodo_id, generated_at desc);
