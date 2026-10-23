package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.AvatarService;
import com.evcharge.customer.account.domain.Avatar;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/me/avatar")
class AvatarController {

    private final AvatarService avatars;

    AvatarController(AvatarService avatars) {
        this.avatars = avatars;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void upload(@AuthenticationPrincipal Jwt jwt, @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                @RequestBody byte[] data) {
        avatars.save(UUID.fromString(jwt.getSubject()), contentType, data);
    }

    @GetMapping
    ResponseEntity<byte[]> download(@AuthenticationPrincipal Jwt jwt) {
        return avatars.find(UUID.fromString(jwt.getSubject()))
                .map(this::image).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@AuthenticationPrincipal Jwt jwt) {
        avatars.delete(UUID.fromString(jwt.getSubject()));
    }

    private ResponseEntity<byte[]> image(Avatar avatar) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(avatar.contentType())).body(avatar.data());
    }
}
