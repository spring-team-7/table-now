package org.example.tablenow.domain.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.notification.dto.request.NotificationAlarmRequestDto;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.dto.response.NotificationAlarmResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationUpdateReadResponseDto;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  // 알림 생성
  @PostMapping("/api/v1/notifications")
  public ResponseEntity<NotificationResponseDto> createNotification(
      @Valid @RequestBody NotificationRequestDto requestDto) {
    NotificationResponseDto notificationResponseDto = notificationService.createNotification(requestDto);
    return ResponseEntity.ok(notificationResponseDto);
  }

  // 알림 조회
  @GetMapping("/api/v1/notifications")
  public ResponseEntity<List<NotificationResponseDto>> getNotifications(@AuthenticationPrincipal AuthUser authUser) {
    List<NotificationResponseDto> responseDtos = notificationService.findNotifications(authUser.getId());
    return ResponseEntity.ok(responseDtos);
  }

  // 알림 읽음 처리
  @PatchMapping("/api/v1/notifications/{notificationId}")
  public ResponseEntity<NotificationUpdateReadResponseDto> updateNotificationRead(
      @PathVariable Long notificationId,
      @AuthenticationPrincipal AuthUser authUser) {
    NotificationUpdateReadResponseDto notificationUpdateReadResponseDto = notificationService.updateNotificationRead(notificationId, authUser.getId());
    return ResponseEntity.ok(notificationUpdateReadResponseDto);
  }

  // 알림 수신 여부
  @PatchMapping("/api/v1/notifications/settings")
  public ResponseEntity<NotificationAlarmResponseDto> updateNotificationAlarm(
      @RequestBody NotificationAlarmRequestDto requestDto,
      @AuthenticationPrincipal AuthUser authUser) {
    NotificationAlarmResponseDto notificationAlarmResponseDto = notificationService.updateNotificationAlarm(authUser.getId(), requestDto.isAlarmEnabled());
    return ResponseEntity.ok(notificationAlarmResponseDto);
  }
}
