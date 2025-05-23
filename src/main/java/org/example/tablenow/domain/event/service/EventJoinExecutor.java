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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.example.tablenow.global.constant.RedisKeyConstants.EVENT_JOIN_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventJoinExecutor {

    private final StringRedisTemplate redisTemplate;
    private final EventRepository eventRepository;
    private final EventJoinRepository eventJoinRepository;

    @Transactional
    public EventJoinResponseDto execute(Long eventId, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        String userId = String.valueOf(user.getId());
        String joinKey = EVENT_JOIN_PREFIX + eventId;

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));

        event.validateOpenStatus();

        Boolean added = redisTemplate.opsForZSet().add(joinKey, userId, System.currentTimeMillis());
        if (Boolean.FALSE.equals(added)) {
            throw new HandledException(ErrorCode.EVENT_ALREADY_JOINED);
        }

        Long rank = redisTemplate.opsForZSet().rank(joinKey, userId);
        if (rank == null || rank >= event.getLimitPeople()) {
            redisTemplate.opsForZSet().remove(joinKey, userId);
            throw new HandledException(ErrorCode.EVENT_FULL);
        }

        try {
            EventJoin eventJoin = saveEventJoin(event, user);
            log.info("이벤트 신청 성공: eventJoinId={}, user={}, event={}", eventJoin.getId(), user.getEmail(), eventId);
            return EventJoinResponseDto.fromEventJoin(eventJoin);
        } catch (Exception e) {
            redisTemplate.opsForZSet().remove(joinKey, String.valueOf(user.getId()));
            log.warn("DB insert 실패로 Redis 자리 반환: userId={}, eventId={}", user.getId(), eventId);
            throw e;
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
}

