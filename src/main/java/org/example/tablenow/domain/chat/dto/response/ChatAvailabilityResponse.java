package org.example.tablenow.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatAvailabilityResponse {
    private final Long reservationId;
    private final Long userId;
    @JsonProperty("isAvailable")
    private final boolean available;
    private final String reason;
}
