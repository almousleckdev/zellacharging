alter table charging_session add column idempotency_key text;
create unique index uk_session_idempotency on charging_session (customer_id, idempotency_key)
    where idempotency_key is not null;
