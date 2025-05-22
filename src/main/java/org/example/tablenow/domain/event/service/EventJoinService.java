package org.example.tablenow.domain.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.event.dto.response.EventJoinResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.entity.EventJoin;
import org.example.tablenow.domain.event.repository.EventJoinRepository;
import org.example.tablenow.domain.event.repository.EventRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.annotation.DistributedLock;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.tablenow.global.constant.RedisKeyConstants.EVENT_LOCK_KEY_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventJoinService {

    private final EventJoinRepository eventJoinRepository;
    private final EventRepository eventRepository;
    private final EventJoinExecutor eventJoinExecutor;

    public EventJoinResponseDto joinEventWithoutLock(Long eventId, AuthUser authUser) {
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

    public EventJoinResponseDto joinEventWithDBLock(Long eventId, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));

        event.validateOpenStatus();

        if (eventJoinRepository.existsByUserAndEvent(user, event)) {
            throw new HandledException(ErrorCode.EVENT_ALREADY_JOINED);
        }

        event.decreaseAvailableSeats();
        eventRepository.save(event);

        EventJoin eventJoin = EventJoin.builder()
                .user(user)
                .event(event)
                .build();
        eventJoinRepository.save(eventJoin);

        return EventJoinResponseDto.fromEventJoin(eventJoin);
    }

    @DistributedLock(
            prefix = EVENT_LOCK_KEY_PREFIX,
            key = "#eventId"
    )
    public EventJoinResponseDto joinEventWithRedissonLock(Long eventId, AuthUser authUser) {
        return eventJoinExecutor.execute(eventId, authUser);
    }
}
