package com.evcharge.customer.admin.domain;

import java.util.List;

public record AdminPage(long total, List<AdminUser> items) {
}
