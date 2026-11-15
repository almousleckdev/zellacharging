insert into platform_setting (key, value, updated_by, updated_at)
values ('charging.serviceFeePercent', '0', null, now())
on conflict (key) do update set value = '0', updated_at = now();
