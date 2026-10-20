alter table charging_session add column battery_percent numeric(5, 2)
    check (battery_percent is null or (battery_percent > 0 and battery_percent <= 100));
