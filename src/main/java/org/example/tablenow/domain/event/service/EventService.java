package org.example.tablenow.domain.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.example.tablenow.global.constant.RedisKeyConstants.EVENT_JOIN_PREFIX;
import static org.example.tablenow.global.constant.RedisKeyConstants.EVENT_OPEN_KEY;
import static org.example.tablenow.global.constant.TimeConstants.ZONE_ID_ASIA_SEOUL;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final StoreService storeService;
    private final StringRedisTemplate redisTemplate;
    private final EventOpenProducer eventOpenProducer;

    private final ObjectMapper objectMapper;

    @Transactional
    public EventResponseDto createEvent(EventRequestDto request) {
        Store store = storeService.getStore(request.getStoreId());

        if (eventRepository.existsByStore_IdAndEventTime(store.getId(), request.getEventTime())) {
            throw new HandledException(ErrorCode.EVENT_ALREADY_EXISTS);
        }

        Event event = Event.create(store, request);
        Event savedEvent = eventRepository.save(event);

        saveEventOpenToRedis(savedEvent);

        return EventResponseDto.fromEvent(savedEvent);
    }

    @Transactional
    public EventResponseDto updateEvent(Long id, EventUpdateRequestDto request) {
        Event event = getEvent(id);

        validateReadyStatus(event);

        event.update(
                request.getOpenAt(),
                request.getEventTime(),
                request.getLimitPeople()
        );

        saveEventOpenToRedis(event);

        return EventResponseDto.fromEvent(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponseDto> getEvents(EventStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Event> events = (status == null)
                ? eventRepository.findAll(pageable)
                : eventRepository.findByStatus(status, pageable);

        return events.map(EventResponseDto::fromEvent);
    }

    @Transactional
    public EventDeleteResponseDto deleteEvent(Long id) {
        Event event = getEvent(id);

        validateReadyStatus(event);

        eventRepository.delete(event);
        eventRepository.save(event); // dirty checking 오류로 추가
        removeEventOpenFromRedis(event);

        return EventDeleteResponseDto.fromEvent(event);
    }

    @Transactional
    public EventCloseResponseDto closeEvent(Long id) {
        Event event = getEvent(id);

        event.close();
        eventRepository.save(event); // dirty checking 오류로 추가
        removeEventOpenFromRedis(event);

        return EventCloseResponseDto.fromEvent(event);
    }

    @Transactional
    public void openEventsIfDue() {
        long now = LocalDateTime.now().atZone(ZONE_ID_ASIA_SEOUL).toEpochSecond();
        Set<String> dueMessages = redisTemplate.opsForZSet()
                .rangeByScore(EVENT_OPEN_KEY, 0, now);
        log.info("[Scheduler] Redis ZSET 조회 결과 = {}", dueMessages);

        if (dueMessages == null || dueMessages.isEmpty()) return;

        for (String messageJson : dueMessages) {
            try {
                EventOpenMessage message = objectMapper.readValue(messageJson, EventOpenMessage.class);

                Event event = getEvent(message.getEventId());
                if (!event.isReady()) continue;

                event.open();
                eventRepository.save(event); // dirty checking 오류로 추가

                // MQ 발행
                eventOpenProducer.send(message);

//                log.info("[EventOpenSuccess]: eventId={}, store={}, openAt={}",
//                        event.getId(), event.getStoreName(), event.getOpenAt());

                // ZSet에서 제거
                redisTemplate.opsForZSet().remove(EVENT_OPEN_KEY, messageJson);

            } catch (Exception e) {
                log.error("이벤트 오픈 처리 실패: eventId={}", messageJson, e);
            }
        }
    }

    public Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));
    }

    private static void validateReadyStatus(Event event) {
        if (!event.isReady()) {
            throw new HandledException(ErrorCode.INVALID_EVENT_STATUS);
        }
    }

    private void saveEventOpenToRedis(Event event) {
        long openEpoch = event.getOpenAt().atZone(ZONE_ID_ASIA_SEOUL).toEpochSecond();
        EventOpenMessage message = EventOpenMessage.fromEvent(event);

        try {
            String messageJson = objectMapper.writeValueAsString(message);
            redisTemplate.opsForZSet().add(EVENT_OPEN_KEY, messageJson, openEpoch);
        } catch (JsonProcessingException e) {
            throw new HandledException(ErrorCode.EVENT_SERIALIZATION_FAILED);
        }
    }

    private void removeEventOpenFromRedis(Event event) {
        EventOpenMessage message = EventOpenMessage.fromEvent(event);
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            redisTemplate.opsForZSet().remove(EVENT_OPEN_KEY, messageJson);
        } catch (JsonProcessingException e) {
            throw new HandledException(ErrorCode.EVENT_SERIALIZATION_FAILED);
        }
        redisTemplate.delete(EVENT_JOIN_PREFIX + event.getId());
    }
}