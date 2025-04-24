package org.example.tablenow.domain.event;

import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.dto.response.EventJoinResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.entity.EventJoin;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.event.repository.EventJoinRepository;
import org.example.tablenow.domain.event.repository.EventRepository;
import org.example.tablenow.domain.event.service.EventJoinExecutor;
import org.example.tablenow.domain.event.service.EventJoinService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class EventJoinServiceTest {

    @Mock
    private EventJoinRepository eventJoinRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventJoinExecutor eventJoinExecutor;

    @InjectMocks
    private EventJoinService eventJoinService;

    Long eventId = 1L;
    Long userId = 1L;
    AuthUser authUser = new AuthUser(userId, "user@test.com", UserRole.ROLE_USER, "일반회원");
    User user = User.fromAuthUser(authUser);

    Store store;
    Event event;

    @BeforeEach
    void setUp() {
        store = Store.builder()
                .id(100L)
                .name("테스트 가게")
                .capacity(10)
                .build();

        event = createEvent(eventId, EventStatus.OPENED);
    }

    private Event createEvent(Long id, EventStatus status) {
        EventRequestDto dto = EventRequestDto.builder()
                .storeId(store.getId())
                .openAt(LocalDateTime.now().minusDays(1))
                .eventTime(LocalDateTime.now().plusDays(1))
                .limitPeople(10)
                .build();

        Event event = Event.create(store, dto);
        ReflectionTestUtils.setField(event, "id", id);
        ReflectionTestUtils.setField(event, "status", status);
        return event;
    }

    @Nested
    class 락_없는_이벤트_참여 {

        @Test
        void 이벤트가_오픈되지_않은_경우_예외_발생() {
            // given
            ReflectionTestUtils.setField(event, "status", EventStatus.READY);
            given(eventRepository.findById(eventId)).willReturn(Optional.of(event));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventJoinService.joinEventWithoutLock(eventId, authUser)
            );

            assertEquals(ErrorCode.EVENT_NOT_OPENED.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이미_참여한_경우_예외_발생() {
            // given
            given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
            given(eventJoinRepository.existsByUserAndEvent(any(User.class), any(Event.class))).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventJoinService.joinEventWithoutLock(eventId, authUser)
            );

            assertEquals(ErrorCode.EVENT_ALREADY_JOINED.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 인원이_마감된_경우_예외_발생() {
            // given
            given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
            given(eventJoinRepository.existsByUserAndEvent(any(User.class), any(Event.class))).willReturn(false);
            given(eventJoinRepository.countByEvent(event)).willReturn(10); // 인원 가득 참

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventJoinService.joinEventWithoutLock(eventId, authUser)
            );

            assertEquals(ErrorCode.EVENT_FULL.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이벤트_참여_성공() {
            // given
            given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
            given(eventJoinRepository.existsByUserAndEvent(any(User.class), any(Event.class))).willReturn(false);
            given(eventJoinRepository.countByEvent(event)).willReturn(3);
            given(eventJoinRepository.save(any(EventJoin.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            EventJoinResponseDto response = eventJoinService.joinEventWithoutLock(eventId, authUser);

            // then
            assertNotNull(response);
            assertAll(
                    () -> assertEquals(eventId, response.getEventId()),
                    () -> assertEquals(store.getId(), response.getStoreId())
            );
        }
    }

    @Nested
    class DB락_기반_이벤트_참여 {

        @Test
        void 이미_참여한_경우_예외_발생() {
            // given
            given(eventRepository.findByIdForUpdate(eventId)).willReturn(Optional.of(event));
            given(eventJoinRepository.existsByUserAndEvent(any(User.class), any(Event.class))).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventJoinService.joinEventWithDBLock(eventId, authUser)
            );

            assertEquals(ErrorCode.EVENT_ALREADY_JOINED.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 인원이_마감된_경우_예외_발생() {
            // given
            given(eventRepository.findByIdForUpdate(eventId)).willReturn(Optional.of(event));
            given(eventJoinRepository.existsByUserAndEvent(any(User.class), any(Event.class))).willReturn(false);
            given(eventJoinRepository.countByEvent(event)).willReturn(10); // 인원 가득 참

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventJoinService.joinEventWithDBLock(eventId, authUser)
            );

            assertEquals(ErrorCode.EVENT_FULL.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 참여_성공() {
            // given
            given(eventRepository.findByIdForUpdate(eventId)).willReturn(Optional.of(event));
            given(eventJoinRepository.existsByUserAndEvent(any(User.class), any(Event.class))).willReturn(false);
            given(eventJoinRepository.countByEvent(event)).willReturn(2);
            given(eventJoinRepository.save(any(EventJoin.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            EventJoinResponseDto response = eventJoinService.joinEventWithDBLock(eventId, authUser);

            // then
            assertNotNull(response);
            assertEquals(eventId, response.getEventId());
        }
    }

    @Nested
    class Redisson락_기반_이벤트_참여 {

        @Test
        void 참여_성공() {
            // given
            EventJoinResponseDto expected = EventJoinResponseDto.fromEventJoin(
                    EventJoin.builder().user(user).event(event).build()
            );
            given(eventJoinExecutor.execute(eventId, authUser)).willReturn(expected);

            // when
            EventJoinResponseDto response = eventJoinService.joinEventWithRedissonLock(eventId, authUser);

            // then
            assertNotNull(response);
            assertEquals(expected.getEventId(), response.getEventId());
        }
    }
}
