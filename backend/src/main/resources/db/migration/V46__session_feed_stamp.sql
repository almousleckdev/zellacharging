alter table charging_session add column feed_at timestamptz;

update charging_session s
set feed_at = case
    when s.created_at >= now() - interval '45 days' then clock_timestamp()
    else greatest(s.updated_at, coalesce((select c.updated_at from session_charge c where c.session_id = s.id),
                                         s.updated_at))
    end;

alter table charging_session
    alter column feed_at set default clock_timestamp(),
    alter column feed_at set not null;

create index charging_session_feed on charging_session (country_code, feed_at, id);

create function stamp_session_feed() returns trigger language plpgsql as
$$
begin
    update charging_session set feed_at = clock_timestamp() where id = new.id;
    return null;
end
$$;

create function stamp_related_session_feed() returns trigger language plpgsql as
$$
begin
    update charging_session set feed_at = clock_timestamp() where id = new.session_id;
    return null;
end
$$;

create constraint trigger charging_session_feed_stamp
    after update on charging_session
    deferrable initially deferred
    for each row
    when ((to_jsonb(old) - 'feed_at') is distinct from (to_jsonb(new) - 'feed_at'))
execute function stamp_session_feed();

create constraint trigger charging_session_feed_stamp_new
    after insert on charging_session
    deferrable initially deferred
    for each row
execute function stamp_session_feed();

create constraint trigger session_charge_feed_stamp
    after insert or update on session_charge
    deferrable initially deferred
    for each row
execute function stamp_related_session_feed();

create constraint trigger funding_hold_feed_stamp
    after insert or update on funding_hold
    deferrable initially deferred
    for each row
execute function stamp_related_session_feed();

create constraint trigger free_sunday_claim_feed_stamp
    after insert or update on free_sunday_claim
    deferrable initially deferred
    for each row
execute function stamp_related_session_feed();
