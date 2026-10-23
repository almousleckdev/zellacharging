alter table charging_session add column price_per_kwh_minor bigint
    check (price_per_kwh_minor is null or price_per_kwh_minor > 0);
