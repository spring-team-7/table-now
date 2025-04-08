package org.example.tablenow.domain.event.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.dto.request.EventUpdateRequestDto;
import org.example.tablenow.domain.event.dto.response.EventDeleteResponseDto;
import org.example.tablenow.domain.event.dto.response.EventResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.event.repository.EventRepository;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final StoreService storeService;

    @Transactional
    public EventResponseDto createEvent(EventRequestDto request) {
        Store store = storeService.getStore(request.getStoreId());

        if (eventRepository.existsByStoreIdAndEventTime(store.getId(), request.getEventTime())) {
            throw new HandledException(ErrorCode.EVENT_ALREADY_EXISTS);
        }

        Event event = Event.create(store, request);
        eventRepository.save(event);

        return EventResponseDto.fromEvent(event);
    }

    @Transactional
    public EventResponseDto updateEvent(Long id, EventUpdateRequestDto request) {
        Event event = getEvent(id);

        if (event.getStatus() != EventStatus.READY) {
            throw new HandledException(ErrorCode.INVALID_EVENT_STATUS);
        }

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

        if (event.getStatus() != EventStatus.READY) {
            throw new HandledException(ErrorCode.INVALID_EVENT_STATUS);
        }

        eventRepository.delete(event);
        return EventDeleteResponseDto.fromEvent(event);
    }

    protected Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.EVENT_NOT_FOUND));
    }
}
