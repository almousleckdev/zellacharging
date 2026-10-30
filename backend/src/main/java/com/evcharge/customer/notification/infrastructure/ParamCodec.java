package com.evcharge.customer.notification.infrastructure;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

final class ParamCodec {

    private ParamCodec() {
    }

    static String encode(Map<String, String> params) {
        return params.entrySet().stream().map(entry -> escape(entry.getKey()) + "=" + escape(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    static Map<String, String> decode(String text) {
        Map<String, String> params = new LinkedHashMap<>();
        if (text == null || text.isBlank()) {
            return params;
        }
        for (String pair : text.split("&")) {
            int cut = pair.indexOf('=');
            params.put(unescape(pair.substring(0, cut)), unescape(pair.substring(cut + 1)));
        }
        return params;
    }

    private static String escape(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String unescape(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
