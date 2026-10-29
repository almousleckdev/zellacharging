insert into admin_permission (admin_id, permission)
select a.id, 'CUSTOMERS_MANAGE'
from admin_user a
where a.role = 'ADMIN'
on conflict do nothing;
