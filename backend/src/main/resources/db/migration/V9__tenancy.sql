create table country (
    code              char(2)  primary key,
    name              text     not null,
    currency          char(3)  not null,
    currency_decimals smallint not null,
    phone_region      char(2)  not null unique,
    active            boolean  not null
);

insert into country (code, name, currency, currency_decimals, phone_region, active) values
    ('GA', 'Gabon', 'XAF', 0, 'GA', true),
    ('ML', 'Mali', 'XOF', 0, 'ML', false);

alter table customer add column country_code char(2);
update customer set country_code = 'GA';
alter table customer alter column country_code set not null;
alter table customer alter column country_code set default current_setting('app.country');
alter table customer add constraint fk_customer_country foreign key (country_code) references country (code);
alter table customer add constraint uk_customer_id_country unique (id, country_code);

drop index uk_customer_email;
drop index uk_customer_phone;
create unique index uk_customer_email on customer (country_code, email) where email is not null;
create unique index uk_customer_phone on customer (country_code, phone_e164) where phone_e164 is not null;

do $$
declare
    owned text;
    tenant text;
begin
    foreach owned in array array['refresh_token', 'verification_code', 'customer_id_tag', 'charging_session',
        'wallet', 'topup', 'funding_hold', 'ledger_entry', 'customer_avatar', 'coupon'] loop
        execute format('alter table %I add column country_code char(2)', owned);
        execute format('update %I t set country_code = c.country_code from customer c where c.id = t.customer_id',
            owned);
        execute format('alter table %I alter column country_code set not null', owned);
        execute format('alter table %I alter column country_code set default current_setting(''app.country'')',
            owned);
        execute format('alter table %I add constraint %I foreign key (customer_id, country_code) '
            || 'references customer (id, country_code)', owned, 'fk_' || owned || '_customer_country');
    end loop;

    foreach tenant in array array['customer', 'refresh_token', 'verification_code', 'customer_id_tag',
        'charging_session', 'wallet', 'topup', 'funding_hold', 'ledger_entry', 'customer_avatar', 'coupon'] loop
        execute format('alter table %I enable row level security', tenant);
        execute format('create policy tenant_isolation on %I using (country_code = current_setting(''app.country'', true)) '
            || 'with check (country_code = current_setting(''app.country'', true))', tenant);
    end loop;
end $$;

do $$
begin
    if not exists (select from pg_roles where rolname = 'zella_app') then
        create role zella_app nologin;
    end if;
end $$;

grant usage on schema public to zella_app;
grant select, insert, update, delete on all tables in schema public to zella_app;
revoke update, delete on ledger_entry from zella_app;
revoke all on flyway_schema_history from zella_app;
alter default privileges in schema public grant select, insert, update, delete on tables to zella_app;
