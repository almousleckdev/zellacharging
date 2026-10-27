package com.evcharge.customer.account.web;

import com.evcharge.customer.account.application.AvatarService;
import com.evcharge.customer.account.domain.Avatar;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.evcharge.customer.shared.CustomerPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/me/avatar")
class AvatarController {

    private final AvatarService avatars;

    AvatarController(AvatarService avatars) {
        this.avatars = avatars;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void upload(CustomerPrincipal me, @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                @RequestBody byte[] data) {
        avatars.save(me.id(), contentType, data);
    }

    @GetMapping
    ResponseEntity<byte[]> download(CustomerPrincipal me) {
        return avatars.find(me.id())
                .map(this::image).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(CustomerPrincipal me) {
        avatars.delete(me.id());
    }

    private ResponseEntity<byte[]> image(Avatar avatar) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(avatar.contentType())).body(avatar.data());
    }
}
