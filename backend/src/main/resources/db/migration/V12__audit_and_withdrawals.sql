create table audit_record (
    id             uuid        primary key,
    country_code   char(2)     not null default current_setting('app.country') references country (code),
    actor_id       uuid,
    action         text        not null,
    subject        text,
    detail         text,
    correlation_id text,
    created_at     timestamptz not null
);
create index ix_audit_record_created on audit_record (created_at desc);
create index ix_audit_record_actor on audit_record (actor_id, created_at desc);

alter table ledger_entry drop constraint ledger_entry_type_check;
alter table ledger_entry add constraint ledger_entry_type_check
    check (type in ('TOPUP', 'RESERVE', 'RELEASE', 'CAPTURE', 'WITHDRAWAL', 'WITHDRAWAL_REVERSAL'));

create table withdrawal (
    id              uuid        primary key,
    customer_id     uuid        not null references customer (id),
    country_code    char(2)     not null default current_setting('app.country'),
    operator        text        not null,
    msisdn          text        not null,
    amount_minor    bigint      not null check (amount_minor > 0),
    state           text        not null check (state in ('REQUESTED', 'PROCESSING', 'PAID', 'FAILED', 'REVIEW')),
    reference       text        not null unique,
    provider_ref    text,
    failure_code    text,
    idempotency_key text        not null,
    created_at      timestamptz not null,
    updated_at      timestamptz not null,
    unique (customer_id, idempotency_key),
    foreign key (customer_id, country_code) references customer (id, country_code)
);
create index ix_withdrawal_unsettled on withdrawal (created_at) where state in ('REQUESTED', 'PROCESSING');
create index ix_withdrawal_customer on withdrawal (customer_id, created_at desc);

do $$
declare
    tenant text;
begin
    foreach tenant in array array['audit_record', 'withdrawal'] loop
        execute format('alter table %I enable row level security', tenant);
        execute format('create policy tenant_isolation on %I using (country_code = current_setting(''app.country'', true)) '
            || 'with check (country_code = current_setting(''app.country'', true))', tenant);
    end loop;
end $$;

revoke update, delete on audit_record from zella_app;
