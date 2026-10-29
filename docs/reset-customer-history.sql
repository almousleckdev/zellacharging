\set ON_ERROR_STOP on
\if :{?keep}
\else
\echo 'Usage: psql -v keep="{a@mail.com}" -v keep_phones="{+241...}" -f docs/reset-customer-history.sql'
\quit
\endif
\if :{?keep_phones}
\else
\set keep_phones '{}'
\endif

begin;

create temporary table targets on commit drop as
select id from customer
where coalesce(email, '') = any (:'keep'::text[]) or coalesce(phone_e164, '') = any (:'keep_phones'::text[]);

select count(*) as customers_reset from targets;

do $$
declare
    removed bigint;
begin
    delete from ledger_hidden where customer_id in (select id from targets);
    get diagnostics removed = row_count;
    raise notice 'ledger_hidden : % rows', removed;
    delete from ledger_entry where customer_id in (select id from targets);
    get diagnostics removed = row_count;
    raise notice 'ledger_entry : % rows', removed;
    delete from funding_hold where customer_id in (select id from targets);
    get diagnostics removed = row_count;
    raise notice 'funding_hold : % rows', removed;
    delete from charging_session where customer_id in (select id from targets);
    get diagnostics removed = row_count;
    raise notice 'charging_session : % rows', removed;
    delete from topup where customer_id in (select id from targets);
    get diagnostics removed = row_count;
    raise notice 'topup : % rows', removed;
    delete from withdrawal where customer_id in (select id from targets);
    get diagnostics removed = row_count;
    raise notice 'withdrawal : % rows', removed;
    delete from incident_review;
    update wallet set balance_minor = 0, reserved_minor = 0 where customer_id in (select id from targets);
end $$;

select 'customer' as table_name, count(*) as remaining from customer
union all select 'topup', count(*) from topup
union all select 'charging_session', count(*) from charging_session
union all select 'ledger_entry', count(*) from ledger_entry;

select coalesce(sum(balance_minor), 0) as wallet_money_left_minor from wallet;

rollback;
