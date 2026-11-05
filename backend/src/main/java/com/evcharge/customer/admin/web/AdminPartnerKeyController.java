package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminPartnerKeyService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.partner.PartnerKeyInfo;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/partners/{partner}/keys")
class AdminPartnerKeyController {

    private final AdminPartnerKeyService service;

    AdminPartnerKeyController(AdminPartnerKeyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    IssuedKeyResponse issue(AdminAccess admin, @PathVariable String partner,
                            @Valid @RequestBody IssueKeyRequest request) {
        return IssuedKeyResponse.of(service.issue(admin, partner, request.country()), partner, request.country());
    }

    @GetMapping
    List<PartnerKeyInfo> list(AdminAccess admin, @PathVariable String partner) {
        return service.list(admin, partner);
    }

    @DeleteMapping("/{keyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revoke(AdminAccess admin, @PathVariable String partner, @PathVariable UUID keyId) {
        service.revoke(admin, partner, keyId);
    }
}
