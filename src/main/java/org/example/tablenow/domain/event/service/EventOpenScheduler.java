package org.example.tablenow.domain.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOpenScheduler {
    private final EventService eventService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void runEventOpenScheduler() {
        log.info("🕒 이벤트 스케줄러 실행됨!");
        eventService.openEventsIfDue();
    }
}
