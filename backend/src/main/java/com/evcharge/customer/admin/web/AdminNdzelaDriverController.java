package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminNdzelaDriverService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.partner.NdzelaDriver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/customers/{country}/{id}/ndzela-driver")
class AdminNdzelaDriverController {

    private final AdminNdzelaDriverService service;

    AdminNdzelaDriverController(AdminNdzelaDriverService service) {
        this.service = service;
    }

    @GetMapping
    NdzelaDriver driver(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        return service.driver(admin, country, id);
    }
}
