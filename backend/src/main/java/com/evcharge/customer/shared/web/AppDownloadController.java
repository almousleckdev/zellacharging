package com.evcharge.customer.shared.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.regex.Pattern;

@RestController
class AppDownloadController {

    private static final Pattern IOS = Pattern.compile("iPhone|iPad|iPod", Pattern.CASE_INSENSITIVE);

    private final String appStoreUrl;
    private final String playStoreUrl;

    AppDownloadController(@Value("${ZELLA_APP_STORE_URL:}") String appStoreUrl,
                          @Value("${ZELLA_PLAY_STORE_URL:https://play.google.com/store/apps/details?id=com.zella.charging.gabon}")
                          String playStoreUrl) {
        this.appStoreUrl = appStoreUrl;
        this.playStoreUrl = playStoreUrl;
    }

    @GetMapping("/v1/app/download")
    ResponseEntity<Void> download(@RequestHeader(value = HttpHeaders.USER_AGENT, defaultValue = "") String userAgent) {
        boolean apple = IOS.matcher(userAgent).find() && !appStoreUrl.isBlank();
        return ResponseEntity.status(307).location(URI.create(apple ? appStoreUrl : playStoreUrl)).build();
    }
}
