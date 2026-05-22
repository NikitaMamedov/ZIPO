create table if not exists licenses (
    id bigserial primary key,
    license_key varchar(100) not null unique,
    user_id bigint not null,
    device_id varchar(100),
    status varchar(20) not null default 'CREATED',
    created_at timestamp not null default now(),
    activated_at timestamp,
    expires_at timestamp not null,
    max_devices int not null default 1,
    description varchar(255)
);

create table if not exists license_history (
    id bigserial primary key,
    license_id bigint not null references licenses(id),
    action varchar(50) not null,
    device_id varchar(100),
    ip_address varchar(50),
    user_agent varchar(255),
    created_at timestamp not null default now()
);

create table if not exists signatures (
    id uuid primary key default gen_random_uuid(),
    threat_name varchar(255) not null,
    first_bytes_hex varchar(512) not null,
    remainder_hash_hex varchar(512) not null,
    remainder_length bigint not null default 0,
    file_type varchar(100) not null,
    offset_start bigint not null default 0,
    offset_end bigint not null default 0,
    updated_at timestamp not null,
    status varchar(20) not null default 'ACTUAL',
    digital_signature_base64 text
);

create table if not exists signatures_history (
    history_id bigserial primary key,
    signature_id uuid not null,
    version_created_at timestamp not null,
    threat_name varchar(255),
    first_bytes_hex varchar(512),
    remainder_hash_hex varchar(512),
    remainder_length bigint,
    file_type varchar(100),
    offset_start bigint,
    offset_end bigint,
    updated_at timestamp,
    status varchar(20),
    digital_signature_base64 text
);

create table if not exists signatures_audit (
    audit_id bigserial primary key,
    signature_id uuid not null,
    changed_by varchar(100),
    changed_at timestamp not null,
    fields_changed text,
    description text
);