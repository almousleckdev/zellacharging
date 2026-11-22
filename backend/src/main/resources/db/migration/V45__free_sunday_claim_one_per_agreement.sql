create unique index free_sunday_claim_one_per_agreement_sunday
    on free_sunday_claim (agreement_id, sunday_date) where state = 'CLAIMED';
