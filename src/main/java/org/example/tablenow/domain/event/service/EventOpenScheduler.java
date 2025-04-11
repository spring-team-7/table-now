package org.example.tablenow.domain.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOpenScheduler {
    private final EventService eventService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void runEventOpenScheduler() {
        try {
            log.info("이벤트 스케줄러가 실행되었습니다. time={}", LocalDateTime.now());
            eventService.openEventsIfDue();
        } catch (Exception e) {
            log.error("이벤트 스케줄러 실행 중 오류가 발생했습니다.", e);
        }
    }
}
