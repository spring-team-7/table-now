package org.example.tablenow.domain.store.service;

import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootTest
@Transactional
//@Rollback(value = false)
public class StoreCreateTest {

    @Autowired
    private StoreJdbcRepository storeJdbcRepository;
    @Autowired
    private StoreRandomCreator storeRandomCreator;

    private final Random random = new Random();

    static final int TOTAL_STORES = 100_000;

//    @Test
    void 테스트_가게_생성() {
        List<StoreCreateRequestDto> requestDtoList = new ArrayList<>();

        for (int i = 0; i < TOTAL_STORES; i++) {
            Map<String, String> storeInfo = storeRandomCreator.createRandomStoreInfo();
            String address = storeRandomCreator.createRandomAddress();
            LocalTime start = LocalTime.of(6, 00).plusHours(random.nextInt(8));
            LocalTime end = start.plusHours(random.nextInt(8, 10));
            Long categoryId = Long.valueOf(random.nextInt(1, 16));
            Integer capacity = random.nextInt(1, 50) * 10;
            Integer deposit = random.nextInt(1, 9) * 10000;

            StoreCreateRequestDto dto = new StoreCreateRequestDto();

            ReflectionTestUtils.setField(dto, "name", storeInfo.get("name"));
            ReflectionTestUtils.setField(dto, "description", storeInfo.get("description"));
            ReflectionTestUtils.setField(dto, "address", address);
            ReflectionTestUtils.setField(dto, "categoryId", categoryId);
            ReflectionTestUtils.setField(dto, "capacity", capacity);
            ReflectionTestUtils.setField(dto, "deposit", deposit);
            ReflectionTestUtils.setField(dto, "startTime", start);
            ReflectionTestUtils.setField(dto, "endTime", end);

            requestDtoList.add(dto);
        }

        storeJdbcRepository.insertBatch(requestDtoList);
    }
}
