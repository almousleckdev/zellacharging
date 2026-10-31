package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.SettingsSource;
import com.evcharge.customer.shared.RuntimeSettings;
import com.evcharge.customer.shared.SettingKey;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminSettingsService {

    private final AdminAccessService access;
    private final List<SettingsSource> sources;
    private final RuntimeSettings runtime;
    private final AdminAudit audit;

    public AdminSettingsService(AdminAccessService access, List<SettingsSource> sources, RuntimeSettings runtime,
                                AdminAudit audit) {
        this.access = access;
        this.sources = sources;
        this.runtime = runtime;
        this.audit = audit;
    }

    @Transactional
    public PlatformSettings update(AdminAccess admin, String section, Map<String, Object> changes) {
        access.require(admin, Permission.ADMINS_MANAGE);
        SettingsSource source = sources.stream().filter(candidate -> candidate.section().equals(section)).findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SETTINGS_SECTION_NOT_FOUND",
                        "Settings section not found"));
        Map<String, Object> current = source.values();
        Map<String, Object> parsed = new LinkedHashMap<>();
        changes.forEach((field, raw) -> {
            SettingKey key = SettingKey.of(section, field).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                    "INVALID_SETTING", "Setting " + field + " cannot be edited"));
            parsed.put(field, raw == null ? null : SettingRules.parse(key, raw));
        });
        Map<String, Object> merged = new LinkedHashMap<>(current);
        parsed.forEach((field, value) -> {
            if (value != null) {
                merged.put(field, value);
            }
        });
        SettingRules.requireOrdered(merged);
        parsed.forEach((field, value) -> {
            SettingKey key = SettingKey.of(section, field).orElseThrow();
            if (value == null) {
                runtime.clear(key);
                audit.record(admin.id(), null, "SETTING_RESET", key.storageKey(), String.valueOf(current.get(field)));
                return;
            }
            runtime.put(key, value.toString(), admin.id());
            audit.record(admin.id(), null, "SETTING_UPDATED", key.storageKey(), current.get(field) + " -> " + value);
        });
        return new PlatformSettings(section, source.values());
    }
}
