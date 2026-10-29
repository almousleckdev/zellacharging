package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminCustomerService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/customers")
class AdminCustomerController {

    private final AdminCustomerService customers;

    AdminCustomerController(AdminCustomerService customers) {
        this.customers = customers;
    }

    @GetMapping
    CustomerListResponse list(AdminAccess admin, @RequestParam(required = false) String country,
                              @RequestParam(required = false) String q, @RequestParam(required = false) String status, @RequestParam(required = false) String sort,
                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return CustomerListResponse.from(customers.list(admin, country, q, status, sort, page, size));
    }

    @PostMapping("/{country}/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void suspend(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        customers.suspend(admin, country, id);
    }

    @PostMapping("/{country}/{id}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reactivate(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        customers.reactivate(admin, country, id);
    }

    @DeleteMapping("/{country}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        customers.delete(admin, country, id);
    }

    @GetMapping("/{country}/{id}")
    CustomerDetailResponse detail(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        return CustomerDetailResponse.from(customers.detail(admin, country, id));
    }
}
