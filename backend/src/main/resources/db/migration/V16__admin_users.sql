alter table admin_user add column first_name text;
alter table admin_user add column last_name text;
alter table admin_user add column phone_e164 text;
alter table admin_user add column last_login_at timestamptz;
alter table admin_user add column invitation_hash text;
alter table admin_user add column invitation_expires_at timestamptz;
alter table admin_user add column invited_by uuid references admin_user (id);
alter table admin_user alter column password_hash drop not null;

alter table admin_user drop constraint admin_user_status_check;
alter table admin_user add constraint admin_user_status_check check (status in ('INVITED', 'ACTIVE', 'DISABLED'));
create unique index uk_admin_invitation on admin_user (invitation_hash) where invitation_hash is not null;

create table admin_permission (
    admin_id   uuid not null references admin_user (id) on delete cascade,
    permission text not null,
    primary key (admin_id, permission)
);

create table admin_avatar (
    admin_id     uuid primary key references admin_user (id) on delete cascade,
    content_type text        not null,
    data         bytea       not null,
    updated_at   timestamptz not null
);

insert into admin_permission (admin_id, permission)
select a.id, p.permission
from admin_user a
         cross join (values ('OVERVIEW_READ'), ('STATIONS_READ'), ('SESSIONS_READ'), ('CUSTOMERS_READ'),
                            ('PAYMENTS_READ'), ('AUDIT_READ')) as p (permission)
where a.role = 'ADMIN';
