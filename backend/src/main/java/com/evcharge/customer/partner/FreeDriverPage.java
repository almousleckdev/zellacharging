package com.evcharge.customer.partner;

import java.util.List;

public record FreeDriverPage(List<FreeDriverOption> items, int page, int size, long total) {
}
