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
import org.example.tablenow.domain.user.service.UserService;
import org.example.tablenow.domain.waitlist.repository.WaitlistRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

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

        @Test
        void 알림_정상_생성() {
            NotificationRequestDto dto = NotificationRequestDto.builder()
                .userId(1L)
                .storeId(null)
                .type(NotificationType.REMIND)
                .content("예약 알림")
                .build();

            given(userService.getUser(1L)).willReturn(user);
            given(notificationRepository.save(any(Notification.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            NotificationResponseDto result = notificationService.createNotification(dto);

            assertEquals("예약 알림", result.getContent());
        }

        @Test
        void 유저를_찾지_못해_알림_생성_실패() {
            NotificationRequestDto dto = NotificationRequestDto.builder()
                .userId(1L)
                .storeId(null)
                .type(NotificationType.REMIND)
                .content("예약 알림")
                .build();

            given(userService.getUser(1L)).willThrow(new HandledException(ErrorCode.USER_NOT_FOUND));

            HandledException exception = assertThrows(HandledException.class, () ->
                notificationService.createNotification(dto)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
        }
    }

    @Nested
    class 빈자리_알림_예외 {

        @Test
        void storeId가_null일때_예외() {
            NotificationRequestDto dto = NotificationRequestDto.builder()
                .userId(1L)
                .storeId(null)
                .type(NotificationType.VACANCY)
                .content("빈자리 알림")
                .build();

            given(userService.getUser(1L)).willReturn(user);

            HandledException exception = assertThrows(HandledException.class, () ->
                notificationService.createNotification(dto)
            );

            assertEquals(ErrorCode.NOTIFICATION_BAD_REQUEST.getStatus(), exception.getHttpStatus());
        }

        @Test
        void 대기정보_없어서_예외() {
            NotificationRequestDto dto = NotificationRequestDto.builder()
                .userId(1L)
                .storeId(10L)
                .type(NotificationType.VACANCY)
                .content("빈자리 알림")
                .build();

            Store store = mock(Store.class);
            given(userService.getUser(1L)).willReturn(user);
            given(storeService.getStore(10L)).willReturn(store);
            given(waitlistRepository.findByUserAndStoreAndIsNotifiedFalse(user, store)).willReturn(Optional.empty());

            HandledException exception = assertThrows(HandledException.class, () ->
                notificationService.createNotification(dto)
            );

            assertEquals(ErrorCode.WAITLIST_NOT_FOUND.getStatus(), exception.getHttpStatus());
        }
    }

    @Nested
    class 알림_조회 {

        @Test
        void 알림_정상_조회() {
            PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));

            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(any())).willReturn(null);
            given(userService.getUser(1L)).willReturn(user);

            Notification noti1 = new Notification(user, NotificationType.REMIND, "알림1");
            Notification noti2 = new Notification(user, NotificationType.VACANCY, "알림2");
            Page<Notification> page = new PageImpl<>(List.of(noti1, noti2));

            given(notificationRepository.findAllByUserAndIsRead(eq(user), eq(false), any(Pageable.class))).willReturn(page);

            Page<NotificationResponseDto> result = notificationService.findNotifications(1L, 1, 5, false);

            assertEquals("알림1", result.getContent().get(0).getContent());
            assertEquals("알림2", result.getContent().get(1).getContent());
        }

        @Test
        void 유저를_찾지_못해_알림_조회_실패() {
            given(userService.getUser(1L)).willThrow(new HandledException(ErrorCode.USER_NOT_FOUND));

            HandledException exception = assertThrows(HandledException.class, () ->
                notificationService.findNotifications(1L, 1, 5, false)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
        }
    }

    @Test
    void 알림_정상_읽음처리() {
        // given
        given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(any())).willReturn(null);

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
        given(notificationRepository.findById(10L)).willReturn(Optional.empty());

        HandledException exception = assertThrows(HandledException.class, () ->
            notificationService.updateNotificationRead(10L, 1L)
        );

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

        given(userService.getUser(1L)).willReturn(user);
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
        given(userService.getUser(1L)).willThrow(new HandledException(ErrorCode.USER_NOT_FOUND));

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
        given(userService.getUser(1L)).willReturn(user);
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
        given(userService.getUser(1L)).willThrow(new HandledException(ErrorCode.USER_NOT_FOUND));

        // when & then
        HandledException exception = assertThrows(HandledException.class, () -> {
            notificationService.updateNotificationAlarm(1L, true);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
    }
}
