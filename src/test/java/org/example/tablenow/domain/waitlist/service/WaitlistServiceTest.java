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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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
    private User user;

    @Mock
    private Store store;

    private final LocalDate testDate = LocalDate.of(2025, 4, 17);

    @Nested
    class 대기등록 {

        private WaitlistRequestDto requestDto;

        @BeforeEach
        void setUp() {
            requestDto = new WaitlistRequestDto();
            ReflectionTestUtils.setField(requestDto, "storeId", 10L);
            ReflectionTestUtils.setField(requestDto, "waitDate", testDate);

        }

        @Test
        void 대기등록_성공() {
            // given
            given(store.getId()).willReturn(10L);
            given(store.getName()).willReturn("테스트 가게");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeService.getStore(10L)).willReturn(store);
            given(waitlistRepository.existsByUserAndStoreAndIsNotifiedFalse(user, store)).willReturn(false);
            given(waitlistRepository.countByStoreAndWaitDateAndIsNotifiedFalse(store, testDate)).willReturn(3L);            given(waitlistRepository.save(any(Waitlist.class)))
                .willAnswer(invocation -> {
                    Waitlist saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 1L);
                    ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0));
                    return saved;
                });

            // when
            WaitlistResponseDto result = waitlistService.registerWaitlist(1L, requestDto);

            // then
            assertEquals(Long.valueOf(1L), result.getWaitlistId());
            assertEquals(Long.valueOf(10L), result.getStoreId());
        }

        @Test
        void 유저를_찾지_못해_대기등록_실패() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
        }

        @Test
        void 가게를_찾지_못해_대기등록_실패() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeService.getStore(10L)).willThrow(new HandledException(ErrorCode.STORE_NOT_FOUND));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.STORE_NOT_FOUND.getStatus(), exception.getHttpStatus());
        }

        @Test
        void 이미_대기중이라_중복대기로_실패() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeService.getStore(10L)).willReturn(store);
            given(waitlistRepository.existsByUserAndStoreAndIsNotifiedFalse(user, store)).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.WAITLIST_ALREADY_REGISTERED.getStatus(), exception.getHttpStatus());
        }

        @Test
        void 대기인원_초과로_실패() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeService.getStore(10L)).willReturn(store);
            given(waitlistRepository.existsByUserAndStoreAndIsNotifiedFalse(user, store)).willReturn(false);
            given(waitlistRepository.countByStoreAndWaitDateAndIsNotifiedFalse(store, testDate)).willReturn(100L);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.registerWaitlist(1L, requestDto)
            );

            assertEquals(ErrorCode.WAITLIST_FULL.getStatus(), exception.getHttpStatus());
        }
    }

    @Nested
    class 내대기목록조회 {

        @Test
        void 내대기목록_정상조회() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            Waitlist waitlist1 = new Waitlist(user, store, testDate);
            Waitlist waitlist2 = new Waitlist(user, store, testDate);
            ReflectionTestUtils.setField(waitlist1, "id", 1L);
            ReflectionTestUtils.setField(waitlist2, "id", 2L);

            given(waitlistRepository.findAllByUserAndIsNotifiedFalse(user))
                .willReturn(List.of(waitlist1, waitlist2));

            // when
            List<WaitlistFindResponseDto> result = waitlistService.findMyWaitlist(1L);

            // then
            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getWaitlistId());
            assertEquals(2L, result.get(1).getWaitlistId());
        }

        @Test
        void 유저없음으로_조회실패() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                waitlistService.findMyWaitlist(1L)
            );

            assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
        }
    }
}