package org.example.tablenow.domain.event.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.event.dto.response.EventJoinResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.entity.EventJoin;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.event.repository.EventJoinRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EventJoinService {

    private final EventJoinRepository eventJoinRepository;
    private final EventService eventService;

    @Transactional
    public EventJoinResponseDto joinEvent(Long eventId, AuthUser authUser) {
        if (authUser == null) { // 테스트용
            long randomId = ThreadLocalRandom.current().nextLong(1, 201);
            authUser = new AuthUser(randomId, "user" + randomId + "@test.com", UserRole.ROLE_USER, "닉네임" + randomId);
        }
        User user = User.fromAuthUser(authUser);
        Event event = eventService.getEvent(eventId);

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
        if (authUser == null) { // 테스트용
            long randomId = ThreadLocalRandom.current().nextLong(1, 201);
            authUser = new AuthUser(randomId, "user" + randomId + "@test.com", UserRole.ROLE_USER, "닉네임" + randomId);
        }
        User user = User.fromAuthUser(authUser);
        Event event = eventService.getEventForUpdate(eventId);

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
}
