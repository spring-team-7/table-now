package org.example.tablenow.domain.notification.service;

import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.dto.response.NotificationAlarmResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationUpdateReadResponseDto;
import org.example.tablenow.domain.notification.entity.Notification;
import org.example.tablenow.domain.notification.enums.NotificationType;
import org.example.tablenow.domain.notification.repository.NotificationRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private User user;

  @InjectMocks
  private NotificationService notificationService;

  @Test
  void 알림_정상_생성() {
    when(user.getIsAlarmEnabled()).thenReturn(true);

    NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
    ReflectionTestUtils.setField(notificationRequestDto, "userId", 1L);
    ReflectionTestUtils.setField(notificationRequestDto, "type", NotificationType.REMIND);
    ReflectionTestUtils.setField(notificationRequestDto, "content", "예약 알림");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(notificationRepository.save(any(Notification.class)))
        .thenReturn(new Notification(user, NotificationType.REMIND, "예약 알림"));

    NotificationResponseDto result = notificationService.createNotification(notificationRequestDto);

    assertEquals("예약 알림", result.getContent());
  }

  @Test
  void 유저를_찾지_못해_알림_생성_실패() {
    NotificationRequestDto requestDto = new NotificationRequestDto();
    ReflectionTestUtils.setField(requestDto, "userId", 1L);
    ReflectionTestUtils.setField(requestDto, "type", NotificationType.REMIND);
    ReflectionTestUtils.setField(requestDto, "content", "예약 알림");

    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    HandledException exception = assertThrows(HandledException.class, () -> {
      notificationService.createNotification(requestDto);
    });

    assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
    assertEquals(ErrorCode.USER_NOT_FOUND.getDefaultMessage(), exception.getMessage());
  }

  @Test
  void 알람수신_거부한_유저에게_알림_생성_실패() {
    NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
    ReflectionTestUtils.setField(notificationRequestDto, "userId", 1L);
    ReflectionTestUtils.setField(notificationRequestDto, "type", NotificationType.REMIND);
    ReflectionTestUtils.setField(notificationRequestDto, "content", "예약 알림");

    when(user.getIsAlarmEnabled()).thenReturn(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    HandledException exception = assertThrows(HandledException.class, () -> {
      notificationService.createNotification(notificationRequestDto);
    });

    assertEquals(ErrorCode.NOTIFICATION_DISABLED.getStatus(), exception.getHttpStatus());
    assertEquals(ErrorCode.NOTIFICATION_DISABLED.getDefaultMessage(), exception.getMessage());
  }


  @Test
  void 알림_정상_조회() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    Notification noti1 = new Notification(user, NotificationType.REMIND, "알림1");
    Notification noti2 = new Notification(user, NotificationType.VACANCY, "알림2");

    when(notificationRepository.findAllByUserOrderByCreatedAtDesc(user))
        .thenReturn(List.of(noti1, noti2));

    List<NotificationResponseDto> result = notificationService.findNotifications(1L);

    assertEquals("알림1", result.get(0).getContent());
    assertEquals("알림2", result.get(1).getContent());
  }

  @Test
  void 유저를_찾지_못해_알림_조회_실패() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    HandledException exception = assertThrows(HandledException.class, () -> {
      notificationService.findNotifications(1L);
    });

    assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
    assertEquals(ErrorCode.USER_NOT_FOUND.getDefaultMessage(), exception.getMessage());
  }

  @Test
  void 알림_정상_읽음처리() {
    Notification notification = new Notification(user, NotificationType.REMIND, "예약 알림");
    ReflectionTestUtils.setField(notification, "id", 10L);
    when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
    when(user.getId()).thenReturn(1L);
    ReflectionTestUtils.setField(user, "id", 1L);

    NotificationUpdateReadResponseDto result = notificationService.updateNotificationRead(10L, 1L);

    assertEquals(10L, result.getNotificationId());
    assertEquals(true, result.getIsRead());
  }

  @Test
  void 알림을_찾지_못해_읽음처리_실패() {
    when(notificationRepository.findById(10L)).thenReturn(Optional.empty());

    HandledException exception = assertThrows(HandledException.class, () -> {
      notificationService.updateNotificationRead(10L, 1L);
    });

    assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND.getStatus(), exception.getHttpStatus());
    assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND.getDefaultMessage(), exception.getMessage());
  }

  @Test
  void 알림_유저가_달라서_읽음처리_실패() {
    User user1 = mock(User.class);
    ReflectionTestUtils.setField(user1, "id", 2L); // 다른 유저 ID
    when(user1.getId()).thenReturn(2L);

    ReflectionTestUtils.setField(user, "id", 1L);

    Notification notification = new Notification(user1, NotificationType.REMIND, "예약 알림");
    ReflectionTestUtils.setField(notification, "id", 10L);

    when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

    HandledException exception = assertThrows(HandledException.class, () -> {
      notificationService.updateNotificationRead(10L, 1L);
    });

    assertEquals(ErrorCode.NOTIFICATION_MISMATCH.getStatus(), exception.getHttpStatus());
    assertEquals(ErrorCode.NOTIFICATION_MISMATCH.getDefaultMessage(), exception.getMessage());
  }

  @Test
  void 알람_수신_설정_변경_성공() {
    ReflectionTestUtils.setField(user, "id", 1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(user.getUpdatedAt()).thenReturn(LocalDateTime.now());
    when(user.getIsAlarmEnabled()).thenReturn(true);

    NotificationAlarmResponseDto response = notificationService.updateNotificationAlarm(1L, true);

    assertEquals(true, response.isAlarmEnabled());
  }

  @Test
  void 유저를_찾지_못해_수신_설정_실패() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    HandledException exception = assertThrows(HandledException.class, () -> {
      notificationService.updateNotificationAlarm(1L, true);
    });

    assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
    assertEquals(ErrorCode.USER_NOT_FOUND.getDefaultMessage(), exception.getMessage());
  }
}