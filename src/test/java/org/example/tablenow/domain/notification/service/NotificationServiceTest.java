package org.example.tablenow.domain.notification.service;

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
import org.example.tablenow.domain.waitlist.repository.WaitlistRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private StoreService storeService;

  @Mock
  private WaitlistRepository waitlistRepository;

  @Mock
  private User user;

  @InjectMocks
  private NotificationService notificationService;

  @Nested
  class 알림_생성 {

    NotificationRequestDto dto;

    @BeforeEach
    void setUp() {
      dto = new NotificationRequestDto();
      ReflectionTestUtils.setField(dto, "userId", 1L);
      ReflectionTestUtils.setField(dto, "type", NotificationType.REMIND);
      ReflectionTestUtils.setField(dto, "content", "예약 알림");

      ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void 알림_정상_생성() {
      given(userRepository.findById(1L)).willReturn(Optional.of(user));
      given(user.getIsAlarmEnabled()).willReturn(true);
      given(notificationRepository.save(any(Notification.class))).willAnswer(invocation -> invocation.getArgument(0));

      NotificationResponseDto result = notificationService.createNotification(dto);

      assertEquals("예약 알림", result.getContent());
    }

    @Test
    void 유저를_찾지_못해_알림_생성_실패() {
      given(userRepository.findById(1L)).willReturn(Optional.empty());

      HandledException exception = assertThrows(HandledException.class, () -> {
        notificationService.createNotification(dto);
      });

      assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
    }

    @Test
    void 알람수신_거부한_유저에게_알림_생성_실패() {
      given(user.getIsAlarmEnabled()).willReturn(false);
      given(userRepository.findById(1L)).willReturn(Optional.of(user));

      HandledException exception = assertThrows(HandledException.class, () -> {
        notificationService.createNotification(dto);
      });

      assertEquals(ErrorCode.NOTIFICATION_DISABLED.getStatus(), exception.getHttpStatus());
    }
  }
  @Nested
  class 빈자리_알림_예외 {

    NotificationRequestDto dto;

    @BeforeEach
    void setUp() {
      dto = new NotificationRequestDto();
      ReflectionTestUtils.setField(dto, "userId", 1L);
      ReflectionTestUtils.setField(dto, "type", NotificationType.VACANCY);
      ReflectionTestUtils.setField(dto, "content", "빈자리 알림");
    }

    @Test
    void StoreId_없어서_예외() {
      // given
      given(userRepository.findById(1L)).willReturn(Optional.of(user));
      given(user.getIsAlarmEnabled()).willReturn(true);

      // when & then
      HandledException exception = assertThrows(HandledException.class, () ->
          notificationService.createNotification(dto)
      );

      assertEquals(ErrorCode.NOTIFICATION_BAD_REQUEST.getStatus(), exception.getHttpStatus());
    }

    @Test
    void 대기정보_없어서_예외() {
      // given
      ReflectionTestUtils.setField(dto, "storeId", 10L);
      Store store = mock(Store.class);

      given(userRepository.findById(1L)).willReturn(Optional.of(user));
      given(user.getIsAlarmEnabled()).willReturn(true);
      given(storeService.getStore(10L)).willReturn(store);
      given(waitlistRepository.findByUserAndStoreAndIsNotifiedFalse(user, store)).willReturn(Optional.empty());

      // when & then
      HandledException exception = assertThrows(HandledException.class, () ->
          notificationService.createNotification(dto)
      );

      assertEquals(ErrorCode.WAITLIST_NOT_FOUNND.getStatus(), exception.getHttpStatus());
    }
  }

  @Nested
  class 알림_조회 {

    @BeforeEach
    void setUp() {
      ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void 알림_정상_조회() {
      given(userRepository.findById(1L)).willReturn(Optional.of(user));

      Notification noti1 = new Notification(user, NotificationType.REMIND, "알림1");
      Notification noti2 = new Notification(user, NotificationType.VACANCY, "알림2");
      given(notificationRepository.findAllByUserOrderByCreatedAtDesc(user)).willReturn(List.of(noti1, noti2));

      List<NotificationResponseDto> result = notificationService.findNotifications(1L);

      assertEquals("알림1", result.get(0).getContent());
      assertEquals("알림2", result.get(1).getContent());
    }

    @Test
    void 유저를_찾지_못해_알림_조회_실패() {
      given(userRepository.findById(1L)).willReturn(Optional.empty());

      HandledException exception = assertThrows(HandledException.class, () -> {
        notificationService.findNotifications(1L);
      });

      assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
    }
  }

  @Test
  void 알림_정상_읽음처리() {
    // given
    Notification notification = new Notification(user, NotificationType.REMIND, "예약 알림");
    ReflectionTestUtils.setField(notification, "id", 10L);
    ReflectionTestUtils.setField(user, "id", 1L);

    given(user.getId()).willReturn(1L);
    given(notificationRepository.findById(10L)).willReturn(Optional.of(notification));

    // when
    NotificationUpdateReadResponseDto result = notificationService.updateNotificationRead(10L, 1L);

    // then
    assertEquals(10L, result.getNotificationId());
    assertTrue(result.getIsRead());
  }

  @Test
  void 알림을_찾지_못해_읽음처리_실패() {
    // given
    given(notificationRepository.findById(10L)).willReturn(Optional.empty());

    // when & then
    HandledException exception = assertThrows(HandledException.class, () -> {
      notificationService.updateNotificationRead(10L, 1L);
    });

    assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND.getStatus(), exception.getHttpStatus());
  }

  @Test
  void 알림_유저가_달라서_읽음처리_실패() {
    // given
    User otherUser = mock(User.class);
    ReflectionTestUtils.setField(otherUser, "id", 2L);
    given(otherUser.getId()).willReturn(2L);
    ReflectionTestUtils.setField(user, "id", 1L);

    Notification notification = new Notification(otherUser, NotificationType.REMIND, "예약 알림");
    ReflectionTestUtils.setField(notification, "id", 10L);
    given(notificationRepository.findById(10L)).willReturn(Optional.of(notification));

    // when & then
    HandledException exception = assertThrows(HandledException.class, () -> {
      notificationService.updateNotificationRead(10L, 1L);
    });

    assertEquals(ErrorCode.NOTIFICATION_MISMATCH.getStatus(), exception.getHttpStatus());
  }

    @Test
    void 전체_읽음처리_성공() {
      // given
      Notification n1 = new Notification(user, NotificationType.REMIND, "알림1");
      Notification n2 = new Notification(user, NotificationType.VACANCY, "알림2");

      ReflectionTestUtils.setField(n1, "id", 10L);
      ReflectionTestUtils.setField(n2, "id", 11L);
      ReflectionTestUtils.setField(user, "id", 1L);

      given(userRepository.findById(1L)).willReturn(Optional.of(user));
      given(notificationRepository.findAllByUserAndIsReadFalse(user)).willReturn(List.of(n1, n2));

      // when
      List<NotificationUpdateReadResponseDto> result =
          notificationService.updateAllNotificationRead(1L);

      // then
      assertTrue(result.get(0).getIsRead());
      assertTrue(n1.getIsRead());
      assertTrue(n2.getIsRead());
    }

    @Test
    void 유저를_찾지_못해서_전체읽음_실패() {
      // given
      given(userRepository.findById(1L)).willReturn(Optional.empty());

      // when & then
      HandledException exception = assertThrows(HandledException.class, () -> {
        notificationService.updateAllNotificationRead(1L);
      });

      assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
    }


  @Test
  void 알람_수신_설정_변경_성공() {
    // given
    ReflectionTestUtils.setField(user, "id", 1L);
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(user.getUpdatedAt()).willReturn(LocalDateTime.now());
    given(user.getIsAlarmEnabled()).willReturn(true);

    // when
    NotificationAlarmResponseDto response = notificationService.updateNotificationAlarm(1L, true);

    // then
    assertTrue(response.isAlarmEnabled());
  }

  @Test
  void 유저를_찾지_못해_수신_설정_실패() {
    // given
    given(userRepository.findById(1L)).willReturn(Optional.empty());

    // when & then
    HandledException exception = assertThrows(HandledException.class, () -> {
      notificationService.updateNotificationAlarm(1L, true);
    });

    assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
  }
}
