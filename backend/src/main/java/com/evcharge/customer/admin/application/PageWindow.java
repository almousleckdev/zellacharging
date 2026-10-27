package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;

record PageWindow(int index, int size, int offset) {

    static final int MAX_SIZE = 100;
    static final int MAX_OFFSET = 10_000;

    static PageWindow of(int page, int size) {
        int boundedSize = Math.max(1, Math.min(size, MAX_SIZE));
        int index = Math.max(0, page);
        long offset = (long) index * boundedSize;
        if (offset > MAX_OFFSET) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PAGE_OUT_OF_RANGE", "Page out of range");
        }
        return new PageWindow(index, boundedSize, (int) offset);
    }

    int end() {
        return offset + size;
    }
}
