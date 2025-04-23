package org.example.tablenow.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final StoreService storeService;
    private final WaitlistRepository waitlistRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);


    // 알림 생성
    @Transactional
    public NotificationResponseDto createNotification(NotificationRequestDto requestDto) {
        User findUser = userRepository.findById(requestDto.getUserId())
            .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

        Notification notification = new Notification(findUser, requestDto.getType(), requestDto.getContent());
        Notification savedNotification = notificationRepository.save(notification);

        // 빈자리 대기 알림일 경우에는 isNotified = true로 업데이트
        if (NotificationType.VACANCY.equals(requestDto.getType())) {
            handleVacancyNotification(findUser, requestDto.getStoreId());
        }

        return NotificationResponseDto.fromNotification(savedNotification);
    }

    // 알림 조회
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> findNotifications(Long userId, int page, int size, Boolean isRead) {

        String key = "notifications:" + userId;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        User findUser = userRepository.findById(userId)
            .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.FALSE.equals(isRead)) {
            List<NotificationResponseDto> cached = (List<NotificationResponseDto>) redisTemplate.opsForValue().get(key);
            if (cached != null && !cached.isEmpty()) {
                log.info(">> Redis hit for {}", key);
                return toPage(cached, pageable);
            }
            log.info(">> Redis miss for {}", key);
        }

        Page<Notification> notifications = (isRead != null)
            ? notificationRepository.findAllByUserAndIsRead(findUser, isRead, pageable)
            : notificationRepository.findAllByUser(findUser, pageable);

        List<NotificationResponseDto> result = notifications.map(NotificationResponseDto::fromNotification).getContent();

        // 캐시 저장
        if (Boolean.FALSE.equals(isRead) && !result.isEmpty()) {
            redisTemplate.opsForValue().set(key, result, CACHE_TTL);
        }

        return toPage(result, pageable);
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

        // 캐시에서 해당 알림 제거
        String key = "notifications:" + userId;
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw instanceof List<?>) {
            List<?> rawList = (List<?>) raw;
            List<NotificationResponseDto> cached = rawList.stream()
                .filter(item -> item instanceof LinkedHashMap)
                .map(item -> objectMapper.convertValue(item, NotificationResponseDto.class))
                .collect(Collectors.toCollection(ArrayList::new));

            cached.removeIf(n -> n.getNotificationId().equals(notificationId));
            redisTemplate.opsForValue().set(key, cached, CACHE_TTL);
        }

        return NotificationUpdateReadResponseDto.fromNotification(findNotification);
    }

    // 알림 전체 읽음 처리
    @Transactional
    public List<NotificationUpdateReadResponseDto> updateAllNotificationRead(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

        List<Notification> notificationList = notificationRepository.findAllByUserAndIsReadFalse(user);

        List<NotificationUpdateReadResponseDto> result = new ArrayList<>();

        for (Notification notification : notificationList) {
            notification.updateRead();
            result.add(NotificationUpdateReadResponseDto.fromNotification(notification));
        }
        String key = "notifications:" + userId;
        redisTemplate.delete(key);

        return result;
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

    public void handleVacancyNotification(User user, Long storeId) {
        if (storeId == null) {
            throw new HandledException(ErrorCode.NOTIFICATION_BAD_REQUEST);
        }

        Store store = storeService.getStore(storeId);

        Waitlist waitlist = waitlistRepository
            .findByUserAndStoreAndIsNotifiedFalse(user, store)
            .orElseThrow(() -> new HandledException(ErrorCode.WAITLIST_NOT_FOUND));

        waitlist.updateNotified();
    }

    // 전체 알림 리스트를 페이징된 Page 객체로 변환
    private Page<NotificationResponseDto> toPage(List<NotificationResponseDto> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        List<NotificationResponseDto> sublist = list.subList(start, end);
        return new PageImpl<>(sublist, pageable, list.size());
    }

}