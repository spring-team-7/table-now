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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
  private final NotificationService notificationService;

  // 알림 생성
  @PostMapping
  public ResponseEntity<NotificationResponseDto> createNotification(
      @Valid @RequestBody NotificationRequestDto requestDto){
    NotificationResponseDto responseDto = notificationService.createNotification(requestDto);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  // 알림 조회
  @GetMapping
  public ResponseEntity<List<NotificationResponseDto>> getNotifications(@AuthenticationPrincipal AuthUser authUser){

    List<NotificationResponseDto> responseDtos = notificationService.findNotifications(authUser.getId());
    return new ResponseEntity<>(responseDtos, HttpStatus.OK);
  }

  // 알림 읽음 처리
  @PatchMapping("/{notificationId}")
  public ResponseEntity<NotificationUpdateReadResponseDto> updateNotificationRead(
      @PathVariable Long notificationId,
      @AuthenticationPrincipal AuthUser authUser) {

    NotificationUpdateReadResponseDto responseDto = notificationService.updateNotificationRead(notificationId, authUser.getId());
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  // 알림 수신 여부
  @PatchMapping("/settings")
  public ResponseEntity<NotificationAlarmResponseDto> updateNotificationAlarm(
      @RequestBody NotificationAlarmRequestDto requestDto,
      @AuthenticationPrincipal AuthUser authUser) {

    NotificationAlarmResponseDto responseDto = notificationService.updateNotificationAlarm(authUser.getId(), requestDto.isAlarmEnabled());
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

}
