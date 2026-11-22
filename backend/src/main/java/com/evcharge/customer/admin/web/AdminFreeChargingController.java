package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminFreeChargingService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.partner.FreeAgreement;
import com.evcharge.customer.partner.FreeChargingUsePage;
import com.evcharge.customer.partner.FreeDriverOption;
import com.evcharge.customer.shared.FreeSundayWindow;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/free-charging")
class AdminFreeChargingController {

    private final AdminFreeChargingService service;

    AdminFreeChargingController(AdminFreeChargingService service) {
        this.service = service;
    }

    @GetMapping("/agreements")
    List<FreeAgreement> list(AdminAccess admin) {
        return service.list(admin);
    }

    @PostMapping("/agreements")
    @ResponseStatus(HttpStatus.CREATED)
    FreeAgreement create(AdminAccess admin, @Valid @RequestBody CreateAgreementRequest request) {
        return service.create(admin, request.customerId(), request.terms());
    }

    @PutMapping("/agreements/{id}")
    FreeAgreement update(AdminAccess admin, @PathVariable UUID id, @Valid @RequestBody UpdateAgreementRequest request) {
        return service.update(admin, id, request.terms());
    }

    @PostMapping("/agreements/{id}/suspend")
    FreeAgreement suspend(AdminAccess admin, @PathVariable UUID id) {
        return service.suspend(admin, id);
    }

    @PostMapping("/agreements/{id}/resume")
    FreeAgreement resume(AdminAccess admin, @PathVariable UUID id) {
        return service.resume(admin, id);
    }

    @PostMapping("/agreements/{id}/revoke")
    FreeAgreement revoke(AdminAccess admin, @PathVariable UUID id) {
        return service.revoke(admin, id);
    }

    @GetMapping("/drivers")
    List<FreeDriverOption> drivers(AdminAccess admin, @RequestParam(defaultValue = "") String q,
                                   @RequestParam(defaultValue = "20") int size) {
        return service.drivers(admin, q, size);
    }

    @GetMapping("/window")
    FreeSundayWindow window(AdminAccess admin) {
        return service.window(admin);
    }

    @PutMapping("/window")
    FreeSundayWindow changeWindow(AdminAccess admin, @Valid @RequestBody ChangeWindowRequest request) {
        return service.changeWindow(admin, request.window());
    }

    @GetMapping("/usage")
    FreeChargingUsePage usage(AdminAccess admin, @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return service.usage(admin, page, size);
    }
}
