package org.example.tablenow.domain.waitlist.service;

import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.domain.waitlist.dto.request.WaitlistRequestDto;
import org.example.tablenow.domain.waitlist.dto.response.WaitlistFindResponseDto;
import org.example.tablenow.domain.waitlist.dto.response.WaitlistResponseDto;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.example.tablenow.domain.waitlist.repository.WaitlistRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @InjectMocks
    private WaitlistService waitlistService;

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationService reservationService;

    @Mock
    private StoreService storeService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private User user;

    @Mock
    private Store store;

    @Nested
    class 대기등록 {

        @Test
        void 대기등록_성공() {
            LocalDate testDate = LocalDate.of(2025, 5, 20);
            WaitlistRequestDto requestDto = new WaitlistRequestDto();
            ReflectionTestUtils.setField(requestDto, "storeId", 10L);
            ReflectionTestUtils.setField(requestDto, "waitDate", testDate);

            given(store.getId()).willReturn(10L);
            given(store.getName()).willReturn("테스트 가게");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeService.getStore(10L)).willReturn(store);
            given(waitlistRepository.existsByUserAndStoreAndWaitDateAndIsNotifiedFalse(user, store, testDate)).willReturn(false);
            given(waitlistRepository.countByStoreAndWaitDateAndIsNotifiedFalse(store, testDate)).willReturn(3L);
            given(waitlistRepository.save(any(Waitlist.class)))
                .willAnswer(invocation -> {
                    Waitlist saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 1L);
                    ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0));
                    return saved;
                });

            WaitlistResponseDto result = waitlistService.registerWaitlist(1L, requestDto);

            assertEquals(1L, result.getWaitlistId());
            assertEquals(10L, result.getStoreId());
        }

        @Test
        void 유저를_찾지_못해_대기등록_실패() {
            LocalDate testDate = LocalDate.of(2025, 5, 20);
            WaitlistRequestDto requestDto = new WaitlistRequestDto();
            ReflectionTestUtils.setField(requestDto, "storeId", 10L);
            ReflectionTestUtils.setField(requestDto, "waitDate", testDate);

            given(userRepository.findById(1L)).willReturn(Optional.empty());

            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
        }

        @Test
        void 가게를_찾지_못해_대기등록_실패() {
            LocalDate testDate = LocalDate.of(2025, 5, 20);
            WaitlistRequestDto requestDto = new WaitlistRequestDto();
            ReflectionTestUtils.setField(requestDto, "storeId", 10L);
            ReflectionTestUtils.setField(requestDto, "waitDate", testDate);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeService.getStore(10L)).willThrow(new HandledException(ErrorCode.STORE_NOT_FOUND));

            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.STORE_NOT_FOUND.getStatus(), exception.getHttpStatus());
        }

        @Test
        void 이미_대기중이라_중복대기로_실패() {
            LocalDate testDate = LocalDate.of(2025, 5, 20);
            WaitlistRequestDto requestDto = new WaitlistRequestDto();
            ReflectionTestUtils.setField(requestDto, "storeId", 10L);
            ReflectionTestUtils.setField(requestDto, "waitDate", testDate);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeService.getStore(10L)).willReturn(store);
            given(waitlistRepository.existsByUserAndStoreAndWaitDateAndIsNotifiedFalse(user, store, testDate)).willReturn(true);

            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.WAITLIST_ALREADY_REGISTERED.getStatus(), exception.getHttpStatus());
        }

        @Test
        void 대기인원_초과로_실패() {
            LocalDate testDate = LocalDate.of(2025, 5, 20);
            WaitlistRequestDto requestDto = new WaitlistRequestDto();
            ReflectionTestUtils.setField(requestDto, "storeId", 10L);
            ReflectionTestUtils.setField(requestDto, "waitDate", testDate);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeService.getStore(10L)).willReturn(store);
            given(waitlistRepository.existsByUserAndStoreAndWaitDateAndIsNotifiedFalse(user, store, testDate)).willReturn(false);
            given(waitlistRepository.countByStoreAndWaitDateAndIsNotifiedFalse(store, testDate)).willReturn(100L);

            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.WAITLIST_FULL.getStatus(), exception.getHttpStatus());
        }
    }

    @Nested
    class 대기등록_Redis분산락 {

        @Test
        void 레디스락_대기등록_성공() throws Exception {
            LocalDate testDate = LocalDate.of(2025, 5, 20);
            WaitlistRequestDto requestDto = new WaitlistRequestDto();
            ReflectionTestUtils.setField(requestDto, "storeId", 10L);
            ReflectionTestUtils.setField(requestDto, "waitDate", testDate);

            String lockKey = String.format("lock:store:10:date:%s", requestDto.getWaitDate());
            RLock mockLock = mock(RLock.class);
            given(redissonClient.getLock(lockKey)).willReturn(mockLock);
            given(mockLock.tryLock(2, 2, TimeUnit.SECONDS)).willReturn(true);
            given(mockLock.isHeldByCurrentThread()).willReturn(true);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeService.getStore(10L)).willReturn(store);
            given(reservationService.hasVacancyDate(store, testDate)).willReturn(false);
            given(waitlistRepository.existsByUserAndStoreAndWaitDateAndIsNotifiedFalse(user, store, testDate)).willReturn(false);
            given(waitlistRepository.countByStoreAndWaitDateAndIsNotifiedFalse(store, testDate)).willReturn(3L);
            given(waitlistRepository.save(any())).willAnswer(invocation -> {
                Waitlist waitlist = invocation.getArgument(0);
                ReflectionTestUtils.setField(waitlist, "id", 1L);
                return waitlist;
            });

            WaitlistResponseDto result = waitlistService.registerLockWaitlist(1L, requestDto);

            assertEquals(1L, result.getWaitlistId());
            verify(mockLock).unlock();
        }

        @Test
        void 레디스락_획득_실패() throws Exception {
            LocalDate testDate = LocalDate.of(2025, 5, 20);
            WaitlistRequestDto requestDto = new WaitlistRequestDto();
            ReflectionTestUtils.setField(requestDto, "storeId", 10L);
            ReflectionTestUtils.setField(requestDto, "waitDate", testDate);

            String lockKey = String.format("lock:store:10:date:%s", requestDto.getWaitDate());
            RLock mockLock = mock(RLock.class);
            given(redissonClient.getLock(lockKey)).willReturn(mockLock);
            given(mockLock.tryLock(2, 2, TimeUnit.SECONDS)).willReturn(false);

            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerLockWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.WAITLIST_REQUEST_TIMEOUT.getStatus(), exception.getHttpStatus());
        }

        @Test
        void 레디스락_인터럽트_발생_예외() throws Exception {
            LocalDate testDate = LocalDate.of(2025, 5, 20);
            WaitlistRequestDto requestDto = new WaitlistRequestDto();
            ReflectionTestUtils.setField(requestDto, "storeId", 10L);
            ReflectionTestUtils.setField(requestDto, "waitDate", testDate);

            String lockKey = String.format("lock:store:10:date:%s", requestDto.getWaitDate());
            RLock mockLock = mock(RLock.class);
            given(redissonClient.getLock(lockKey)).willReturn(mockLock);
            given(mockLock.tryLock(2, 2, TimeUnit.SECONDS)).willThrow(new InterruptedException());

            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerLockWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.WAITLIST_REQUEST_INTERRUPTED.getStatus(), exception.getHttpStatus());
        }
    }

    @Nested
    class 내대기목록조회 {

        @Test
        void 내대기목록_정상조회() {
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            Waitlist waitlist1 = new Waitlist(user, store, LocalDate.of(2025, 5, 20));
            Waitlist waitlist2 = new Waitlist(user, store, LocalDate.of(2025, 5, 21));
            ReflectionTestUtils.setField(waitlist1, "id", 1L);
            ReflectionTestUtils.setField(waitlist2, "id", 2L);

            given(waitlistRepository.findAllByUserAndIsNotifiedFalse(user)).willReturn(List.of(waitlist1, waitlist2));

            List<WaitlistFindResponseDto> result = waitlistService.findMyWaitlist(1L);

            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getWaitlistId());
            assertEquals(2L, result.get(1).getWaitlistId());
        }

        @Test
        void 유저없음으로_조회실패() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.findMyWaitlist(1L)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
        }
    }
}