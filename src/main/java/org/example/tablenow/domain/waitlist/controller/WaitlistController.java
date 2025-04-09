package org.example.tablenow.domain.waitlist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.waitlist.dto.request.WaitlistRequestDto;
import org.example.tablenow.domain.waitlist.dto.response.WaitlistResponseDto;
import org.example.tablenow.domain.waitlist.service.WaitlistService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WaitlistController {
  private final WaitlistService waitlistService;
  // 빈자리 대기 등록
  @PostMapping("/v1/waitlist")
  public ResponseEntity<WaitlistResponseDto> registerWaitlist(
      @Valid @RequestBody WaitlistRequestDto requestDto,
      @AuthenticationPrincipal AuthUser authUser){

    WaitlistResponseDto waitlistResponseDto = waitlistService.registerWaitlist(authUser.getId(), requestDto);
    return ResponseEntity.ok(waitlistResponseDto);
  }
}
