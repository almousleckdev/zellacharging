create table wallet (
    customer_id    uuid primary key references customer (id),
    currency       char(3)     not null,
    decimals       smallint    not null,
    balance_minor  bigint      not null default 0,
    reserved_minor bigint      not null default 0,
    created_at     timestamptz not null,
    check (balance_minor >= 0 and reserved_minor >= 0 and reserved_minor <= balance_minor)
);

create table topup (
    id              uuid primary key,
    customer_id     uuid        not null references customer (id),
    operator        text        not null check (operator in ('AIRTEL', 'MOOV')),
    msisdn          text        not null,
    amount_minor    bigint      not null check (amount_minor > 0),
    state           text        not null check (state in ('PENDING', 'SUCCEEDED', 'FAILED')),
    provider_ref    text unique,
    failure_code    text,
    idempotency_key text        not null,
    created_at      timestamptz not null,
    updated_at      timestamptz not null,
    unique (customer_id, idempotency_key)
);

create table funding_hold (
    session_id     uuid primary key,
    customer_id    uuid        not null references customer (id),
    reserved_minor bigint      not null check (reserved_minor > 0),
    captured_minor bigint,
    state          text        not null check (state in ('OPEN', 'SETTLED', 'RELEASED', 'REVIEW')),
    created_at     timestamptz not null,
    closed_at      timestamptz
);

create index ix_funding_hold_open on funding_hold (created_at) where state = 'OPEN';

create table ledger_entry (
    id              uuid primary key,
    customer_id     uuid        not null references customer (id),
    type            text        not null check (type in ('TOPUP', 'RESERVE', 'RELEASE', 'CAPTURE')),
    amount_minor    bigint      not null check (amount_minor >= 0),
    session_id      uuid,
    topup_id        uuid,
    idempotency_key text        not null unique,
    created_at      timestamptz not null
);

create index ix_ledger_customer_created on ledger_entry (customer_id, created_at desc);
