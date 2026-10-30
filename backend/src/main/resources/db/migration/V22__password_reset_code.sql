alter table verification_code drop constraint verification_code_channel_check;
alter table verification_code add constraint verification_code_channel_check
    check (channel in ('EMAIL', 'PHONE', 'PASSWORD_RESET'));
