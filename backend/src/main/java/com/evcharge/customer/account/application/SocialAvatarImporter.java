package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.ProfilePictureFetcher;
import com.evcharge.customer.shared.ApiException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SocialAvatarImporter {

    private final AvatarService avatars;
    private final ProfilePictureFetcher fetcher;

    public SocialAvatarImporter(AvatarService avatars, ProfilePictureFetcher fetcher) {
        this.avatars = avatars;
        this.fetcher = fetcher;
    }

    public void importIfMissing(UUID customerId, String pictureUrl) {
        if (pictureUrl == null || pictureUrl.isBlank() || avatars.find(customerId).isPresent()) {
            return;
        }
        fetcher.fetch(pictureUrl).ifPresent(picture -> {
            try {
                avatars.save(customerId, picture.contentType(), picture.data());
            } catch (ApiException ignored) {
                return;
            }
        });
    }
}
