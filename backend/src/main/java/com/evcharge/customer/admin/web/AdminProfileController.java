package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminProfileService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.AdminAvatar;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/me")
class AdminProfileController {

    private final AdminProfileService profiles;

    AdminProfileController(AdminProfileService profiles) {
        this.profiles = profiles;
    }

    @PutMapping("/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void update(AdminAccess admin, @Valid @RequestBody UpdateProfileRequest request) {
        profiles.update(admin.id(), request.firstName(), request.lastName(), request.phone());
    }

    @PutMapping("/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void upload(AdminAccess admin, @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                @RequestBody byte[] data) {
        profiles.saveAvatar(admin.id(), contentType, data);
    }

    @GetMapping("/avatar")
    ResponseEntity<byte[]> download(AdminAccess admin) {
        return profiles.avatar(admin.id()).map(AdminProfileController::image)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(AdminAccess admin) {
        profiles.deleteAvatar(admin.id());
    }

    static ResponseEntity<byte[]> image(AdminAvatar avatar) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(avatar.contentType()))
                .cacheControl(CacheControl.noCache().cachePrivate())
                .header("X-Content-Type-Options", "nosniff").body(avatar.data());
    }
}
