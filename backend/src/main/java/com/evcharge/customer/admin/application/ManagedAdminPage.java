package com.evcharge.customer.admin.application;

import java.util.List;

public record ManagedAdminPage(long total, int page, int size, List<ManagedAdmin> items) {
}
