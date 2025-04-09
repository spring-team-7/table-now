package org.example.tablenow.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.user.entity.User;

@Getter
@Builder
public class UserProfileResponse {
    private final Long id;
    private final String email;
    private final String name;
    private final String nickname;
    private final String phoneNumber;
    private final String imageUrl;
    private final boolean isSocialUser;

    public static UserProfileResponse fromUser(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .imageUrl(user.getImageUrl())
                .isSocialUser(user.getOauthProvider() != null)
                .build();
    }
}
