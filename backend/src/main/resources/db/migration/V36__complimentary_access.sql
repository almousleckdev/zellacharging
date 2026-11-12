create table customer_complimentary (
    customer_id  uuid        primary key,
    country_code char(2)     not null default current_setting('app.country'),
    granted_by   uuid        not null,
    reason       text        not null,
    granted_at   timestamptz not null,
    expires_at   timestamptz,
    foreign key (customer_id, country_code) references customer (id, country_code) on delete cascade
);

alter table customer_complimentary enable row level security;
create policy tenant_isolation on customer_complimentary
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));

alter table funding_hold add column waived boolean not null default false;
