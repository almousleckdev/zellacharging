alter table charging_session add column hidden_at timestamptz;
alter table charging_session add column last_voltage numeric(7, 2);
alter table charging_session add column last_power_kw numeric(8, 3);
