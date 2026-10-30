alter table notification_outbox add column read_at timestamptz;
create index ix_notification_outbox_inbox on notification_outbox (customer_id, created_at desc);
create index ix_notification_outbox_unread on notification_outbox (customer_id) where read_at is null;
