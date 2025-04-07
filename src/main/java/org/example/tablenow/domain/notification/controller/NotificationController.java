package org.example.tablenow.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.dto.response.NotificationResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationUpdateReadResponseDto;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
  private final NotificationService notificationService;
  private final UserRepository userRepository;

  // 알림 생성
  @PostMapping
  public ResponseEntity<NotificationResponseDto> createNotification(@RequestBody NotificationRequestDto requestDto){
    NotificationResponseDto responseDto = notificationService.createNotification(requestDto);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  // 알림 조회
  @GetMapping
  public ResponseEntity<List<NotificationResponseDto>> getNotifications(){
    //테스트 용
    User user = userRepository.findById(1L)
        .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

    List<NotificationResponseDto> responseDtos = notificationService.findNotifications(user);
    return new ResponseEntity<>(responseDtos, HttpStatus.OK);
  }

  // 알림 읽음 처리
  @PatchMapping("/{notificationId}")
  public ResponseEntity<NotificationUpdateReadResponseDto> updateNotificationRead(@PathVariable Long notificationId) {
    // 테스트용
    User user = userRepository.findById(1L)
        .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "유저 없음"));

    NotificationUpdateReadResponseDto responseDto = notificationService.updateNotificationRead(notificationId, user);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }


}
