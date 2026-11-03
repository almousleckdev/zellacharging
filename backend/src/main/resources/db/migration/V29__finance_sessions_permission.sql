insert into admin_permission (admin_id, permission)
select a.id, 'SESSIONS_READ'
from admin_user a
where a.role = 'FINANCE'
on conflict do nothing;
