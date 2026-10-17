package com.evcharge.customer.catalog.application;

import com.evcharge.customer.catalog.application.port.StationDirectory;
import com.evcharge.customer.catalog.domain.QrContent;
import com.evcharge.customer.catalog.domain.Station;
import com.evcharge.customer.shared.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final StationDirectory directory;

    public CatalogService(StationDirectory directory) {
        this.directory = directory;
    }

    public List<Station> nearby(Double latitude, Double longitude, Double radiusKm) {
        boolean partial = (latitude == null) != (longitude == null)
                || (radiusKm != null && latitude == null);
        if (partial) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_LOCATION",
                    "latitude, longitude and radiusKm must be given together");
        }
        return directory.list(latitude, longitude, latitude == null ? null : radiusKm);
    }

    public Station station(String stationId) {
        return directory.find(stationId).orElseThrow(CatalogService::notFound);
    }

    public ResolvedConnector resolveQr(String scanned) {
        String token = QrContent.tokenOf(scanned);
        log.info("QR scan received content='{}' extractedToken='{}'", scanned, token);
        if (token.isEmpty()) {
            throw invalidQr();
        }
        var ref = directory.resolveQr(token).orElseThrow(() -> {
            log.warn("QR token '{}' not found on the charging network", token);
            return invalidQr();
        });
        Station station = directory.find(ref.stationId()).orElseThrow(CatalogService::invalidQr);
        return new ResolvedConnector(station, station.connector(ref.connectorId())
                .orElseThrow(CatalogService::invalidQr));
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Station not found");
    }

    private static ApiException invalidQr() {
        return new ApiException(HttpStatus.NOT_FOUND, "INVALID_QR", "QR code is invalid or expired");
    }
}
