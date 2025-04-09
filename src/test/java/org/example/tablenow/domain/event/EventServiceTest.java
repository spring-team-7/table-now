package org.example.tablenow.domain.event;

import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.dto.request.EventUpdateRequestDto;
import org.example.tablenow.domain.event.dto.response.EventDeleteResponseDto;
import org.example.tablenow.domain.event.dto.response.EventResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.event.repository.EventRepository;
import org.example.tablenow.domain.event.service.EventService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private StoreService storeService;

    @InjectMocks
    private EventService eventService;

    Long eventId = 1L;
    Long storeId = 1L;

    Store store;
    LocalDateTime openAt;
    LocalDateTime eventTime;

    @BeforeEach
    void setUp() {
        store = Store.builder()
                .id(storeId)
                .name("이벤트 가게")
                .build();

        openAt = LocalDateTime.of(2025, 4, 30, 10, 0);
        eventTime = LocalDateTime.of(2025, 4, 30, 18, 0);
    }

    private Event createEvent(Long id, EventStatus status) {
        EventRequestDto dto = EventRequestDto.builder()
                .storeId(storeId)
                .openAt(openAt)
                .eventTime(eventTime)
                .limitPeople(10)
                .build();

        Event event = Event.create(store, dto);
        ReflectionTestUtils.setField(event, "id", id);
        ReflectionTestUtils.setField(event, "status", status);
        return event;
    }

    @Nested
    class 이벤트_생성 {
        EventRequestDto dto = EventRequestDto.builder()
                .storeId(storeId)
                .openAt(openAt)
                .eventTime(eventTime)
                .limitPeople(10)
                .build();

        @Test
        void 동일한_이벤트가_있는_경우_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(eventRepository.existsByStoreIdAndEventTime(anyLong(), any())).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventService.createEvent(dto)
            );
            assertEquals(ErrorCode.EVENT_ALREADY_EXISTS.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이벤트_생성_성공() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(eventRepository.existsByStoreIdAndEventTime(anyLong(), any())).willReturn(false);
            given(eventRepository.save(any(Event.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            EventResponseDto response = eventService.createEvent(dto);

            // then
            assertNotNull(response);
            assertEquals(storeId, response.getStoreId());
            assertEquals(dto.getLimitPeople(), response.getLimitPeople());
        }
    }

    @Nested
    class 이벤트_수정 {
        EventUpdateRequestDto dto;

        @BeforeEach
        void setUp() {
            dto = EventUpdateRequestDto.builder()
                    .openAt(openAt.plusDays(1))
                    .eventTime(eventTime.plusDays(1))
                    .limitPeople(20)
                    .build();
        }

        @Test
        void 이벤트_상태가_READY가_아닐_경우_예외_발생() {
            // given
            Event event = createEvent(eventId, EventStatus.OPENED);
            given(eventRepository.findById(eq(eventId))).willReturn(Optional.of(event));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventService.updateEvent(eventId, dto)
            );

            assertEquals(ErrorCode.INVALID_EVENT_STATUS.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이벤트_수정_성공() {
            // given
            Event event = createEvent(eventId, EventStatus.READY);
            given(eventRepository.findById(eq(eventId))).willReturn(Optional.of(event));

            // when
            EventResponseDto response = eventService.updateEvent(eventId, dto);

            // then
            assertNotNull(response);
            assertEquals(dto.getOpenAt(), response.getOpenAt());
            assertEquals(dto.getEventTime(), response.getEventTime());
            assertEquals(dto.getLimitPeople(), response.getLimitPeople());
        }
    }

    @Nested
    class 이벤트_조회 {
        int page = 1;
        int size = 10;

        @Test
        void 상태값이_있는_경우_조회_성공() {
            // given
            Event event = createEvent(1L, EventStatus.READY);
            Page<Event> result = new PageImpl<>(List.of(event));

            given(eventRepository.findByStatus(eq(EventStatus.READY), any(Pageable.class)))
                    .willReturn(result);

            // when
            Page<EventResponseDto> response = eventService.getEvents(EventStatus.READY, page, size);

            // then
            assertNotNull(response);
            assertEquals(1, response.getTotalElements());

            EventResponseDto dto = response.getContent().get(0);
            assertAll(
                    () -> assertEquals(event.getId(), dto.getEventId()),
                    () -> assertEquals(storeId, dto.getStoreId()),
                    () -> assertEquals(event.getEventTime(), dto.getEventTime()),
                    () -> assertEquals(event.getOpenAt(), dto.getOpenAt()),
                    () -> assertEquals(event.getLimitPeople(), dto.getLimitPeople()),
                    () -> assertEquals(event.getStatus(), dto.getStatus())
            );
        }

        @Test
        void 상태값이_없는_경우_조회_성공() {
            // given
            Event event = createEvent(1L, EventStatus.READY);
            Page<Event> result = new PageImpl<>(List.of(event));

            given(eventRepository.findAll(any(Pageable.class))).willReturn(result);

            // when
            Page<EventResponseDto> response = eventService.getEvents(null, page, size);

            // then
            assertNotNull(response);
            assertEquals(1, response.getTotalElements());

            EventResponseDto dto = response.getContent().get(0);
            assertAll(
                    () -> assertEquals(event.getId(), dto.getEventId()),
                    () -> assertEquals(storeId, dto.getStoreId()),
                    () -> assertEquals(event.getEventTime(), dto.getEventTime()),
                    () -> assertEquals(event.getOpenAt(), dto.getOpenAt()),
                    () -> assertEquals(event.getLimitPeople(), dto.getLimitPeople()),
                    () -> assertEquals(event.getStatus(), dto.getStatus())
            );
        }
    }

    @Nested
    class 이벤트_삭제 {

        @Test
        void 이벤트_상태가_READY가_아닐_경우_예외_발생() {
            // given
            Event event = createEvent(eventId, EventStatus.OPENED);
            given(eventRepository.findById(eq(eventId))).willReturn(Optional.of(event));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventService.deleteEvent(eventId)
            );

            assertEquals(ErrorCode.INVALID_EVENT_STATUS.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이벤트_삭제_성공() {
            // given
            Event event = createEvent(eventId, EventStatus.READY);
            given(eventRepository.findById(eq(eventId))).willReturn(Optional.of(event));
            willDoNothing().given(eventRepository).delete(eq(event));

            // when
            EventDeleteResponseDto response = eventService.deleteEvent(eventId);

            // then
            assertNotNull(response);
            assertAll(
                    () -> assertEquals(event.getId(), response.getEventId()),
                    () -> assertEquals(storeId, response.getStoreId()),
                    () -> assertEquals("이벤트 삭제에 성공했습니다.", response.getMessage())
            );
        }
    }

    @Nested
    class 이벤트_오픈_처리 {

        @Test
        void 이벤트_오픈_성공() {
            // given
            Event event1 = createEvent(1L, EventStatus.READY);
            Event event2 = createEvent(2L, EventStatus.READY);
            List<Event> events = List.of(event1, event2);

            given(eventRepository.findAllByStatusAndOpenAtLessThanEqual(
                    eq(EventStatus.READY), any(LocalDateTime.class))
            ).willReturn(events);

            // when
            eventService.openEventsIfDue();

            // then
            assertAll(
                    () -> assertEquals(EventStatus.OPENED, event1.getStatus()),
                    () -> assertEquals(EventStatus.OPENED, event2.getStatus())
            );
        }
    }

}
