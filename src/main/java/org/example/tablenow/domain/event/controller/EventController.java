package org.example.tablenow.domain.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.dto.request.EventUpdateRequestDto;
import org.example.tablenow.domain.event.dto.response.EventCloseResponseDto;
import org.example.tablenow.domain.event.dto.response.EventDeleteResponseDto;
import org.example.tablenow.domain.event.dto.response.EventResponseDto;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.event.service.EventService;
import org.example.tablenow.domain.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이벤트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "이벤트 생성")
    @Secured(UserRole.Authority.ADMIN)
    @PostMapping("/v1/admin/events")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @Operation(summary = "이벤트 수정")
    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/v1/admin/events/{eventId}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequestDto request
    ) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, request));
    }

    @Operation(summary = "이벤트 삭제")
    @Secured(UserRole.Authority.ADMIN)
    @DeleteMapping("/v1/admin/events/{eventId}")
    public ResponseEntity<EventDeleteResponseDto> deleteEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.deleteEvent(eventId));
    }

    @Operation(summary = "이벤트 마감")
    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/v1/admin/events/{eventId}/close")
    public ResponseEntity<EventCloseResponseDto> closeEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.closeEvent(eventId));
    }

    @Operation(summary = "이벤트 목록 조회")
    @GetMapping("/v1/events")
    public ResponseEntity<Page<EventResponseDto>> getEvents(
            @RequestParam(required = false) EventStatus status,
            @Positive @RequestParam(defaultValue = "1") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(eventService.getEvents(status, page, size));
    }
}
