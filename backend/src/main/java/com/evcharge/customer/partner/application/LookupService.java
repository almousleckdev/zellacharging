package com.evcharge.customer.partner.application;

import com.evcharge.customer.account.CustomerContacts;
import com.evcharge.customer.account.CustomerMatch;
import com.evcharge.customer.charging.CustomerIdTags;
import com.evcharge.customer.partner.domain.MatchQuality;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LookupService {

    private final DriverQueryValidator validator;
    private final CustomerContacts contacts;
    private final CustomerIdTags idTags;
    private final AuditTrail audit;

    LookupService(DriverQueryValidator validator, CustomerContacts contacts, CustomerIdTags idTags,
                  AuditTrail audit) {
        this.validator = validator;
        this.contacts = contacts;
        this.idTags = idTags;
        this.audit = audit;
    }

    public List<LookupResult> lookup(PartnerPrincipal principal, List<DriverQuery> queries) {
        validator.require(queries);
        List<CustomerMatch> found = contacts.activeByEmailOrPhone(
                queries.stream().map(DriverQuery::email).distinct().toList(),
                queries.stream().map(DriverQuery::phoneE164).distinct().toList());
        Map<String, CustomerMatch> byEmail = index(found, true);
        Map<String, CustomerMatch> byPhone = index(found, false);
        Map<UUID, String> tags = idTags.existing(found.stream().map(CustomerMatch::id).toList());
        List<LookupResult> results = queries.stream().map(query -> new LookupResult(query.ref(),
                matchesOf(byEmail.get(query.email()), byPhone.get(query.phoneE164()), tags))).toList();
        record(principal, queries.size(), results);
        return results;
    }

    private static List<LookupMatch> matchesOf(CustomerMatch email, CustomerMatch phone, Map<UUID, String> tags) {
        List<LookupMatch> matches = new ArrayList<>();
        if (email != null && phone != null && email.id().equals(phone.id())) {
            matches.add(new LookupMatch(email.id(), tags.get(email.id()), List.of("email", "phone"),
                    MatchQuality.FULL, email.firstName(), email.lastName()));
            return matches;
        }
        if (email != null && email.emailProvenByProvider()) {
            matches.add(new LookupMatch(email.id(), tags.get(email.id()), List.of("email"), MatchQuality.FULL,
                    email.firstName(), email.lastName()));
        } else if (email != null) {
            matches.add(partial(email, "email", tags));
        }
        if (phone != null) {
            matches.add(partial(phone, "phone", tags));
        }
        return matches;
    }

    private static LookupMatch partial(CustomerMatch customer, String field, Map<UUID, String> tags) {
        return new LookupMatch(customer.id(), tags.get(customer.id()), List.of(field), MatchQuality.PARTIAL, null,
                null);
    }

    private static Map<String, CustomerMatch> index(List<CustomerMatch> customers, boolean byEmail) {
        Map<String, CustomerMatch> index = new HashMap<>();
        customers.forEach(customer -> {
            String key = byEmail ? customer.email() : customer.phoneE164();
            if (key != null) {
                index.put(key, customer);
            }
        });
        return index;
    }

    private void record(PartnerPrincipal principal, int entries, List<LookupResult> results) {
        long full = results.stream().flatMap(result -> result.matches().stream())
                .filter(match -> match.quality() == MatchQuality.FULL).count();
        long partial = results.stream().flatMap(result -> result.matches().stream())
                .filter(match -> match.quality() == MatchQuality.PARTIAL).count();
        audit.record("PARTNER_LOOKUP", principal.keyId(), principal.partner(),
                "entries=" + entries + " full=" + full + " partial=" + partial);
    }
}
