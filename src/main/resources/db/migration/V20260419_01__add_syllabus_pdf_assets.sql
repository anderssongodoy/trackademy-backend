create table if not exists silabo_pdf_asset (
  id bigserial primary key,
  public_id uuid not null default gen_random_uuid(),
  storage_provider text not null check (storage_provider in ('filesystem','s3','minio','r2','manual')),
  storage_key text not null,
  original_filename text not null,
  mime_type text not null default 'application/pdf',
  size_bytes bigint null check (size_bytes is null or size_bytes > 0),
  sha256 text not null,
  source_path text null,
  created_at timestamptz not null default now(),
  unique(public_id),
  unique(storage_provider, storage_key)
);

create index if not exists idx_silabo_pdf_asset_sha256 on silabo_pdf_asset(sha256);

alter table if exists silabo
  add column if not exists pdf_asset_id bigint null references silabo_pdf_asset(id) on delete set null;

create index if not exists idx_silabo_pdf_asset_id on silabo(pdf_asset_id);
