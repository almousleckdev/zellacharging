alter table customer_complimentary add column category varchar(20) not null default 'OTHER';
alter table customer_complimentary add constraint customer_complimentary_category_check
    check (category in ('NDZELA_STAFF', 'OTHER'));
