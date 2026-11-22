create table free_sunday_claim (
    id             uuid        primary key,
    country_code   char(2)     not null default current_setting('app.country'),
    customer_id    uuid        not null,
    sunday_date    date        not null,
    session_id     uuid        not null,
    agreement_id   uuid        not null,
    window_ends_at timestamptz not null,
    state          text        not null default 'CLAIMED' check (state in ('CLAIMED', 'RELEASED')),
    claimed_at     timestamptz not null default now(),
    released_at    timestamptz,
    foreign key (customer_id, country_code) references customer (id, country_code) on delete cascade
);

create unique index free_sunday_claim_one_per_sunday
    on free_sunday_claim (customer_id, sunday_date) where state = 'CLAIMED';
create index free_sunday_claim_session on free_sunday_claim (session_id);
create index free_sunday_claim_open_window on free_sunday_claim (window_ends_at) where state = 'CLAIMED';

alter table free_sunday_claim enable row level security;
create policy tenant_isolation on free_sunday_claim
    using (country_code = current_setting('app.country', true))
    with check (country_code = current_setting('app.country', true));
