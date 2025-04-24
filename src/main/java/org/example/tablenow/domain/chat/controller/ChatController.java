package org.example.tablenow.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.chat.dto.response.ChatAvailabilityResponse;
import org.example.tablenow.domain.chat.service.ChatMessageService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/v1/chats/{reservationId}/availability")
    public ResponseEntity<ChatAvailabilityResponse> checkChatAvailability(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(chatMessageService.isChatAvailable(reservationId, authUser.getId()));
    }
}
