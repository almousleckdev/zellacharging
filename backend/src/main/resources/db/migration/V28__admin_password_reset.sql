alter table admin_user add column password_reset_hash text;
alter table admin_user add column password_reset_expires_at timestamptz;
create unique index uk_admin_password_reset on admin_user (password_reset_hash) where password_reset_hash is not null;
