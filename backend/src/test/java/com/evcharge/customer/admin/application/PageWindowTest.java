package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageWindowTest {

    @Test
    void theOffsetIsThePageTimesTheSize() {
        PageWindow window = PageWindow.of(3, 20);

        assertThat(window.index()).isEqualTo(3);
        assertThat(window.size()).isEqualTo(20);
        assertThat(window.offset()).isEqualTo(60);
        assertThat(window.end()).isEqualTo(80);
    }

    @Test
    void aNegativePageIsTheFirstPage() {
        PageWindow window = PageWindow.of(-5, 20);

        assertThat(window.index()).isZero();
        assertThat(window.offset()).isZero();
    }

    @Test
    void theSizeIsKeptBetweenOneAndTheMaximum() {
        assertThat(PageWindow.of(0, 0).size()).isOne();
        assertThat(PageWindow.of(0, -4).size()).isOne();
        assertThat(PageWindow.of(0, 100_000).size()).isEqualTo(PageWindow.MAX_SIZE);
    }

    @Test
    void theLastReachableOffsetIsAccepted() {
        PageWindow window = PageWindow.of(100, 100);

        assertThat(window.offset()).isEqualTo(PageWindow.MAX_OFFSET);
    }

    @Test
    void anOffsetBeyondTheMaximumIsRefused() {
        assertThatThrownBy(() -> PageWindow.of(101, 100)).isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).code()).isEqualTo("PAGE_OUT_OF_RANGE");
    }

    @Test
    void aHugePageIsRefusedInsteadOfOverflowingToAnotherOffset() {
        assertThatThrownBy(() -> PageWindow.of(Integer.MAX_VALUE, 100)).isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).code()).isEqualTo("PAGE_OUT_OF_RANGE");
        assertThatThrownBy(() -> PageWindow.of(43_000_000, 100)).isInstanceOf(ApiException.class);
    }
}
