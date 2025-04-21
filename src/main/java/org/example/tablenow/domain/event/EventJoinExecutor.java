package org.example.tablenow.domain.event;

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventJoinExecutor {

    private final RedisTemplate<String, String> redisTemplate;
    private final EventRepository eventRepository;
    private final EventJoinRepository eventJoinRepository;
    private static final String EVENT_JOIN_PREFIX = "event:join:";

    @Transactional
    public EventJoinResponseDto execute(Long eventId, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        String userId = String.valueOf(user.getId());
        String zsetKey = "event:join:" + eventId;

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));

        event.validateOpenStatus();

        Boolean added = redisTemplate.opsForZSet().add(zsetKey, userId, System.currentTimeMillis());
        if (Boolean.FALSE.equals(added)) {
            throw new HandledException(ErrorCode.EVENT_ALREADY_JOINED);
        }

        Long rank = redisTemplate.opsForZSet().rank(zsetKey, userId);
        if (rank == null || rank >= event.getLimitPeople()) {
            redisTemplate.opsForZSet().remove(zsetKey, userId);
            throw new HandledException(ErrorCode.EVENT_FULL);
        }

        try {
            EventJoin eventJoin = saveEventJoin(event, user);
            log.info("이벤트 신청 성공: eventJoinId={}, user={}, event={}", eventJoin.getId(), user.getEmail(), eventId);
            return EventJoinResponseDto.fromEventJoin(eventJoin);
        } catch (Exception e) {
            redisTemplate.opsForZSet().remove(zsetKey, String.valueOf(user.getId()));
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

