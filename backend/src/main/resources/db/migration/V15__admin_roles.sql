alter table admin_user drop constraint admin_user_role_check;
update admin_user set role = 'ADMIN' where role = 'COUNTRY_ADMIN';
alter table admin_user add constraint admin_user_role_check
    check (role in ('SUPER_ADMIN', 'ADMIN', 'SUPPORT', 'FINANCE', 'OPERATIONS'));
