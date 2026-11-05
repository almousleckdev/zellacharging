create table partner_api_key (
    id           uuid        primary key,
    partner      text        not null check (partner in ('NDZELA')),
    country_code char(2)     not null,
    key_hash     text        not null unique,
    created_at   timestamptz not null,
    expires_at   timestamptz,
    revoked_at   timestamptz,
    last_used_at timestamptz
);

create table partner_link (
    partner      text        not null check (partner in ('NDZELA')),
    customer_id  uuid        not null,
    country_code char(2)     not null default current_setting('app.country'),
    linked_at    timestamptz not null,
    primary key (partner, customer_id),
    foreign key (customer_id, country_code) references customer (id, country_code)
);

alter table partner_link enable row level security;
create policy tenant_isolation on partner_link
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));
