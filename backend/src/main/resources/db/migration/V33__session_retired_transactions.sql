alter table charging_session add column retired_transactions integer[] not null default '{}';
