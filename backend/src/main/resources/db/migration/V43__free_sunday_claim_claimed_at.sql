create index free_sunday_claim_open_claimed_at on free_sunday_claim (claimed_at) where state = 'CLAIMED';
