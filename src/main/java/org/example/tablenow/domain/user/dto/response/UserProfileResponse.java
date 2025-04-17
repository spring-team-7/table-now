package org.example.tablenow.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isSocialUser")
    private final boolean socialUser;

    public static UserProfileResponse fromUser(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .imageUrl(user.getImageUrl())
                .socialUser(user.isOAuthUser())
                .build();
    }
}
