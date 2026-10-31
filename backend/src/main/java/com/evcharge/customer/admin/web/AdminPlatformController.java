package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminPlatformService;
import com.evcharge.customer.admin.application.AdminSettingsService;
import com.evcharge.customer.admin.application.CountryStationPrices;
import com.evcharge.customer.admin.application.PlatformSettings;
import com.evcharge.customer.admin.application.ProbeReport;
import com.evcharge.customer.admin.domain.AdminAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
class AdminPlatformController {

    private final AdminPlatformService platform;
    private final AdminSettingsService settings;

    AdminPlatformController(AdminPlatformService platform, AdminSettingsService settings) {
        this.platform = platform;
        this.settings = settings;
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

    @PutMapping("/settings/{section}")
    PlatformSettings update(AdminAccess admin, @PathVariable String section,
                            @Valid @RequestBody UpdateSettingsRequest request) {
        return settings.update(admin, section, request.values());
    }

    record UpdateSettingsRequest(@NotEmpty Map<String, Object> values) {
    }

    record TariffsResponse(List<PlatformSettings> settings, List<CountryStationPrices> stations) {
    }

    record SystemResponse(List<PlatformSettings> settings, List<ProbeReport> probes, long startedAtMillis,
                          String javaVersion) {
    }
}
