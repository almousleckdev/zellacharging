package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminAccountRepository;
import com.evcharge.customer.admin.application.port.AdminAvatarRepository;
import com.evcharge.customer.admin.application.port.AdminPhones;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.admin.domain.AdminAvatar;
import com.evcharge.customer.admin.domain.AdminAvatarPolicy;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Country;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminProfileService {

    private static final int MAX_NAME = 60;

    private final AdminRepository admins;
    private final AdminAccountRepository accounts;
    private final AdminAvatarRepository avatars;
    private final AdminPhones phones;
    private final CountryCatalog catalog;
    private final AdminAudit audit;
    private final Clock clock;

    public AdminProfileService(AdminRepository admins, AdminAccountRepository accounts,
                               AdminAvatarRepository avatars, AdminPhones phones, CountryCatalog catalog,
                               AdminAudit audit, Clock clock) {
        this.admins = admins;
        this.accounts = accounts;
        this.avatars = avatars;
        this.phones = phones;
        this.catalog = catalog;
        this.audit = audit;
        this.clock = clock;
    }

    public ManagedAdmin current(UUID adminId) {
        return new ManagedAdmin(admins.findById(adminId).orElseThrow(), avatars.updatedAt(adminId).orElse(null));
    }

    public void update(UUID adminId, String firstName, String lastName, String phone) {
        String first = name(firstName);
        String last = name(lastName);
        accounts.updateProfile(adminId, first, last, normalizedPhone(phone));
        audit.record(adminId, null, "ADMIN_PROFILE_UPDATED", adminId.toString(), null);
    }

    public void saveAvatar(UUID adminId, String contentType, byte[] data) {
        String type = AdminAvatarPolicy.acceptedType(contentType, data).orElseThrow(
                () -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AVATAR", "The picture is not accepted"));
        avatars.save(adminId, new AdminAvatar(type, data), clock.instant());
    }

    public Optional<AdminAvatar> avatar(UUID adminId) {
        return avatars.find(adminId);
    }

    public Optional<Instant> avatarUpdatedAt(UUID adminId) {
        return avatars.updatedAt(adminId);
    }

    public void deleteAvatar(UUID adminId) {
        avatars.delete(adminId);
    }

    private String normalizedPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        Set<String> regions = catalog.all().stream().map(Country::phoneRegion).collect(Collectors.toSet());
        return phones.normalize(phone, regions).orElseThrow(
                () -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHONE", "The phone number is not valid"));
    }

    private static String name(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_NAME", "The name is not valid");
        }
        return trimmed;
    }
}
