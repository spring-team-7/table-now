package org.example.tablenow.domain.notification.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.notification.dto.request.NotificationAlarmRequestDto;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.dto.response.NotificationAlarmResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationUpdateReadResponseDto;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationController {
  private final NotificationService notificationService;

  // 알림 생성
  @PostMapping("/v1/notifications")
  public ResponseEntity<NotificationResponseDto> createNotification(
      @Valid @RequestBody NotificationRequestDto requestDto) {
    NotificationResponseDto notificationResponseDto = notificationService.createNotification(requestDto);
    return ResponseEntity.ok(notificationResponseDto);
  }

  // 알림 조회
  @GetMapping("/v1/notifications")
  public ResponseEntity<Page<NotificationResponseDto>> getNotifications(
      @AuthenticationPrincipal AuthUser authUser,
      @Positive @RequestParam(defaultValue = "1") int page,
      @Positive @RequestParam(defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(notificationService.findNotifications(authUser.getId(), page, size));
  }

  // 알림 읽음 처리
  @PatchMapping("/v1/notifications/{notificationId}")
  public ResponseEntity<NotificationUpdateReadResponseDto> updateNotificationRead(
      @PathVariable Long notificationId,
      @AuthenticationPrincipal AuthUser authUser) {
    NotificationUpdateReadResponseDto notificationUpdateReadResponseDto = notificationService.updateNotificationRead(notificationId, authUser.getId());
    return ResponseEntity.ok(notificationUpdateReadResponseDto);
  }

  // 알림 전체 읽음 처리
  @PatchMapping("/v1/notifications/readAll")
  public ResponseEntity<List<NotificationUpdateReadResponseDto>> updateAllNotificationRead(@AuthenticationPrincipal AuthUser authUser){
    List<NotificationUpdateReadResponseDto> notificationUpdateReadResponseDtos = notificationService.updateAllNotificationRead(authUser.getId());
    return ResponseEntity.ok(notificationUpdateReadResponseDtos);
  }

  // 알림 수신 여부
  @PatchMapping("/v1/notifications/settings")
  public ResponseEntity<NotificationAlarmResponseDto> updateNotificationAlarm(
      @RequestBody NotificationAlarmRequestDto requestDto,
      @AuthenticationPrincipal AuthUser authUser) {
    NotificationAlarmResponseDto notificationAlarmResponseDto = notificationService.updateNotificationAlarm(authUser.getId(), requestDto.isAlarmEnabled());
    return ResponseEntity.ok(notificationAlarmResponseDto);
  }
}
