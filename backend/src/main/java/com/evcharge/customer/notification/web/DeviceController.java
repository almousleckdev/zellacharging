package com.evcharge.customer.notification.web;

import com.evcharge.customer.notification.application.DeviceService;
import com.evcharge.customer.shared.CustomerPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/devices")
class DeviceController {

    private final DeviceService devices;

    DeviceController(DeviceService devices) {
        this.devices = devices;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void register(CustomerPrincipal principal, @Valid @RequestBody DeviceRequest request) {
        String language = request.language() == null ? "fr" : request.language();
        devices.register(principal.id(), request.token(), request.platform(), language);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unregister(CustomerPrincipal principal, @Valid @RequestBody DeviceRequest request) {
        devices.unregister(principal.id(), request.token());
    }
}
