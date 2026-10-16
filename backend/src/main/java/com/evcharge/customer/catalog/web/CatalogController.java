package com.evcharge.customer.catalog.web;

import com.evcharge.customer.catalog.application.CatalogService;
import com.evcharge.customer.catalog.domain.Station;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/v1")
class CatalogController {

    private final CatalogService catalog;

    CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/stations")
    List<StationResponse> stations(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm) {
        return catalog.nearby(lat, lng, radiusKm).stream()
                .map(station -> toResponse(station, lat, lng))
                .sorted(Comparator.comparing(StationResponse::distanceKm,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @GetMapping("/stations/{id}")
    StationResponse station(@PathVariable String id) {
        return StationResponse.from(catalog.station(id), null);
    }

    @PostMapping("/qr/resolve")
    QrResolveResponse resolve(@Valid @RequestBody QrResolveRequest request) {
        return QrResolveResponse.from(catalog.resolveQr(request.content()));
    }

    private static StationResponse toResponse(Station station, Double lat, Double lng) {
        Double distance = lat == null ? null : station.distanceKmFrom(lat, lng).orElse(null);
        return StationResponse.from(station, distance);
    }
}
