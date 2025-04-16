package org.example.tablenow.domain.event.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventJoinController {

    private final EventJoinService eventJoinService;

    @PostMapping("/v1/events/{eventId}/join")
    public ResponseEntity<EventJoinResponseDto> joinEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventJoinService.joinEvent(eventId, authUser));
    }

    @PostMapping("/v2/events/{eventId}/join")
    public ResponseEntity<EventJoinResponseDto> joinEventV2(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventJoinService.joinEventV2(eventId, authUser));
    }

    @PostMapping("/v3/events/{eventId}/join")
    public ResponseEntity<EventJoinResponseDto> joinEventV3(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventJoinService.joinEventV3(eventId, authUser));
    }
}
