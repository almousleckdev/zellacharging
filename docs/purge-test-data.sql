\set ON_ERROR_STOP on
\if :{?cutoff}
\else
\echo 'Usage: psql "$DATABASE_URL" -v cutoff="2026-10-28 12:00:00+00" -v keep="{real1@mail.com,real2@mail.com}" -f docs/purge-test-data.sql'
\quit
\endif
\if :{?keep}
\else
\set keep '{}'
\endif
\if :{?keep_phones}
\else
\set keep_phones '{}'
\endif

begin;

create temporary table doomed on commit drop as
select id from customer
where created_at < :'cutoff'::timestamptz
  and coalesce(email, '') <> all (:'keep'::text[])
  and coalesce(phone_e164, '') <> all (:'keep_phones'::text[]);

select count(*) as customers_to_delete from doomed;
select count(*) as customers_kept from customer where id not in (select id from doomed);

do $$
declare
    related record;
    removed bigint;
begin
    for related in
        select table_name
        from information_schema.columns
        where table_schema = 'public' and column_name = 'customer_id' and table_name <> 'customer'
        order by table_name
    loop
        execute format('delete from %I where customer_id in (select id from doomed)', related.table_name);
        get diagnostics removed = row_count;
        raise notice '% : % rows', related.table_name, removed;
    end loop;
    delete from audit_record where actor_id in (select id from doomed);
    get diagnostics removed = row_count;
    raise notice 'audit_record : % rows', removed;
    delete from incident_review
    where split_part(incident_key, ':', 2)::uuid not in (
        select id from charging_session union select id from topup union select id from withdrawal);
    get diagnostics removed = row_count;
    raise notice 'incident_review : % rows', removed;
    delete from customer where id in (select id from doomed);
    get diagnostics removed = row_count;
    raise notice 'customer : % rows', removed;
end $$;

select 'customer' as table_name, count(*) as remaining from customer
union all select 'wallet', count(*) from wallet
union all select 'topup', count(*) from topup
union all select 'withdrawal', count(*) from withdrawal
union all select 'charging_session', count(*) from charging_session
union all select 'ledger_entry', count(*) from ledger_entry;

select coalesce(sum(balance_minor), 0) as wallet_money_left_minor from wallet;

rollback;
