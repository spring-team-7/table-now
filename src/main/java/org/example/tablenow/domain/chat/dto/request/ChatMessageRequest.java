package org.example.tablenow.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tablenow.domain.image.annotation.ImageUrlPattern;

@Getter
@AllArgsConstructor
public class ChatMessageRequest {

    @NotNull(message = "예약 아이디는 필수입니다.")
    private Long reservationId;

    private String content;

    @ImageUrlPattern
    private String imageUrl;
}
