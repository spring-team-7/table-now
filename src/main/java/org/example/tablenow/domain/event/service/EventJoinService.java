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
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventJoinService {

    private final EventJoinRepository eventJoinRepository;
    private final EventRepository eventRepository;

    private static final String EVENT_LOCK_PREFIX = "lock:event:";
    private final RedissonClient redissonClient;

    @Transactional
    public EventJoinResponseDto joinEvent(Long eventId, AuthUser authUser) {
        if (authUser == null) { // 테스트용
            long randomId = ThreadLocalRandom.current().nextLong(1, 201);
            authUser = new AuthUser(randomId, "user" + randomId + "@test.com", UserRole.ROLE_USER, "닉네임" + randomId);
        }
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
//        if (authUser == null) { // 테스트용
//            long randomId = ThreadLocalRandom.current().nextLong(1, 201);
//            authUser = new AuthUser(randomId, "user" + randomId + "@test.com", UserRole.ROLE_USER, "닉네임" + randomId);
//        }
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

    @Transactional
    public EventJoinResponseDto joinEventV3(Long eventId, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);

        String lockKey = EVENT_LOCK_PREFIX + eventId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(3, 3, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("락 획득 실패: {}", lockKey);
                throw new IllegalStateException("이벤트 신청 대기 중 시간이 초과되었습니다.");
            }

            log.info("락 획득 성공: {}", lockKey);

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));

            if (event.getStatus() != EventStatus.OPENED) {
                throw new HandledException(ErrorCode.EVENT_NOT_OPENED);
            }

            if (eventJoinRepository.existsByUserAndEvent(user, event)) {
                throw new HandledException(ErrorCode.EVENT_ALREADY_JOINED);
            }

            long currentCount = eventJoinRepository.countByEvent(event);
            if (currentCount >= event.getLimitPeople()) {
                throw new HandledException(ErrorCode.EVENT_FULL);
            }

            EventJoin eventJoin = EventJoin.builder()
                    .user(user)
                    .event(event)
                    .build();

            eventJoinRepository.save(eventJoin);

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

    public List<User> getUsersByEventId(Long id) {
        return eventJoinRepository.findUsersByEventId(id);
    }
}
