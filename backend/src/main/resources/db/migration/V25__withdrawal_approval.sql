alter table withdrawal drop constraint withdrawal_state_check;
alter table withdrawal add constraint withdrawal_state_check
    check (state in ('PENDING_APPROVAL', 'REQUESTED', 'PROCESSING', 'PAID', 'FAILED', 'REVIEW'));
alter table withdrawal add column reviewed_by uuid;
alter table withdrawal add column reviewed_at timestamptz;
alter table withdrawal add column review_note text;
create index ix_withdrawal_pending_approval on withdrawal (created_at) where state = 'PENDING_APPROVAL';

insert into admin_permission (admin_id, permission)
select a.id, 'WITHDRAWALS_REVIEW'
from admin_user a
where a.role in ('ADMIN', 'SUPER_ADMIN')
on conflict do nothing;
