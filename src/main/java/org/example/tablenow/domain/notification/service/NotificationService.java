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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    Notification notification = new Notification(findUser, requestDto.getType(), requestDto.getContent());
    notificationRepository.save(notification);

    // 빈자리 대기 알림일 경우에는 isNotified = true로 업데이트
    if (NotificationType.VACANCY.equals(requestDto.getType())) {
      handleVacancyNotification(findUser, requestDto.getStoreId());
    }

    return NotificationResponseDto.fromNotification(notification);
  }

  // 알림 조회
  @Transactional(readOnly = true)
  public Page<NotificationResponseDto> findNotifications(Long userId, int page, int size, Boolean isRead) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

    return (isRead != null)
        ? notificationRepository.findAllByUserAndIsRead(user, isRead, pageable).map(NotificationResponseDto::fromNotification)
        : notificationRepository.findAllByUser(user, pageable).map(NotificationResponseDto::fromNotification);
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

  private void handleVacancyNotification(User user, Long storeId) {
    if (storeId == null) {
      throw new HandledException(ErrorCode.NOTIFICATION_BAD_REQUEST);
    }

    Store store = storeService.getStore(storeId);

    Waitlist waitlist = waitlistRepository
        .findByUserAndStoreAndIsNotifiedFalse(user, store)
        .orElseThrow(() -> new HandledException(ErrorCode.WAITLIST_NOT_FOUNND));

    waitlist.updateNotified();
  }

}

