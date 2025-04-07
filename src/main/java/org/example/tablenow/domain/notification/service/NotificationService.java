package org.example.tablenow.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.dto.response.NotificationAlarmResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationUpdateReadResponseDto;
import org.example.tablenow.domain.notification.entity.Notification;
import org.example.tablenow.domain.notification.repository.NotificationRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  // 알림 생성
  public NotificationResponseDto createNotification(NotificationRequestDto requestDto) {
    User findUser = userRepository.findById(requestDto.getUserId())
        .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

    //알람 수신 여부 확인(수신 거부된 사람한테 못 보냄)
    if (!findUser.getIsAlarmEnabled()) {
      throw new HandledException(ErrorCode.NOTIFICATION_DISABLED);
    }

    Notification notification = new Notification(findUser,requestDto.getType(),requestDto.getContent());
    notificationRepository.save(notification);

    return new NotificationResponseDto(notification);
  }

  // 알림 조회
  public List<NotificationResponseDto> findNotifications(Long userId) {
    User findUser = userRepository.findById(userId)
        .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

    List<Notification> notificationList = notificationRepository.findAllByUserOrderByCreatedAtDesc(findUser);
    ArrayList<NotificationResponseDto> responseList = new ArrayList<>();

    for (Notification notification : notificationList) {
      responseList.add(new NotificationResponseDto(notification));
    }

    return responseList;
  }

  // 알림 읽음 처리
  @Transactional
  public NotificationUpdateReadResponseDto updateNotificationRead(Long notificationId, Long userId) {
    Notification findNotification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new HandledException(ErrorCode.NOTIFICATION_NOT_FOUND));

    if (!findNotification.getUser().getId().equals(userId)) {
      throw new HandledException(ErrorCode.NOTIFICATION_MISMATCH);
    }

    findNotification.updateRead();
    return new NotificationUpdateReadResponseDto(findNotification.getId(), findNotification.getIsRead());
  }

  //알람 수신 여부
  @Transactional
  public NotificationAlarmResponseDto updateNotificationAlarm(Long userId, boolean isAlarmEnabled) {
    User findUser = userRepository.findById(userId)
        .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
    // 알람 수신 여부 업데이트
    findUser.updateAlarmSetting(isAlarmEnabled);

    return new NotificationAlarmResponseDto(findUser.getIsAlarmEnabled(), findUser.getUpdatedAt());
  }
}

