package org.example.tablenow.domain.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.event.dto.response.EventJoinResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.entity.EventJoin;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.event.repository.EventJoinRepository;
import org.example.tablenow.domain.event.repository.EventRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventJoinService {

    private final EventJoinRepository eventJoinRepository;
    private final EventRepository eventRepository;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String EVENT_JOIN_PREFIX = "event:join:";
    private static final String EVENT_LOCK_PREFIX = "lock:event:";
    private static final long LOCK_WAIT_TIME = 3L;
    private static final long LOCK_LEASE_TIME = 3L;

    @Transactional
    public EventJoinResponseDto joinEvent(Long eventId, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.OPENED) {
            throw new HandledException(ErrorCode.EVENT_NOT_OPENED);
        }

        if (eventJoinRepository.existsByUserAndEvent(user, event)) {
            throw new HandledException(ErrorCode.EVENT_ALREADY_JOINED);
        }

        if (eventJoinRepository.countByEvent(event) >= event.getLimitPeople()) {
            throw new HandledException(ErrorCode.EVENT_FULL);
        }

        EventJoin eventJoin = EventJoin.builder()
                .user(user)
                .event(event)
                .build();
        eventJoinRepository.save(eventJoin);

        return EventJoinResponseDto.fromEventJoin(eventJoin);
    }

    @Transactional
    public EventJoinResponseDto joinEventV2(Long eventId, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.OPENED) {
            throw new HandledException(ErrorCode.EVENT_NOT_OPENED);
        }

        if (eventJoinRepository.existsByUserAndEvent(user, event)) {
            throw new HandledException(ErrorCode.EVENT_ALREADY_JOINED);
        }

        if (eventJoinRepository.countByEvent(event) >= event.getLimitPeople()) {
            throw new HandledException(ErrorCode.EVENT_FULL);
        }

        EventJoin eventJoin = EventJoin.builder()
                .user(user)
                .event(event)
                .build();
        eventJoinRepository.save(eventJoin);

        return EventJoinResponseDto.fromEventJoin(eventJoin);
    }

    public EventJoinResponseDto joinEventV3(Long eventId, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);

        String zsetKey = EVENT_JOIN_PREFIX + eventId;
        String lockKey = EVENT_LOCK_PREFIX + eventId;

        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("락 획득 실패: {}", lockKey);
                throw new IllegalStateException("이벤트 신청 대기 중 시간이 초과되었습니다.");
            }

            log.info("락 획득 성공: {}", lockKey);

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));
            int limit = event.getLimitPeople();

            // 현재 인원 확인
            Long current = redisTemplate.opsForZSet().zCard(zsetKey);
            if (current != null && current >= limit) {
                throw new HandledException(ErrorCode.EVENT_FULL);
            }

            // 중복 신청 방지
            Boolean added = redisTemplate.opsForZSet().add(zsetKey, user.getId(), System.currentTimeMillis());
            if (Boolean.FALSE.equals(added)) {
                throw new HandledException(ErrorCode.EVENT_ALREADY_JOINED);
            }

            // TTL 설정 (최초 1회만 적용됨)
            redisTemplate.expire(zsetKey, Duration.ofHours(1));

            // DB 저장
            EventJoin eventJoin = saveEventJoin(event, user);

            log.info("이벤트 신청 성공: user={}, event={}", user.getEmail(), eventId);
            return EventJoinResponseDto.fromEventJoin(eventJoin);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("인터럽트 발생", e);
        } catch (Exception e) {
            log.error("이벤트 신청 중 예외 발생", e);
            throw new RuntimeException("이벤트 신청 중 오류 발생", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("락 해제: {}", lockKey);
            }
        }
    }

    @Transactional
    public EventJoin saveEventJoin(Event event, User user) {
        EventJoin eventJoin = EventJoin.builder()
                .user(user)
                .event(event)
                .build();

        return eventJoinRepository.save(eventJoin);
    }

    public List<User> getUsersByEventId(Long id) {
        return eventJoinRepository.findUsersByEventId(id);
    }
}
