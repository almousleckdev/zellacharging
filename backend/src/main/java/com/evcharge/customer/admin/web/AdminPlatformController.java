package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminPlatformService;
import com.evcharge.customer.admin.application.CountryStationPrices;
import com.evcharge.customer.admin.application.PlatformSettings;
import com.evcharge.customer.admin.application.ProbeReport;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.List;

@RestController
@RequestMapping("/v1/admin")
class AdminPlatformController {

    private final AdminPlatformService platform;

    AdminPlatformController(AdminPlatformService platform) {
        this.platform = platform;
    }

    @GetMapping("/tariffs")
    TariffsResponse tariffs(AdminAccess admin, @RequestParam(required = false) String country) {
        return new TariffsResponse(platform.pricing(admin), platform.stationPrices(admin, country));
    }

    @GetMapping("/system")
    SystemResponse system(AdminAccess admin) {
        var runtime = ManagementFactory.getRuntimeMXBean();
        return new SystemResponse(platform.system(admin), platform.probes(admin), runtime.getStartTime(),
                runtime.getVmVersion());
    }

    record TariffsResponse(List<PlatformSettings> settings, List<CountryStationPrices> stations) {
    }

    record SystemResponse(List<PlatformSettings> settings, List<ProbeReport> probes, long startedAtMillis,
                          String javaVersion) {
    }
}
