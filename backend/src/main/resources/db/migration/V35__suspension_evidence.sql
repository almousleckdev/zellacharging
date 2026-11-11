create table suspension_evidence (
    seq                   bigint generated always as identity,
    id                    uuid        primary key,
    partner               text        not null,
    country_code          char(2)     not null default current_setting('app.country'),
    suspended_customer_id uuid        not null,
    receiving_customer_id uuid        not null,
    receiving_ref         text        not null,
    receiving_linked      boolean     not null,
    kind                  text        not null,
    msisdn                text        not null,
    amount_minor          bigint      not null,
    currency              char(3)     not null,
    idempotency_key       text        not null,
    occurred_at           timestamptz not null,
    unique (partner, suspended_customer_id, receiving_customer_id, idempotency_key),
    foreign key (suspended_customer_id, country_code) references customer (id, country_code) on delete cascade
);

create unique index uk_suspension_evidence_seq on suspension_evidence (seq);

alter table suspension_evidence enable row level security;
create policy tenant_isolation on suspension_evidence
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));
