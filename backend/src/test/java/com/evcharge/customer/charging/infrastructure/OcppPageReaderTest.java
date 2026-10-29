package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.domain.RemoteSession;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class OcppPageReaderTest {

    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 3;

    private final List<Integer> requested = new ArrayList<>();

    private static OcppSessionPayload payload(int transactionId) {
        return new OcppSessionPayload(transactionId, "CP-1", 1, "ACTIVE", null, null, null, null, null, null, null,
                null, "Z-TAG", null, null);
    }

    private OcppSessionPage serve(int index, long total) {
        requested.add(index);
        int from = index * PAGE_SIZE;
        int to = (int) Math.min(total, from + PAGE_SIZE);
        List<OcppSessionPayload> items = from >= to ? List.of()
                : IntStream.range(from, to).mapToObj(OcppPageReaderTest::payload).toList();
        return new OcppSessionPage(items, total);
    }

    @Test
    void aFleetThatFitsOnePageCostsASingleRequest() {
        List<RemoteSession> sessions = OcppPageReader.readAll(MAX_PAGES, index -> serve(index, 40));

        assertThat(sessions).hasSize(40);
        assertThat(requested).containsExactly(0);
    }

    @Test
    void everyPageIsReadUntilTheTotalIsReached() {
        List<RemoteSession> sessions = OcppPageReader.readAll(MAX_PAGES, index -> serve(index, 250));

        assertThat(sessions).hasSize(250);
        assertThat(sessions).extracting(RemoteSession::transactionId).doesNotHaveDuplicates();
        assertThat(requested).containsExactly(0, 1, 2);
    }

    @Test
    void anExactMultipleOfThePageSizeDoesNotAskForAnEmptyExtraPage() {
        List<RemoteSession> sessions = OcppPageReader.readAll(MAX_PAGES, index -> serve(index, 200));

        assertThat(sessions).hasSize(200);
        assertThat(requested).containsExactly(0, 1);
    }

    @Test
    void anEmptyPageEndsTheReadEvenIfTheTotalPromisesMore() {
        List<RemoteSession> sessions = OcppPageReader.readAll(MAX_PAGES, index -> index == 0
                ? serve(0, 500) : new OcppSessionPage(List.of(), 500L));

        assertThat(sessions).hasSize(PAGE_SIZE);
    }

    @Test
    void thePageCapBoundsTheRequestsAndKeepsWhatWasRead() {
        List<RemoteSession> sessions = OcppPageReader.readAll(MAX_PAGES, index -> serve(index, 10_000));

        assertThat(sessions).hasSize(MAX_PAGES * PAGE_SIZE);
        assertThat(requested).containsExactly(0, 1, 2);
    }

    @Test
    void aPageWithoutATotalIsTheWholeFleet() {
        List<RemoteSession> sessions = OcppPageReader.readAll(MAX_PAGES, index -> {
            requested.add(index);
            return new OcppSessionPage(List.of(payload(1), payload(2)), null);
        });

        assertThat(sessions).hasSize(2);
        assertThat(requested).containsExactly(0);
    }

    @Test
    void anEmptyBodyIsNoSessions() {
        assertThat(OcppPageReader.readAll(MAX_PAGES, index -> null)).isEmpty();
    }
}
