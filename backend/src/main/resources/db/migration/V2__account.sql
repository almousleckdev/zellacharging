create table customer (
    id                    uuid primary key,
    first_name            text        not null,
    last_name             text        not null,
    email                 text,
    phone_e164            text,
    email_verified_at     timestamptz,
    phone_verified_at     timestamptz,
    password_hash         text        not null,
    status                text        not null
        check (status in ('PENDING_VERIFICATION', 'PENDING_PHONE', 'ACTIVE', 'BLOCKED')),
    failed_login_attempts integer     not null default 0,
    locked_until          timestamptz,
    created_at            timestamptz not null,
    constraint ck_customer_has_contact check (email is not null or phone_e164 is not null),
    constraint ck_customer_email_lower check (email is null or email = lower(email))
);

create unique index uk_customer_email on customer (email) where email is not null;
create unique index uk_customer_phone on customer (phone_e164) where phone_e164 is not null;

create table refresh_token (
    id          uuid primary key,
    customer_id uuid        not null references customer (id),
    family_id   uuid        not null,
    token_hash  text        not null unique,
    expires_at  timestamptz not null,
    used_at     timestamptz,
    revoked_at  timestamptz
);
create index ix_refresh_token_family on refresh_token (family_id);

create table verification_code (
    id          uuid primary key,
    customer_id uuid        not null references customer (id),
    channel     text        not null check (channel in ('EMAIL', 'PHONE')),
    code_hash   text        not null,
    expires_at  timestamptz not null,
    attempts    integer     not null default 0,
    consumed_at timestamptz,
    created_at  timestamptz not null
);
create index ix_verification_code_lookup on verification_code (customer_id, channel, created_at desc);
