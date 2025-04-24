package org.example.tablenow.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatReadStatusResponse {
    private final Long reservationId;
    private final Long userId;
    private final String message;
}
