insert into admin_permission (admin_id, permission)
select a.id, 'FREE_CHARGING_READ'
from admin_user a
where a.role in ('ADMIN', 'SUPPORT', 'FINANCE', 'OPERATIONS')
on conflict do nothing;

insert into admin_permission (admin_id, permission)
select a.id, 'FREE_CHARGING_MANAGE'
from admin_user a
where a.role = 'ADMIN'
on conflict do nothing;
