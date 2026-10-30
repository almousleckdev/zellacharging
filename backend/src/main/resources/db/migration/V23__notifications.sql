create table device_token (
    token        text        primary key,
    customer_id  uuid        not null,
    country_code char(2)     not null default current_setting('app.country'),
    platform     text        not null check (platform in ('ANDROID', 'IOS')),
    language     text        not null default 'fr',
    created_at   timestamptz not null,
    last_seen_at timestamptz not null,
    foreign key (customer_id, country_code) references customer (id, country_code)
);
create index ix_device_token_customer on device_token (customer_id);

create table notification_outbox (
    id                uuid        primary key,
    customer_id       uuid        not null,
    country_code      char(2)     not null default current_setting('app.country'),
    type              text        not null,
    params            text        not null,
    dedupe_key        text        not null,
    state             text        not null check (state in ('PENDING', 'SENT', 'DEAD')),
    attempts          int         not null default 0,
    next_attempt_at   timestamptz not null,
    delivered_devices int         not null default 0,
    created_at        timestamptz not null,
    sent_at           timestamptz,
    unique (customer_id, dedupe_key),
    foreign key (customer_id, country_code) references customer (id, country_code)
);
create index ix_notification_outbox_due on notification_outbox (next_attempt_at) where state = 'PENDING';

do $$
declare
    tenant text;
begin
    foreach tenant in array array['device_token', 'notification_outbox'] loop
        execute format('alter table %I enable row level security', tenant);
        execute format('create policy tenant_isolation on %I using (country_code = current_setting(''app.country'', true)) '
            || 'with check (country_code = current_setting(''app.country'', true))', tenant);
    end loop;
end $$;
