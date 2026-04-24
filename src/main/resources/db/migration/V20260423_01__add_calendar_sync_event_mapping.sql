create table if not exists calendar_sync_event (
  id bigserial primary key,
  calendar_sync_account_id bigint not null references calendar_sync_account(id) on delete cascade,
  source_key text not null,
  source_type text not null,
  source_hash text not null,
  source_start_at timestamp not null,
  source_end_at timestamp not null,
  google_calendar_id text null,
  google_event_id text null,
  estado text not null default 'pending' check (estado in ('pending','synced','error','deleted')),
  error_message text null,
  last_seen_at timestamptz not null default now(),
  last_synced_at timestamptz null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(calendar_sync_account_id, source_key)
);

create index if not exists idx_calendar_sync_event_account_range
  on calendar_sync_event(calendar_sync_account_id, source_start_at, source_end_at);
