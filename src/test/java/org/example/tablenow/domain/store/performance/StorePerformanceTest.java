package org.example.tablenow.domain.store.performance;

import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.config.WithMockAuthUser;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class StorePerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    StoreRepository storeRepository;

    @Test
    @WithMockAuthUser(userId = 1L, email = "test@test.com", role = UserRole.ROLE_USER, nickname = "테스트")
    void 가게_목록_조회_캐시_성능_테스트() throws Exception {
        int iterations = 1000;
        long totalDuration = 0L;

        for (int i = 0; i < iterations; i++) {
            Instant start = Instant.now();
            mockMvc.perform(get("/api/v1/stores")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "rating")
                    .param("direction", "desc")
                    .param("categoryId", "1")
                    .param("search", "가게")
            ).andExpect(status().isOk());
            totalDuration += Duration.between(start, Instant.now()).toMillis();
        }
        log.info("평균 응답 시간: {}ms", (totalDuration / (double) iterations));
    }

    @Test
    void 동시_요청_시_redis_캐시_조회_검증() throws InterruptedException {
        int threadCount = 200;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(get("/api/v1/stores")
                            .param("page", "1")
                            .param("size", "10")
                            .param("sort", "rating")
                            .param("direction", "desc")
                            .param("categoryId", "1")
                            .param("search", "가게")
                    ).andExpect(status().isOk());
                } catch (Exception e) {
                    log.error(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        verify(storeRepository, atMost(1))
                .searchStores(
                        any(Pageable.class),
                        anyLong(),
                        anyString()
                );
    }
}
