package org.example.tablenow.domain.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.event.dto.response.EventJoinResponseDto;
import org.example.tablenow.domain.event.service.EventJoinService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이벤트 참여 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventJoinController {

    private final EventJoinService eventJoinService;

    @Operation(summary = "이벤트 참여 - Lock 없음")
    @PostMapping("/v1/events/{eventId}/join")
    public ResponseEntity<EventJoinResponseDto> joinEventWithoutLock(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventJoinService.joinEventWithoutLock(eventId, authUser));
    }

    @Operation(summary = "이벤트 참여 - DB Lock 방식")
    @PostMapping("/v2/events/{eventId}/join")
    public ResponseEntity<EventJoinResponseDto> joinEventWithDBLock(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventJoinService.joinEventWithDBLock(eventId, authUser));
    }

    @Operation(summary = "이벤트 참여 - Redisson Lock 방식")
    @PostMapping("/v3/events/{eventId}/join")
    public ResponseEntity<EventJoinResponseDto> joinEventWithLock(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventJoinService.joinEventWithRedissonLock(eventId, authUser));
    }
}
