package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminSeriesService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/v1/admin/series")
class AdminSeriesController {

    private final AdminSeriesService series;

    AdminSeriesController(AdminSeriesService series) {
        this.series = series;
    }

    @GetMapping
    SeriesResponse series(AdminAccess admin, @RequestParam(required = false) String country,
                          @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
                          @RequestParam(required = false) String bucket) {
        return SeriesResponse.from(series.series(admin, country, from, to, bucket));
    }
}
