package org.example.tablenow.domain.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.dto.request.EventUpdateRequestDto;
import org.example.tablenow.domain.event.dto.response.EventCloseResponseDto;
import org.example.tablenow.domain.event.dto.response.EventDeleteResponseDto;
import org.example.tablenow.domain.event.dto.response.EventResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.event.repository.EventRepository;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.enums.NotificationType;
import org.example.tablenow.domain.notification.service.NotificationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final StoreService storeService;
    private final EventJoinService eventJoinService;
    private final NotificationService notificationService;
    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String EVENT_JOIN_PREFIX = "event:join:";

    @Transactional
    public EventResponseDto createEvent(EventRequestDto request) {
        Store store = storeService.getStore(request.getStoreId());

        if (eventRepository.existsByStoreIdAndEventTime(store.getId(), request.getEventTime())) {
            throw new HandledException(ErrorCode.EVENT_ALREADY_EXISTS);
        }

        Event event = Event.create(store, request);
        Event savedEvent = eventRepository.save(event);

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
        redisTemplate.delete(EVENT_JOIN_PREFIX + id);
        return EventDeleteResponseDto.fromEvent(event);
    }

    @Transactional
    public EventCloseResponseDto closeEvent(Long id) {
        Event event = getEvent(id);

        event.close();

        redisTemplate.delete(EVENT_JOIN_PREFIX + id);
        return EventCloseResponseDto.fromEvent(event);
    }

    @Transactional
    public void openEventsIfDue() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> eventsToOpen = eventRepository.findAllByStatusAndOpenAtLessThanEqual(EventStatus.READY, now);

        for (Event event : eventsToOpen) {
            event.open();
            log.info("이벤트 오픈됨: eventId={}, storeName={}, openAt={}", event.getId(), event.getStore().getName(), event.getOpenAt());

            List<User> usersToNotify = eventJoinService.getUsersByEventId(event.getId());

            for (User user : usersToNotify) {
                if (user.getIsAlarmEnabled()) {
                    notificationService.createNotification(
                            NotificationRequestDto.builder()
                                    .userId(user.getId())
                                    .storeId(event.getStore().getId())
                                    .type(NotificationType.REMIND)
                                    .content(event.getStore().getName() + "의 이벤트가 오픈되었습니다!")
                                    .build()
                    );
                }
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
}
