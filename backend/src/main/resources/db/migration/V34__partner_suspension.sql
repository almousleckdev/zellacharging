create table partner_suspension (
    partner        text        not null check (partner in ('NDZELA')),
    customer_id    uuid        not null,
    country_code   char(2)     not null default current_setting('app.country'),
    owed_minor     bigint      not null check (owed_minor >= 0),
    currency       char(3)     not null,
    decimals       smallint    not null,
    blocked_before boolean     not null,
    suspended_at   timestamptz not null,
    restored_at    timestamptz,
    primary key (partner, customer_id),
    foreign key (customer_id, country_code) references customer (id, country_code) on delete cascade
);

create table suspended_number (
    partner      text    not null,
    customer_id  uuid    not null,
    country_code char(2) not null default current_setting('app.country'),
    msisdn       text    not null,
    primary key (partner, customer_id, msisdn),
    foreign key (partner, customer_id) references partner_suspension (partner, customer_id) on delete cascade
);

create index ix_suspended_number_msisdn on suspended_number (country_code, msisdn);

alter table partner_suspension enable row level security;
create policy tenant_isolation on partner_suspension
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));

alter table suspended_number enable row level security;
create policy tenant_isolation on suspended_number
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));
