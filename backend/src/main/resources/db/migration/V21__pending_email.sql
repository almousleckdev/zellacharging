create table pending_email (
    customer_id  uuid        primary key references customer (id),
    country_code char(2)     not null default current_setting('app.country') references country (code),
    email        text        not null,
    created_at   timestamptz not null
);

alter table pending_email enable row level security;
create policy tenant_isolation on pending_email
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));
