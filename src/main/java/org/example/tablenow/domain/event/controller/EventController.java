package org.example.tablenow.domain.event.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    @Secured(UserRole.Authority.ADMIN)
    @PostMapping("/v1/admin/events")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/v1/admin/events/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequestDto request
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @Secured(UserRole.Authority.ADMIN)
    @DeleteMapping("/v1/admin/events/{id}")
    public ResponseEntity<EventDeleteResponseDto> deleteEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.deleteEvent(id));
    }

    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/v1/admin/events/{id}/close")
    public ResponseEntity<EventCloseResponseDto> closeEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.closeEvent(id));
    }

    @GetMapping("/v1/events")
    public ResponseEntity<Page<EventResponseDto>> getEvents(
            @RequestParam(required = false) EventStatus status,
            @Positive @RequestParam(defaultValue = "1") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(eventService.getEvents(status, page, size));
    }
}
