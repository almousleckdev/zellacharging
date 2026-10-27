package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.ManagedAdminPage;

import java.util.List;

record AdminUserPageResponse(long total, int page, int size, List<AdminUserResponse> items) {

    static AdminUserPageResponse from(ManagedAdminPage page) {
        return new AdminUserPageResponse(page.total(), page.page(), page.size(),
                page.items().stream().map(AdminUserResponse::from).toList());
    }
}
