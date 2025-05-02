package org.example.tablenow.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.chat.dto.response.ChatAvailabilityResponse;
import org.example.tablenow.domain.chat.dto.response.ChatMessageResponse;
import org.example.tablenow.domain.chat.dto.response.ChatReadStatusResponse;
import org.example.tablenow.domain.chat.service.ChatMessageService;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채팅 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅 가능 여부 확인")
    @GetMapping("/v1/chats/{reservationId}/availability")
    public ResponseEntity<ChatAvailabilityResponse> checkChatAvailability(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(chatMessageService.isChatAvailable(reservationId, authUser.getId()));
    }

    @Operation(summary = "채팅 메시지 목록 조회")
    @GetMapping("/v1/chats/{reservationId}")
    public ResponseEntity<PageResponse<ChatMessageResponse>> getChatMessages(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ChatMessageResponse> page = chatMessageService.getMessages(reservationId, authUser.getId(), pageable);
        return ResponseEntity.ok(new PageResponse<>(page));
    }

    @Operation(summary = "채팅 읽음 처리")
    @PatchMapping("/v1/chats/{reservationId}/read")
    public ResponseEntity<ChatReadStatusResponse> changeReadStatus(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(chatMessageService.changeReadStatus(reservationId, authUser.getId()));
    }
}
