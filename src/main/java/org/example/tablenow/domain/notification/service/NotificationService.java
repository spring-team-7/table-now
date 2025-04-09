package org.example.tablenow.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.dto.response.NotificationAlarmResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationUpdateReadResponseDto;
import org.example.tablenow.domain.notification.entity.Notification;
import org.example.tablenow.domain.notification.enums.NotificationType;
import org.example.tablenow.domain.notification.repository.NotificationRepository;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.example.tablenow.domain.waitlist.repository.WaitlistRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final StoreService storeService;
  private final WaitlistRepository waitlistRepository;

  // 알림 생성
  @Transactional
  public NotificationResponseDto createNotification(NotificationRequestDto requestDto) {
    User findUser = userRepository.findById(requestDto.getUserId())
        .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

    //알람 수신 여부 확인(수신 거부된 사람한테 못 보냄)
    if (!findUser.getIsAlarmEnabled()) {
      throw new HandledException(ErrorCode.NOTIFICATION_DISABLED);
    }

    Notification notification = new Notification(findUser,requestDto.getType(),requestDto.getContent());
    notificationRepository.save(notification);

    // 빈자리 대기 알람일 경우에는 isNotified = true로 업데이트
    if(requestDto.getType() == NotificationType.VACANCY){
      if (requestDto.getStoreId() == null){
        throw new HandledException(ErrorCode.NOTIFICATION_BAD_REQUEST);
      }
      Store findStore = storeService.getStore(requestDto.getStoreId());

      Waitlist waitlist = waitlistRepository
          .findByUserAndStoreAndIsNotifiedFalse(findUser,findStore)
          .orElseThrow(() -> new HandledException(ErrorCode.WAITLIST_NOT_FOUNND));

      waitlist.updateNotified();
    }

    return NotificationResponseDto.fromNotification(notification);
  }

  // 알림 조회
  @Transactional(readOnly = true)
  public List<NotificationResponseDto> findNotifications(Long userId) {
    User findUser = userRepository.findById(userId)
        .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

    List<Notification> notificationList = notificationRepository.findAllByUserOrderByCreatedAtDesc(findUser);
    return notificationList.stream()
        .map(NotificationResponseDto::fromNotification)
        .toList();
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
    return NotificationUpdateReadResponseDto.fromNotification(findNotification);
  }

  // 알림 전체 읽음 처리
  @Transactional
  public List<NotificationUpdateReadResponseDto> updateAllNotificationRead(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

    List<Notification> notificationList = notificationRepository.findAllByUserAndIsReadFalse(user);
    return notificationList.stream()
        .peek(Notification::updateRead)
        .map(NotificationUpdateReadResponseDto::fromNotification)
        .toList();
  }

  //알람 수신 여부
  @Transactional
  public NotificationAlarmResponseDto updateNotificationAlarm(Long userId, boolean isAlarmEnabled) {
    User findUser = userRepository.findById(userId)
        .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
    // 알람 수신 여부 업데이트
    findUser.updateAlarmSetting(isAlarmEnabled);

    return NotificationAlarmResponseDto.fromNotification(findUser);
  }

}

