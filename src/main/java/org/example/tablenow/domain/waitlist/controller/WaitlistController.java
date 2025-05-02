package org.example.tablenow.domain.waitlist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.waitlist.dto.request.WaitlistRequestDto;
import org.example.tablenow.domain.waitlist.dto.response.WaitlistFindResponseDto;
import org.example.tablenow.domain.waitlist.dto.response.WaitlistResponseDto;
import org.example.tablenow.domain.waitlist.service.WaitlistService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "대기 등록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WaitlistController {
    private final WaitlistService waitlistService;

    // 빈자리 대기 등록
    @Operation(summary = "빈자리 대기 등록")
    @PostMapping("/v1/waitlist")
    public ResponseEntity<WaitlistResponseDto> registerWaitlist(
        @Valid @RequestBody WaitlistRequestDto requestDto,
        @AuthenticationPrincipal AuthUser authUser) {

        WaitlistResponseDto waitlistResponseDto = waitlistService.registerWaitlist(authUser.getId(), requestDto);
        return ResponseEntity.ok(waitlistResponseDto);
    }

    // 빈자리 대기 등록 - Redisson 분산 락 적용
    @Operation(summary = "빈자리 대기 등록 (Redisson Lock)")
    @PostMapping("/v2/waitlist")
    public ResponseEntity<WaitlistResponseDto> registerWaitlistV2(
        @Valid @RequestBody WaitlistRequestDto requestDto,
        @AuthenticationPrincipal AuthUser authUser) {

        WaitlistResponseDto waitlistResponseDto = waitlistService.registerLockWaitlist(authUser.getId(), requestDto);
        return ResponseEntity.ok(waitlistResponseDto);
    }

    // 내 대기 목록 조회
    @Operation(summary = "내 대기 목록 조회")
    public ResponseEntity<List<WaitlistFindResponseDto>> getMyWaitlist(@AuthenticationPrincipal AuthUser authUser) {
        List<WaitlistFindResponseDto> myWaitlist = waitlistService.findMyWaitlist(authUser.getId());
        return ResponseEntity.ok(myWaitlist);
    }
}
