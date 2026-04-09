create table if not exists whatsapp_link_codes (
    id bigserial primary key,
    user_id bigint not null references usuario(id),
    code varchar(32) not null,
    status varchar(20) not null,
    expires_at timestamptz not null,
    used_at timestamptz null,
    created_at timestamptz not null
);

create index if not exists idx_whatsapp_link_codes_user_status
    on whatsapp_link_codes(user_id, status);

create index if not exists idx_whatsapp_link_codes_code
    on whatsapp_link_codes(code);

create table if not exists user_whatsapp_links (
    id bigserial primary key,
    user_id bigint not null unique references usuario(id),
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
