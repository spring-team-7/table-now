package org.example.tablenow.domain.rating.service;

import org.example.tablenow.domain.rating.dto.request.RatingRequestDto;
import org.example.tablenow.domain.rating.dto.response.RatingCreateResponseDto;
import org.example.tablenow.domain.rating.dto.response.RatingDeleteResponseDto;
import org.example.tablenow.domain.rating.dto.response.RatingUpdateResponseDto;
import org.example.tablenow.domain.rating.entity.Rating;
import org.example.tablenow.domain.rating.repository.RatingRepository;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private StoreService storeService;
    @Mock
    private ReservationService reservationService;
    @InjectMocks
    private RatingService ratingService;

    Long userId = 1L;
    AuthUser authUser = new AuthUser(userId, "user@a.com", UserRole.ROLE_USER, "일반");
    User user = User.fromAuthUser(authUser);

    Long storeId = 1L;
    Double averageRating = 5.0;
    int ratingCount = 2;
    Store store = Store.builder().id(storeId).averageRating(averageRating).ratingCount(ratingCount).build();

    Long ratingId = 1L;
    Rating rating = Rating.builder().id(1L).user(user).store(store).rating(4).build();

    @Nested
    class 평점_등록 {
        RatingRequestDto dto = new RatingRequestDto(4);

        @Test
        void 존재하지_않는_가게의_평점_등록_시_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willThrow(new HandledException(ErrorCode.STORE_NOT_FOUND));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                ratingService.createRating(authUser, storeId, dto);
            });

            assertEquals(exception.getMessage(), ErrorCode.STORE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 평점_중복_등록_시_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(ratingRepository.existsByUserAndStore(anyLong(), anyLong())).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                ratingService.createRating(authUser, storeId, dto);
            });

            assertEquals(exception.getMessage(), ErrorCode.RATING_ALREADY_EXISTS.getDefaultMessage());
        }

        @Test
        void 해당_가게에_유저가_등록한_완료_예약이_없을_시_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(ratingRepository.existsByUserAndStore(anyLong(), anyLong())).willReturn(false);
            willThrow(new HandledException(ErrorCode.RATING_RESERVATION_NOT_FOUND))
                    .given(reservationService).validateCreateRating(anyLong(), anyLong());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                ratingService.createRating(authUser, storeId, dto);
            });

            assertEquals(exception.getMessage(), ErrorCode.RATING_RESERVATION_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 등록_성공() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(ratingRepository.existsByUserAndStore(anyLong(), anyLong())).willReturn(false);
            given(ratingRepository.save(any(Rating.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            RatingCreateResponseDto response = ratingService.createRating(authUser, storeId, dto);

            // then
            verify(reservationService).validateCreateRating(anyLong(), anyLong());
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getUserId(), userId),
                    () -> assertEquals(response.getStoreId(), storeId),
                    () -> assertEquals(response.getRating(), dto.getRating()),
                    () -> assertEquals(store.getRatingCount(), ratingCount + 1),
                    () -> assertEquals(store.getAverageRating(),
                            ((averageRating * ratingCount) + dto.getRating()) / store.getRatingCount())
            );
        }
    }

    @Nested
    class 평점_수정 {

        RatingRequestDto dto = new RatingRequestDto(4);

        @Test
        void 존재하지_않는_가게의_평점_수정_시_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willThrow(new HandledException(ErrorCode.STORE_NOT_FOUND));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                ratingService.updateRating(authUser, storeId, dto);
            });

            verify(storeService).getStore(anyLong());
            assertEquals(exception.getMessage(), ErrorCode.STORE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 존재하지_않는_평점_수정_시_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(ratingRepository.findByUserIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                ratingService.updateRating(authUser, storeId, dto);
            });

            assertEquals(exception.getMessage(), ErrorCode.RATING_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 평점_등록_수가_0_인_경우_예외_발생() {
            // given
            ReflectionTestUtils.setField(store, "ratingCount", 0);
            given(storeService.getStore(anyLong())).willReturn(store);
            given(ratingRepository.findByUserIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.of(rating));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                ratingService.updateRating(authUser, storeId, dto);
            });

            assertEquals(exception.getMessage(), ErrorCode.CONFLICT.getDefaultMessage());
        }

        @Test
        void 수정_성공() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(ratingRepository.findByUserIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.of(rating));

            // when
            RatingUpdateResponseDto response = ratingService.updateRating(authUser, storeId, dto);

            // then
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getUserId(), userId),
                    () -> assertEquals(response.getStoreId(), storeId),
                    () -> assertEquals(response.getRating(), dto.getRating()),
                    () -> assertEquals(store.getRatingCount(), ratingCount),
                    () -> assertEquals(store.getAverageRating(),
                            ((averageRating * ratingCount) - rating.getRating() + dto.getRating()) / ratingCount)
            );
        }
    }

    @Nested
    class 평점_삭제 {

        @Test
        void 존재하지_않는_가게의_평점_삭제_시_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willThrow(new HandledException(ErrorCode.STORE_NOT_FOUND));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                ratingService.deleteRating(authUser, storeId);
            });

            verify(storeService).getStore(anyLong());
            assertEquals(exception.getMessage(), ErrorCode.STORE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 존재하지_않는_평점_삭제_시_예외_발생() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(ratingRepository.findByUserIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                ratingService.deleteRating(authUser, storeId);
            });

            assertEquals(exception.getMessage(), ErrorCode.RATING_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 전체_평점_삭제_시_가게_평점_0_초기화() {
            // given
            ReflectionTestUtils.setField(store, "ratingCount", 1);
            given(storeService.getStore(anyLong())).willReturn(store);
            given(ratingRepository.findByUserIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.of(rating));

            // when
            RatingDeleteResponseDto response = ratingService.deleteRating(authUser, storeId);

            // then
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getRatingId(), ratingId),
                    () -> assertEquals(store.getRatingCount(), 0),
                    () -> assertEquals(store.getAverageRating(), 0.0)
            );
        }

        @Test
        void 삭제_성공() {
            // given
            given(storeService.getStore(anyLong())).willReturn(store);
            given(ratingRepository.findByUserIdAndStoreId(anyLong(), anyLong())).willReturn(Optional.of(rating));

            // when
            RatingDeleteResponseDto response = ratingService.deleteRating(authUser, storeId);

            // then
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getRatingId(), ratingId),
                    () -> assertEquals(store.getRatingCount(), ratingCount - 1),
                    () -> assertEquals(store.getAverageRating(),
                            ((averageRating * ratingCount) - rating.getRating()) / (ratingCount - 1))
            );
        }
    }
}
