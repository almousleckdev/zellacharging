insert into admin_permission (admin_id, permission)
select a.id, 'SESSIONS_STOP'
from admin_user a
where a.role in ('ADMIN', 'OPERATIONS')
on conflict do nothing;
