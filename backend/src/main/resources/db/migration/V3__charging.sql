create table customer_id_tag (
    customer_id uuid primary key references customer (id),
    id_tag      text        not null unique,
    created_at  timestamptz not null
);

create table charging_session (
    id                uuid primary key,
    customer_id       uuid        not null references customer (id),
    station_id        text        not null,
    connector_id      integer     not null,
    id_tag            text        not null,
    state             text        not null check (state in
        ('REQUESTED', 'START_ACCEPTED', 'ACTIVE', 'STOPPING', 'COMPLETED', 'FAILED', 'ABANDONED')),
    failure_code      text,
    transaction_id    integer     check (transaction_id is null or transaction_id > 0),
    started_at        timestamptz,
    stopped_at        timestamptz,
    energy_kwh        numeric(12, 3),
    cost_minor        bigint,
    currency          char(3),
    currency_decimals smallint,
    stop_reason       text,
    created_at        timestamptz not null,
    updated_at        timestamptz not null
);

create unique index uk_session_live_customer on charging_session (customer_id)
    where state in ('REQUESTED', 'START_ACCEPTED', 'ACTIVE', 'STOPPING');
create unique index uk_session_live_connector on charging_session (station_id, connector_id)
    where state in ('REQUESTED', 'START_ACCEPTED', 'ACTIVE', 'STOPPING');
create index ix_session_customer_created on charging_session (customer_id, created_at desc);
