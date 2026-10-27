alter table topup add column reference text;
update topup set reference = country_code || replace(id::text, '-', '');
alter table topup alter column reference set not null;
create unique index uk_topup_reference on topup (reference);

alter table topup drop constraint topup_state_check;
alter table topup add constraint topup_state_check
    check (state in ('PENDING', 'SUCCEEDED', 'FAILED', 'EXPIRED'));

create index ix_topup_unsettled on topup (created_at) where state in ('PENDING', 'EXPIRED');
