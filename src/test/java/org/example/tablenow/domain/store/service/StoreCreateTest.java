package org.example.tablenow.domain.store.service;

import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
@Transactional
//@Rollback(value = false)
public class StoreCreateTest {

    @Autowired
    private StoreJdbcRepository storeJdbcRepository;
    private final Random random = new Random();

    static final int TOTAL_STORES = 100_000;

    //@Test
    void 테스트_가게_생성() {
        List<StoreCreateRequestDto> requestDtoList = new ArrayList<>();

        for (int i = 0; i < TOTAL_STORES; i++) {
            LocalTime start = LocalTime.of(6, 00).plusHours(random.nextInt(8));
            LocalTime end = start.plusHours(random.nextInt(8, 10));
            Long categoryId = Long.valueOf(random.nextInt(1, 16));
            Integer capacity = random.nextInt(1, 50) * 10;
            Integer deposit = random.nextInt(1, 9) * 10000;

            StoreCreateRequestDto dto = StoreCreateRequestDto.builder()
                    .name("테스트_가게_" + i)
                    .description("테스트_설명_" + i)
                    .address("주소_" + i)
                    .categoryId(categoryId)
                    .capacity(capacity)
                    .deposit(deposit)
                    .startTime(start)
                    .endTime(end)
                    .build();
            requestDtoList.add(dto);
        }

        storeJdbcRepository.insertBatch(requestDtoList);
    }
}
