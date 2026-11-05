create table session_charge (
    session_id        uuid        primary key,
    customer_id       uuid        not null,
    country_code      char(2)     not null default current_setting('app.country'),
    state             text        not null check (state in ('CHARGED', 'NOT_CHARGED', 'UNDER_REVIEW', 'REFUNDED')),
    energy_cost_minor bigint,
    vat_minor         bigint,
    service_fee_minor bigint,
    total_minor       bigint,
    currency          char(3),
    decimals          smallint,
    estimated         boolean     not null default false,
    updated_at        timestamptz not null,
    foreign key (customer_id, country_code) references customer (id, country_code)
);

create index ix_session_charge_updated on session_charge (updated_at, session_id);

alter table session_charge enable row level security;
create policy tenant_isolation on session_charge
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));
