alter table charging_session add column start_battery_percent numeric(5, 2)
    check (start_battery_percent is null or (start_battery_percent > 0 and start_battery_percent <= 100));
