create table customer_identity (
    id           uuid        primary key,
    customer_id  uuid        not null,
    country_code char(2)     not null default current_setting('app.country'),
    provider     text        not null check (provider in ('GOOGLE', 'APPLE')),
    subject      text        not null,
    created_at   timestamptz not null,
    unique (provider, subject),
    foreign key (customer_id, country_code) references customer (id, country_code)
);
create index ix_customer_identity_customer on customer_identity (customer_id);

alter table customer add column password_login boolean not null default true;

alter table customer_identity enable row level security;
create policy tenant_isolation on customer_identity
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));
