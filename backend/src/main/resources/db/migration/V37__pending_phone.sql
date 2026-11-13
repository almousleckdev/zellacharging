create table pending_phone (
    customer_id  uuid        primary key references customer (id),
    country_code char(2)     not null default current_setting('app.country') references country (code),
    phone_e164   text        not null,
    created_at   timestamptz not null
);

alter table pending_phone enable row level security;
create policy tenant_isolation on pending_phone
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));
