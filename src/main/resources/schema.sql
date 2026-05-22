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