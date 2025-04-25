package org.example.tablenow.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.chat.dto.response.ChatAvailabilityResponse;
import org.example.tablenow.domain.chat.dto.response.ChatMessagePageResponse;
import org.example.tablenow.domain.chat.dto.response.ChatMessageResponse;
import org.example.tablenow.domain.chat.dto.response.ChatReadStatusResponse;
import org.example.tablenow.domain.chat.service.ChatMessageService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/v1/chats/{reservationId}")
    public ResponseEntity<ChatMessagePageResponse> getChatMessages(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ChatMessageResponse> page = chatMessageService.getMessages(reservationId, authUser.getId(), pageable);
        return ResponseEntity.ok(ChatMessagePageResponse.fromChatMessageResponsePage(page));
    }

    @PatchMapping("/v1/chats/{reservationId}/read")
    public ResponseEntity<ChatReadStatusResponse> changeReadStatus(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(chatMessageService.changeReadStatus(reservationId, authUser.getId()));
    }
}
