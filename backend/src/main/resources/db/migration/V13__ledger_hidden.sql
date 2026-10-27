create table ledger_hidden (
    entry_id     uuid        primary key references ledger_entry (id),
    customer_id  uuid        not null,
    country_code char(2)     not null default current_setting('app.country'),
    hidden_at    timestamptz not null,
    constraint fk_ledger_hidden_customer_country foreign key (customer_id, country_code)
        references customer (id, country_code)
);

alter table ledger_hidden enable row level security;
create policy tenant_isolation on ledger_hidden
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));
