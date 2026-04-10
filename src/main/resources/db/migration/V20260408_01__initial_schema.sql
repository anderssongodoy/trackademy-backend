-- Trackademy initial PostgreSQL schema.
-- Based on extraer-pdf/sql/schema.sql as the database reference, with WhatsApp MVP tables included.

create extension if not exists pgcrypto;
create extension if not exists unaccent;

-- =========================
-- Core catalog
-- =========================

create table if not exists universidad (
  id bigserial primary key,
  public_id uuid not null default gen_random_uuid(),
  nombre text not null unique,
  created_at timestamptz not null default now(),
  unique(public_id)
);

create table if not exists campus (
  id bigserial primary key,
  public_id uuid not null default gen_random_uuid(),
  universidad_id bigint not null references universidad(id) on delete cascade,
  nombre text not null,
  timezone text not null default 'America/Lima',
  created_at timestamptz not null default now(),
  unique(universidad_id, nombre),
  unique(public_id)
);

create table if not exists periodo (
  id bigserial primary key,
  public_id uuid not null default gen_random_uuid(),
  universidad_id bigint not null references universidad(id) on delete cascade,
  etiqueta text not null,
  fecha_inicio date null,
  fecha_fin date null,
  estado text not null default 'planificado' check (estado in ('planificado','activo','cerrado')),
  created_at timestamptz not null default now(),
  unique(universidad_id, etiqueta),
  unique(public_id),
  check (fecha_fin is null or fecha_inicio is null or fecha_fin >= fecha_inicio)
);

create table if not exists periodo_evento (
  id bigserial primary key,
  periodo_id bigint not null references periodo(id) on delete cascade,
  tipo text not null,
  titulo text not null,
  fecha_inicio date not null,
  fecha_fin date null,
  descripcion text null,
  fuente text not null default 'manual' check (fuente in ('manual','importado')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(periodo_id, tipo, titulo, fecha_inicio),
  check (fecha_fin is null or fecha_fin >= fecha_inicio)
);

create index if not exists idx_periodo_evento_periodo on periodo_evento(periodo_id);
create index if not exists idx_periodo_evento_fechas on periodo_evento(fecha_inicio, fecha_fin);

create table if not exists carrera (
  id bigserial primary key,
  public_id uuid not null default gen_random_uuid(),
  universidad_id bigint not null references universidad(id) on delete cascade,
  nombre text not null,
  created_at timestamptz not null default now(),
  unique(universidad_id, nombre),
  unique(public_id)
);

create table if not exists curso (
  id bigserial primary key,
  public_id uuid not null default gen_random_uuid(),
  universidad_id bigint not null references universidad(id) on delete cascade,
  codigo text not null,
  nombre text not null,
  course_key text not null,
  modalidad text null,
  creditos int null check (creditos is null or creditos >= 0),
  horas_semanales int null check (horas_semanales is null or horas_semanales >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(universidad_id, codigo),
  unique(public_id)
);

create index if not exists idx_curso_course_key on curso(course_key);

create table if not exists curso_carrera (
  curso_id bigint not null references curso(id) on delete cascade,
  carrera_id bigint not null references carrera(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (curso_id, carrera_id)
);

create index if not exists idx_curso_carrera_carrera on curso_carrera(carrera_id, curso_id);

-- =========================
-- Syllabus versioning
-- =========================

create table if not exists silabo (
  id bigserial primary key,
  public_id uuid not null default gen_random_uuid(),
  curso_id bigint not null references curso(id) on delete cascade,
  periodo_id bigint null references periodo(id) on delete set null,
  version text not null,
  vigente boolean not null default true,
  fuente_pdf text null,
  hash_pdf text null,
  anio int null,
  periodo_texto text null,
  sumilla text null,
  fundamentacion text null,
  metodologia text null,
  logro_general text null,
  extraido_en timestamptz null,
  created_at timestamptz not null default now(),
  unique(curso_id, version),
  unique(public_id)
);

create index if not exists idx_silabo_curso on silabo(curso_id);
create index if not exists idx_silabo_periodo on silabo(periodo_id);
create index if not exists idx_silabo_vigente on silabo(curso_id, vigente);

create table if not exists silabo_unidad (
  id bigserial primary key,
  silabo_id bigint not null references silabo(id) on delete cascade,
  nro int not null,
  titulo text null,
  semana_inicio int null check (semana_inicio is null or semana_inicio between 1 and 30),
  semana_fin int null check (semana_fin is null or semana_fin between 1 and 30),
  logro_especifico text null,
  created_at timestamptz not null default now(),
  unique(silabo_id, nro),
  check (semana_fin is null or semana_inicio is null or semana_fin >= semana_inicio)
);

create table if not exists silabo_tema (
  id bigserial primary key,
  silabo_unidad_id bigint not null references silabo_unidad(id) on delete cascade,
  orden int not null,
  titulo text not null,
  created_at timestamptz not null default now(),
  unique(silabo_unidad_id, orden)
);

create table if not exists silabo_evaluacion (
  id bigserial primary key,
  silabo_id bigint not null references silabo(id) on delete cascade,
  codigo text not null,
  tipo text null,
  descripcion text null,
  porcentaje numeric(6,2) null check (porcentaje is null or (porcentaje >= 0 and porcentaje <= 100)),
  semana int null check (semana is null or semana between 1 and 30),
  observacion text null,
  modalidad text null,
  individual_grupal text null,
  producto text null,
  flexible boolean not null default false,
  unidad_nro int null,
  atributos_json jsonb null,
  created_at timestamptz not null default now(),
  unique(silabo_id, codigo)
);

create index if not exists idx_silabo_eval_semana on silabo_evaluacion(silabo_id, semana);

create table if not exists silabo_bibliografia (
  id bigserial primary key,
  silabo_id bigint not null references silabo(id) on delete cascade,
  tipo text null,
  autores text null,
  titulo text null,
  editorial text null,
  anio int null check (anio is null or anio between 1900 and 2100),
  url text null,
  created_at timestamptz not null default now()
);

create table if not exists competencia (
  id bigserial primary key,
  universidad_id bigint not null references universidad(id) on delete cascade,
  tipo text not null check (tipo in ('general','especifica')),
  nombre text not null,
  unique(universidad_id, tipo, nombre)
);

create table if not exists silabo_competencia (
  silabo_id bigint not null references silabo(id) on delete cascade,
  competencia_id bigint not null references competencia(id) on delete cascade,
  primary key (silabo_id, competencia_id)
);

create table if not exists silabo_nota_politica (
  id bigserial primary key,
  silabo_id bigint not null references silabo(id) on delete cascade,
  seccion text not null,
  item_orden int not null,
  texto text not null,
  created_at timestamptz not null default now(),
  unique(silabo_id, seccion, item_orden),
  check (item_orden > 0)
);

create table if not exists silabo_cronograma_sesion (
  id bigserial primary key,
  silabo_id bigint not null references silabo(id) on delete cascade,
  unidad_nro int null,
  semana int null check (semana is null or semana between 1 and 30),
  sesion text null,
  sesion_tipo text null,
  tema text null,
  es_evaluacion boolean not null default false,
  evaluacion_codigo text null,
  created_at timestamptz not null default now(),
  unique(silabo_id, semana, sesion, tema)
);

create table if not exists silabo_cronograma_actividad (
  id bigserial primary key,
  silabo_cronograma_sesion_id bigint not null references silabo_cronograma_sesion(id) on delete cascade,
  orden int not null,
  texto text not null,
  created_at timestamptz not null default now(),
  unique(silabo_cronograma_sesion_id, orden),
  check (orden > 0)
);

create index if not exists idx_silabo_crono_semana on silabo_cronograma_sesion(silabo_id, semana);

-- =========================
-- User domain
-- =========================

create table if not exists usuario (
  id bigserial primary key,
  public_id uuid not null default gen_random_uuid(),
  email text not null unique,
  nombre text null,
  nombre_preferido text null,
  email_institucional text null,
  apellido text null,
  imagen_url text null,
  locale text null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(public_id)
);

create table if not exists auth_identity (
  id bigserial primary key,
  usuario_id bigint not null references usuario(id) on delete cascade,
  provider text not null check (provider in ('google','microsoft','github','email')),
  provider_subject text not null,
  email text null,
  email_verified boolean not null default false,
  display_name text null,
  avatar_url text null,
  provider_data_json jsonb null,
  last_login_at timestamptz null,
  created_at timestamptz not null default now(),
  unique(provider, provider_subject)
);

create table if not exists usuario_periodo (
  id bigserial primary key,
  public_id uuid not null default gen_random_uuid(),
  usuario_id bigint not null references usuario(id) on delete cascade,
  periodo_id bigint not null references periodo(id) on delete restrict,
  campus_id bigint null references campus(id) on delete restrict,
  carrera_id bigint null references carrera(id) on delete restrict,
  ciclo_actual int null check (ciclo_actual is null or ciclo_actual between 1 and 20),
  onboarding_estado text not null default 'pendiente' check (onboarding_estado in ('pendiente','en_progreso','completado')),
  onboarding_completado_at timestamptz null,
  meta_promedio_ciclo numeric(5,2) null check (meta_promedio_ciclo is null or (meta_promedio_ciclo >= 0 and meta_promedio_ciclo <= 20)),
  horas_estudio_semana_objetivo int null check (horas_estudio_semana_objetivo is null or horas_estudio_semana_objetivo >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(usuario_id, periodo_id),
  unique(public_id)
);

create index if not exists idx_usuario_periodo_usuario_id on usuario_periodo(usuario_id, id desc);

create table if not exists usuario_preferencia_estudio (
  id bigserial primary key,
  usuario_periodo_id bigint not null references usuario_periodo(id) on delete cascade,
  dia_semana smallint not null check (dia_semana between 1 and 7),
  hora_inicio time not null,
  hora_fin time not null,
  prioridad smallint not null default 1 check (prioridad between 1 and 5),
  tipo text not null default 'estudio' check (tipo in ('estudio','trabajo')),
  created_at timestamptz not null default now(),
  check (hora_fin > hora_inicio)
);

create table if not exists usuario_onboarding_evento (
  id bigserial primary key,
  usuario_periodo_id bigint not null references usuario_periodo(id) on delete cascade,
  tipo text not null,
  payload_json jsonb null,
  created_at timestamptz not null default now()
);

create table if not exists usuario_periodo_curso (
  id bigserial primary key,
  usuario_periodo_id bigint not null references usuario_periodo(id) on delete cascade,
  silabo_id bigint null references silabo(id) on delete set null,
  curso_id bigint not null references curso(id) on delete restrict,
  estado text not null default 'matriculado' check (estado in ('matriculado','retirado','completado')),
  activo boolean not null default true,
  seccion text null,
  profesor text null,
  modalidad text null,
  origen text not null default 'onboarding' check (origen in ('onboarding','manual','importado')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(usuario_periodo_id, curso_id)
);

create index if not exists idx_upc_usuario_periodo on usuario_periodo_curso(usuario_periodo_id);

create table if not exists usuario_periodo_curso_confianza (
  id bigserial primary key,
  usuario_periodo_curso_id bigint not null references usuario_periodo_curso(id) on delete cascade,
  nivel_confianza smallint not null check (nivel_confianza between 1 and 5),
  comentario text null,
  created_at timestamptz not null default now(),
  unique(usuario_periodo_curso_id)
);

create table if not exists usuario_periodo_curso_horario (
  id bigserial primary key,
  usuario_periodo_curso_id bigint not null references usuario_periodo_curso(id) on delete cascade,
  bloque_nro int not null,
  dia_semana smallint null check (dia_semana is null or dia_semana between 1 and 7),
  hora_inicio time null,
  hora_fin time null,
  duracion_min smallint not null default 45 check (duracion_min > 0),
  tipo_sesion text null,
  ubicacion text null,
  url_virtual text null,
  created_at timestamptz not null default now(),
  unique(usuario_periodo_curso_id, bloque_nro)
);

create table if not exists usuario_periodo_evaluacion (
  id bigserial primary key,
  usuario_periodo_curso_id bigint not null references usuario_periodo_curso(id) on delete cascade,
  silabo_evaluacion_id bigint null references silabo_evaluacion(id) on delete set null,
  codigo text null,
  semana int null check (semana is null or semana between 1 and 30),
  fecha_estimada date null,
  fecha_real date null,
  nota numeric(5,2) null check (nota is null or (nota >= 0 and nota <= 20)),
  exonerado boolean not null default false,
  es_rezagado boolean not null default false,
  reemplaza_a_id bigint null references usuario_periodo_evaluacion(id) on delete set null,
  comentarios text null,
  created_at timestamptz not null default now(),
  unique(usuario_periodo_curso_id, codigo)
);

create table if not exists usuario_historial_curso (
  id bigserial primary key,
  usuario_id bigint not null references usuario(id) on delete cascade,
  carrera_id bigint null references carrera(id) on delete set null,
  curso_id bigint null references curso(id) on delete set null,
  curso_codigo text null,
  curso_nombre text not null,
  periodo_texto text null,
  ciclo_referencial int null check (ciclo_referencial is null or ciclo_referencial between 1 and 20),
  nota_final numeric(5,2) null check (nota_final is null or (nota_final >= 0 and nota_final <= 20)),
  estado text not null default 'aprobado' check (estado in ('aprobado','desaprobado','retirado','convalidado')),
  es_convalidado boolean not null default false,
  es_manual boolean not null default true,
  fuente text not null default 'usuario' check (fuente in ('usuario','importado','sistema')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_uhc_usuario on usuario_historial_curso(usuario_id);

-- =========================
-- Agenda, reminders, telemetry
-- =========================

create table if not exists agenda_evento (
  id bigserial primary key,
  usuario_periodo_id bigint not null references usuario_periodo(id) on delete cascade,
  titulo text not null,
  descripcion text null,
  inicio timestamptz not null,
  fin timestamptz not null,
  fuente text not null check (fuente in ('evaluacion','manual','habito','sistema')),
  ref_id bigint null,
  estado text not null default 'activo' check (estado in ('activo','cancelado','completado')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  check (fin >= inicio)
);

create table if not exists recordatorio_regla (
  id bigserial primary key,
  usuario_periodo_id bigint not null references usuario_periodo(id) on delete cascade,
  tipo text not null check (tipo in ('evaluacion','agenda','habito')),
  anticipacion_dias int not null default 3 check (anticipacion_dias >= 0),
  canal text not null default 'app' check (canal in ('app','email','sms')),
  created_at timestamptz not null default now(),
  unique(usuario_periodo_id, tipo)
);

create table if not exists recordatorio_evento (
  id bigserial primary key,
  usuario_periodo_id bigint not null references usuario_periodo(id) on delete cascade,
  usuario_periodo_evaluacion_id bigint null references usuario_periodo_evaluacion(id) on delete cascade,
  agenda_evento_id bigint null references agenda_evento(id) on delete cascade,
  fecha_envio timestamptz not null,
  canal text not null default 'app' check (canal in ('app','email','sms')),
  estado text not null default 'pendiente' check (estado in ('pendiente','enviado','cancelado')),
  payload_json jsonb null,
  created_at timestamptz not null default now()
);

create table if not exists telemetry_evento (
  id bigserial primary key,
  usuario_id bigint not null references usuario(id) on delete cascade,
  usuario_periodo_id bigint null references usuario_periodo(id) on delete set null,
  nombre text not null,
  evento_ts timestamptz not null default now(),
  data_json jsonb null
);

create index if not exists idx_telemetry_usuario_ts on telemetry_evento(usuario_id, evento_ts);

-- =========================
-- Operations
-- =========================

create table if not exists rol (
  id bigserial primary key,
  nombre text not null unique,
  descripcion text null,
  created_at timestamptz not null default now()
);

create table if not exists usuario_rol (
  usuario_id bigint not null references usuario(id) on delete cascade,
  rol_id bigint not null references rol(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (usuario_id, rol_id)
);

create table if not exists ingestion_run (
  id bigserial primary key,
  fuente text not null default 'pdf',
  trigger_tipo text not null default 'manual' check (trigger_tipo in ('manual','job','api')),
  input_path text null,
  started_at timestamptz not null default now(),
  finished_at timestamptz null,
  estado text not null default 'running' check (estado in ('running','success','partial','failed')),
  total_archivos int not null default 0,
  archivos_ok int not null default 0,
  archivos_error int not null default 0,
  metadata_json jsonb null
);

create table if not exists ingestion_error (
  id bigserial primary key,
  ingestion_run_id bigint not null references ingestion_run(id) on delete cascade,
  archivo text null,
  curso_codigo text null,
  etapa text null,
  mensaje text not null,
  detalle text null,
  created_at timestamptz not null default now()
);

create index if not exists idx_ingestion_error_run on ingestion_error(ingestion_run_id);

create table if not exists calendar_sync_account (
  id bigserial primary key,
  usuario_id bigint not null references usuario(id) on delete cascade,
  provider text not null check (provider in ('microsoft','google')),
  external_account_id text null,
  email text null,
  calendar_id text null,
  sync_direction text not null default 'bidirectional' check (sync_direction in ('read','write','bidirectional')),
  access_token_encrypted text null,
  refresh_token_encrypted text null,
  token_expires_at timestamptz null,
  estado text not null default 'active' check (estado in ('active','revoked','error')),
  last_sync_at timestamptz null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(usuario_id, provider)
);

create table if not exists calendar_sync_log (
  id bigserial primary key,
  calendar_sync_account_id bigint not null references calendar_sync_account(id) on delete cascade,
  usuario_periodo_id bigint null references usuario_periodo(id) on delete set null,
  entidad text not null check (entidad in ('agenda_evento','recordatorio_evento')),
  local_id bigint null,
  agenda_evento_id bigint null references agenda_evento(id) on delete set null,
  recordatorio_evento_id bigint null references recordatorio_evento(id) on delete set null,
  external_id text null,
  accion text not null check (accion in ('create','update','delete')),
  estado text not null check (estado in ('ok','error')),
  mensaje text null,
  synced_at timestamptz not null default now(),
  check (
    (entidad = 'agenda_evento' and agenda_evento_id is not null and recordatorio_evento_id is null)
    or (entidad = 'recordatorio_evento' and recordatorio_evento_id is not null and agenda_evento_id is null)
  )
);

create index if not exists idx_calendar_sync_log_account on calendar_sync_log(calendar_sync_account_id, synced_at);

create table if not exists usuario_tarea (
  id bigserial primary key,
  usuario_periodo_id bigint not null references usuario_periodo(id) on delete cascade,
  usuario_periodo_curso_id bigint null references usuario_periodo_curso(id) on delete set null,
  titulo text not null,
  descripcion text null,
  tipo text not null default 'tarea' check (tipo in ('tarea','entrega','estudio','otro')),
  prioridad text not null default 'media' check (prioridad in ('alta','media','baja')),
  estado text not null default 'pendiente' check (estado in ('pendiente','en_progreso','completada','cancelada')),
  fecha_vencimiento timestamptz null,
  completed_at timestamptz null,
  external_source text null,
  external_id text null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_usuario_tarea_periodo on usuario_tarea(usuario_periodo_id, estado);
create index if not exists idx_usuario_tarea_vencimiento on usuario_tarea(fecha_vencimiento);

create table if not exists usuario_nota_manual (
  id bigserial primary key,
  usuario_periodo_curso_id bigint not null references usuario_periodo_curso(id) on delete cascade,
  etiqueta text not null,
  tipo text null,
  peso numeric(6,2) null check (peso is null or (peso >= 0 and peso <= 100)),
  nota numeric(5,2) null check (nota is null or (nota >= 0 and nota <= 20)),
  semana int null check (semana is null or semana between 1 and 30),
  fecha_registro timestamptz not null default now(),
  fuente text not null default 'manual' check (fuente in ('manual','importado','estimado')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_usuario_nota_manual_upc on usuario_nota_manual(usuario_periodo_curso_id);

-- =========================
-- WhatsApp MVP
-- =========================

create table if not exists whatsapp_link_codes (
  id bigserial primary key,
  user_id bigint not null references usuario(id) on delete cascade,
  code varchar(32) not null,
  status varchar(20) not null,
  expires_at timestamptz not null,
  used_at timestamptz null,
  created_at timestamptz not null
);

create index if not exists idx_whatsapp_link_codes_user_status on whatsapp_link_codes(user_id, status);
create index if not exists idx_whatsapp_link_codes_code on whatsapp_link_codes(code);

create table if not exists user_whatsapp_links (
  id bigserial primary key,
  user_id bigint not null unique references usuario(id) on delete cascade,
  wa_id varchar(64) not null unique,
  phone_number varchar(64) null,
  verified boolean not null default true,
  linked_at timestamptz not null,
  last_interaction_at timestamptz null
);

create table if not exists whatsapp_inbound_message (
  id bigserial primary key,
  meta_message_id varchar(128) not null unique,
  wa_id varchar(64) null,
  received_at timestamptz not null
);
