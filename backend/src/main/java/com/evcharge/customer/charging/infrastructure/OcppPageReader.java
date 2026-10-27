package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.domain.RemoteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

final class OcppPageReader {

    private static final Logger log = LoggerFactory.getLogger(OcppPageReader.class);

    private OcppPageReader() {
    }

    static List<RemoteSession> readAll(int maxPages, IntFunction<OcppSessionPage> fetchPage) {
        List<RemoteSession> sessions = new ArrayList<>();
        for (int index = 0; index < maxPages; index++) {
            OcppSessionPage page = fetchPage.apply(index);
            List<RemoteSession> found = page == null ? List.of() : page.sessions();
            if (found.isEmpty()) {
                return sessions;
            }
            sessions.addAll(found);
            if (page.total() == null || sessions.size() >= page.total()) {
                return sessions;
            }
        }
        log.error("ALERT the charging platform has more than {} pages of active sessions: only {} were read",
                maxPages, sessions.size());
        return sessions;
    }
}
