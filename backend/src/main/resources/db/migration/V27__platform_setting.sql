create table platform_setting (
    key        text primary key,
    value      text        not null,
    updated_by uuid,
    updated_at timestamptz not null
);
