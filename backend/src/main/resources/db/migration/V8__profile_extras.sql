alter table customer drop constraint customer_status_check;
alter table customer add constraint customer_status_check
    check (status in ('PENDING_VERIFICATION', 'PENDING_PHONE', 'ACTIVE', 'BLOCKED', 'DELETED'));
alter table customer drop constraint ck_customer_has_contact;
alter table customer add constraint ck_customer_has_contact
    check (status = 'DELETED' or email is not null or phone_e164 is not null);

create table customer_avatar (
    customer_id  uuid primary key references customer (id),
    content_type text        not null,
    data         bytea       not null,
    updated_at   timestamptz not null
);

create table coupon (
    id          uuid primary key,
    customer_id uuid        not null references customer (id),
    title       text        not null,
    description text,
    expires_at  timestamptz,
    used_at     timestamptz,
    created_at  timestamptz not null
);
create index ix_coupon_customer on coupon (customer_id);
