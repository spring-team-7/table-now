package org.example.tablenow.domain.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.dto.request.EventUpdateRequestDto;
import org.example.tablenow.domain.event.dto.response.EventCloseResponseDto;
import org.example.tablenow.domain.event.dto.response.EventDeleteResponseDto;
import org.example.tablenow.domain.event.dto.response.EventResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.event.message.dto.EventOpenMessage;
import org.example.tablenow.domain.event.message.producer.EventOpenProducer;
import org.example.tablenow.domain.event.repository.EventRepository;
import org.example.tablenow.domain.event.service.EventService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private StoreService storeService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private EventOpenProducer eventOpenProducer;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventService eventService;

    private final LocalDateTime baseTime = LocalDateTime.of(2025, 4, 30, 0, 0);

    private Event createEvent(Long id, Store store, EventStatus status,
                              LocalDateTime openAt, LocalDateTime eventTime) {

        EventRequestDto dto = new EventRequestDto(
                store.getId(),
                "테스트 이벤트",
                openAt.minusDays(1),
                openAt,
                eventTime,
                10
        );

        Event event = Event.create(store, dto);
        ReflectionTestUtils.setField(event, "id", id);
        ReflectionTestUtils.setField(event, "status", status);
        return event;
    }

    private Store createStore(Long id, String name) {
        return Store.builder()
                .id(id)
                .name(name)
                .build();
    }

    @Nested
    class 이벤트_생성 {

        private EventRequestDto createEventRequestDto(Long storeId, LocalDateTime openAt, LocalDateTime eventTime) {
            return new EventRequestDto(
                    storeId,
                    "content",
                    openAt.minusDays(1),
                    openAt,
                    eventTime,
                    10
            );
        }

        @Test
        void 동일한_이벤트가_있는_경우_예외_발생() {
            // given
            Store store = createStore(1L, "테스트가게");
            EventRequestDto dto = createEventRequestDto(store.getId(), baseTime.plusHours(10), baseTime.plusHours(18));

            given(storeService.getStore(anyLong())).willReturn(store);
            given(eventRepository.existsByStore_IdAndEventTime(anyLong(), any())).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventService.createEvent(dto)
            );
            assertEquals(ErrorCode.EVENT_ALREADY_EXISTS.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이벤트_생성_성공() throws JsonProcessingException {
            // given
            Store store = createStore(1L, "테스트가게");
            EventRequestDto dto = createEventRequestDto(store.getId(), baseTime.plusHours(10), baseTime.plusHours(18));
            given(storeService.getStore(anyLong())).willReturn(store);
            given(eventRepository.existsByStore_IdAndEventTime(anyLong(), any())).willReturn(false);
            given(eventRepository.save(any(Event.class))).willAnswer(invocation -> {
                Event e = invocation.getArgument(0);
                ReflectionTestUtils.setField(e, "openAt", LocalDateTime.of(2025, 4, 30, 10, 0));
                return e;
            });
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(objectMapper.writeValueAsString(any())).willReturn("{\"eventId\":1}");

            // when
            EventResponseDto response = eventService.createEvent(dto);

            // then
            assertNotNull(response);
            assertEquals(store.getId(), response.getStoreId());
            assertEquals(dto.getLimitPeople(), response.getLimitPeople());
            verify(zSetOperations).add(eq("event:open:zset"), anyString(), anyDouble());
        }
    }

    @Nested
    class 이벤트_수정 {

        @Test
        void 이벤트_상태가_READY가_아닐_경우_예외_발생() {
            // given
            Long eventId = 1L;
            Store store = createStore(10L, "수정불가 매장");
            LocalDateTime openAt = baseTime.plusHours(10);
            LocalDateTime eventTime = baseTime.plusHours(18);
            Event event = createEvent(eventId, store, EventStatus.CLOSED, openAt, eventTime);

            EventUpdateRequestDto dto = new EventUpdateRequestDto(
                    openAt.plusDays(1),
                    eventTime.plusDays(1),
                    20
            );
            given(eventRepository.findById(eq(eventId))).willReturn(Optional.of(event));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventService.updateEvent(eventId, dto)
            );

            assertEquals(ErrorCode.INVALID_EVENT_STATUS.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이벤트_수정_성공() throws JsonProcessingException {
            // given
            Long eventId = 2L;
            Store store = createStore(20L, "정상 수정 매장");
            LocalDateTime openAt = baseTime.plusHours(10);
            LocalDateTime eventTime = baseTime.plusHours(18);
            Event event = createEvent(eventId, store, EventStatus.READY, openAt, eventTime);

            EventUpdateRequestDto dto = new EventUpdateRequestDto(
                    openAt.plusDays(1),
                    eventTime.plusDays(1),
                    20
            );

            given(eventRepository.findById(eq(eventId))).willReturn(Optional.of(event));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.add(anyString(), anyString(), anyDouble())).willReturn(true);
            given(objectMapper.writeValueAsString(any())).willReturn("{\"eventId\":1}");

            // when
            EventResponseDto response = eventService.updateEvent(eventId, dto);

            // then
            assertNotNull(response);
            assertEquals(dto.getOpenAt(), response.getOpenAt());
            assertEquals(dto.getEventTime(), response.getEventTime());
            assertEquals(dto.getLimitPeople(), response.getLimitPeople());
            verify(zSetOperations).add(anyString(), anyString(), anyDouble());
        }
    }

    @Nested
    class 이벤트_조회 {
        int page = 1;
        int size = 10;

        @Test
        void 상태값이_있는_경우_조회_성공() {
            // given
            Store store = createStore(1L, "조회 가게");
            LocalDateTime openAt = baseTime.plusHours(10);
            LocalDateTime eventTime = baseTime.plusHours(18);
            Event event = createEvent(1L, store, EventStatus.READY, openAt, eventTime);

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
                    () -> assertEquals(store.getId(), dto.getStoreId()),
                    () -> assertEquals(event.getEventTime(), dto.getEventTime()),
                    () -> assertEquals(event.getOpenAt(), dto.getOpenAt()),
                    () -> assertEquals(event.getLimitPeople(), dto.getLimitPeople()),
                    () -> assertEquals(event.getStatus(), dto.getStatus())
            );
        }

        @Test
        void 상태값이_없는_경우_조회_성공() {
            // given
            Store store = createStore(2L, "조회 가게 2");
            LocalDateTime openAt = baseTime.plusHours(11);
            LocalDateTime eventTime = baseTime.plusHours(19);
            Event event = createEvent(2L, store, EventStatus.READY, openAt, eventTime);

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
                    () -> assertEquals(store.getId(), dto.getStoreId()),
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
            Long eventId = 1L;
            Store store = createStore(1L, "삭제불가 가게");
            Event event = createEvent(eventId, store, EventStatus.OPENED,
                    baseTime.plusHours(10), baseTime.plusHours(18));

            given(eventRepository.findById(eq(eventId))).willReturn(Optional.of(event));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    eventService.deleteEvent(eventId)
            );

            assertEquals(ErrorCode.INVALID_EVENT_STATUS.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 이벤트_삭제_성공() throws JsonProcessingException {
            // given
            Long eventId = 2L;
            Store store = createStore(2L, "삭제가능 가게");
            Event event = createEvent(eventId, store, EventStatus.READY,
                    baseTime.plusHours(10), baseTime.plusHours(18));

            given(eventRepository.findById(eq(eventId))).willReturn(Optional.of(event));
            willDoNothing().given(eventRepository).delete(eq(event));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(objectMapper.writeValueAsString(any())).willReturn("{\"eventId\":2}");

            // when
            EventDeleteResponseDto response = eventService.deleteEvent(eventId);

            // then
            assertNotNull(response);
            assertAll(
                    () -> assertEquals(event.getId(), response.getEventId()),
                    () -> assertEquals(store.getId(), response.getStoreId()),
                    () -> assertEquals("이벤트 삭제에 성공했습니다.", response.getMessage())
            );
            verify(redisTemplate).delete("event:join:" + eventId);
            verify(zSetOperations).remove(eq("event:open:zset"), anyString());
            verify(eventRepository).delete(event);
        }
    }

    @Nested
    class 이벤트_오픈_처리 {

        @Test
        void 이벤트_오픈_성공() throws JsonProcessingException {
            // given
            Store store = createStore(1L, "가게A");
            LocalDateTime openAt = baseTime.plusHours(10);
            LocalDateTime eventTime = baseTime.plusHours(18);

            Event event1 = createEvent(1L, store, EventStatus.READY, openAt, eventTime);
            Event event2 = createEvent(2L, store, EventStatus.READY, openAt, eventTime);

            EventOpenMessage message1 = EventOpenMessage.fromEvent(event1);
            EventOpenMessage message2 = EventOpenMessage.fromEvent(event2);

            String json1 = "{\"eventId\":1}";
            String json2 = "{\"eventId\":2}";

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.rangeByScore(eq("event:open:zset"), anyDouble(), anyDouble()))
                    .willReturn(Set.of(json1, json2));
            given(objectMapper.readValue(eq(json1), eq(EventOpenMessage.class))).willReturn(message1);
            given(objectMapper.readValue(eq(json2), eq(EventOpenMessage.class))).willReturn(message2);

            given(eventRepository.findById(1L)).willReturn(Optional.of(event1));
            given(eventRepository.findById(2L)).willReturn(Optional.of(event2));

            // when
            eventService.openEventsIfDue();

            // then
            assertAll(
                    () -> assertEquals(EventStatus.OPENED, event1.getStatus()),
                    () -> assertEquals(EventStatus.OPENED, event2.getStatus())
            );

            verify(eventOpenProducer, times(2)).send(any(EventOpenMessage.class));
            verify(zSetOperations, times(1)).remove("event:open:zset", json1);
            verify(zSetOperations, times(1)).remove("event:open:zset", json2);
        }

        @Test
        void 상태가_READY가_아닌_이벤트는_오픈되지_않음() throws JsonProcessingException {
            // given
            Store store = createStore(2L, "가게B");
            Event notReadyEvent = createEvent(3L, store, EventStatus.CLOSED,
                    baseTime.plusHours(10), baseTime.plusHours(18));

            String json = "{\"eventId\":3}";
            EventOpenMessage message = EventOpenMessage.fromEvent(notReadyEvent);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.rangeByScore(eq("event:open:zset"), anyDouble(), anyDouble()))
                    .willReturn(Set.of(json));
            given(objectMapper.readValue(eq(json), eq(EventOpenMessage.class))).willReturn(message);
            given(eventRepository.findById(3L)).willReturn(Optional.of(notReadyEvent));

            // when
            eventService.openEventsIfDue();

            // then
            assertEquals(EventStatus.CLOSED, notReadyEvent.getStatus());

            verify(eventOpenProducer, never()).send(any());
            verify(zSetOperations, never()).remove(anyString(), anyString());
        }

        @Test
        void 오픈시간_도래한_이벤트가_비어있으면_아무작업도_하지_않음() {
            // given
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.rangeByScore(eq("event:open:zset"), anyDouble(), anyDouble()))
                    .willReturn(Collections.emptySet());

            // when
            eventService.openEventsIfDue();

            // then
            verify(eventRepository, never()).findById(anyLong());
            verify(eventOpenProducer, never()).send(any());
            verify(zSetOperations, never()).remove(anyString(), anyString());
        }

        @Test
        void 오픈시간_도래한_이벤트가_null이면_아무작업도_하지_않음() {
            // given
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.rangeByScore(eq("event:open:zset"), anyDouble(), anyDouble())).willReturn(null);

            // when
            eventService.openEventsIfDue();

            // then
            verify(eventRepository, never()).findById(anyLong());
            verify(eventOpenProducer, never()).send(any());
            verify(zSetOperations, never()).remove(anyString(), anyString());
        }
    }

    @Nested
    class 이벤트_마감 {

        @Test
        void 이벤트_마감_성공() throws JsonProcessingException {
            // given
            Long eventId = 1L;
            Store store = createStore(1L, "마감 가게");
            Event event = createEvent(eventId, store, EventStatus.OPENED,
                    baseTime.plusHours(10), baseTime.plusHours(18));

            given(eventRepository.findById(eq(eventId))).willReturn(Optional.of(event));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(objectMapper.writeValueAsString(any())).willReturn("{\"eventId\":1}");

            // when
            EventCloseResponseDto response = eventService.closeEvent(eventId);

            // then
            assertNotNull(response);
            assertEquals(EventStatus.CLOSED, event.getStatus());
            assertEquals(eventId, response.getEventId());
            verify(redisTemplate).delete("event:join:" + eventId);
            verify(zSetOperations).remove(anyString(), anyString());
        }
    }

}
