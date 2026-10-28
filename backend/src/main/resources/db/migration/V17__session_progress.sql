alter table charging_session add column last_energy_kwh numeric(12, 3);
alter table charging_session add column progress_at timestamptz;
