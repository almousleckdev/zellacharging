create table incident_review (
    country_code char(2)     not null references country (code),
    incident_key text        not null,
    status       text        not null check (status in ('ACKNOWLEDGED', 'RESOLVED')),
    note         text,
    admin_id     uuid        not null references admin_user (id),
    updated_at   timestamptz not null,
    primary key (country_code, incident_key)
);
