package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminStationService;
import com.evcharge.customer.admin.application.StationView;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/stations")
class AdminStationController {

    private final AdminStationService stations;

    AdminStationController(AdminStationService stations) {
        this.stations = stations;
    }

    @GetMapping
    List<StationView> list(AdminAccess admin, @RequestParam(required = false) String country) {
        return stations.list(admin, country);
    }

    @GetMapping("/{id}")
    StationView detail(AdminAccess admin, @PathVariable String id) {
        return stations.detail(admin, id);
    }
}
