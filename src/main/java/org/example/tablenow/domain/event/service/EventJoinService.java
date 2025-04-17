package org.example.tablenow.domain.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.event.dto.response.EventJoinResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.entity.EventJoin;
import org.example.tablenow.domain.event.repository.EventJoinRepository;
import org.example.tablenow.domain.event.repository.EventRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

    private static final String EVENT_JOIN_PREFIX = "event:join:";
    private static final String EVENT_LOCK_PREFIX = "lock:event:";
    private static final long LOCK_WAIT_TIME = 3L;
    private static final long LOCK_LEASE_TIME = 3L;

    @Transactional
    public EventJoinResponseDto joinEvent(Long eventId, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));

        event.validateOpenStatus();

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

        event.validateOpenStatus();

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
        String lockKey = EVENT_LOCK_PREFIX + eventId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS)) {
                log.warn("락 획득 실패: {}", lockKey);
                throw new HandledException(ErrorCode.EVENT_LOCK_TIMEOUT);
            }

            return handleJoinLogic(eventId, authUser);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("인터럽트 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("이벤트 신청 중 오류 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private EventJoinResponseDto handleJoinLogic(Long eventId, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        String zsetKey = EVENT_JOIN_PREFIX + eventId;

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));

        event.validateOpenStatus();
        validateEventNotAlreadyJoined(zsetKey, user);
        validateEventCapacity(zsetKey, event.getLimitPeople());

        EventJoin eventJoin = saveEventJoin(event, user);

        log.info("이벤트 신청 성공: eventJoinId={}, user={}, event={}", eventJoin.getId(), user.getEmail(), eventId);
        return EventJoinResponseDto.fromEventJoin(eventJoin);
    }

    private void validateEventNotAlreadyJoined(String zsetKey, User user) {
        Boolean added = redisTemplate.opsForZSet().add(zsetKey, String.valueOf(user.getId()), System.currentTimeMillis());
        if (Boolean.FALSE.equals(added)) {
            throw new HandledException(ErrorCode.EVENT_ALREADY_JOINED);
        }
        redisTemplate.expire(zsetKey, Duration.ofHours(1));
    }

    private void validateEventCapacity(String zsetKey, int limit) {
        Long current = redisTemplate.opsForZSet().zCard(zsetKey);
        if (current != null && current >= limit) {
            throw new HandledException(ErrorCode.EVENT_FULL);
        }
    }

    @Transactional
    protected EventJoin saveEventJoin(Event event, User user) {
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
