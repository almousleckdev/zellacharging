create table admin_user (
    id                  uuid        primary key,
    email               text        not null,
    password_hash       text        not null,
    role                text        not null check (role in ('SUPER_ADMIN', 'COUNTRY_ADMIN')),
    status              text        not null check (status in ('ACTIVE', 'DISABLED')),
    totp_secret         text,
    totp_pending_secret text,
    totp_last_step      bigint,
    failed_attempts     int         not null default 0,
    locked_until        timestamptz,
    created_at          timestamptz not null
);
create unique index uk_admin_user_email on admin_user (lower(email));

create table admin_country_grant (
    admin_id     uuid    not null references admin_user (id) on delete cascade,
    country_code char(2) not null references country (code),
    primary key (admin_id, country_code)
);

create table admin_refresh_token (
    id         uuid        primary key,
    admin_id   uuid        not null references admin_user (id) on delete cascade,
    family_id  uuid        not null,
    token_hash text        not null unique,
    expires_at timestamptz not null,
    used_at    timestamptz,
    revoked_at timestamptz
);
create index ix_admin_refresh_token_admin on admin_refresh_token (admin_id);

create table admin_audit_record (
    id             uuid        primary key,
    admin_id       uuid        not null references admin_user (id),
    country_code   char(2),
    action         text        not null,
    subject        text,
    detail         text,
    correlation_id text,
    created_at     timestamptz not null
);
create index ix_admin_audit_created on admin_audit_record (created_at desc);
create index ix_admin_audit_admin on admin_audit_record (admin_id, created_at desc);

revoke update, delete on admin_audit_record from zella_app;
