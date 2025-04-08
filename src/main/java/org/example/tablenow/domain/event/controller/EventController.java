package org.example.tablenow.domain.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.dto.request.EventUpdateRequestDto;
import org.example.tablenow.domain.event.dto.response.EventResponseDto;
import org.example.tablenow.domain.event.service.EventService;
import org.example.tablenow.domain.user.enums.UserRole;
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
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody EventRequestDto request
    ) {
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
}
