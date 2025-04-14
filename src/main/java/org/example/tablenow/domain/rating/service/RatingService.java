package org.example.tablenow.domain.rating.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final StoreService storeService;
    private final ReservationService reservationService;

    @Transactional
    public RatingCreateResponseDto createRating(AuthUser authUser, Long storeId, @Valid RatingRequestDto requestDto) {
        User user = User.fromAuthUser(authUser);
        Store store = storeService.getStore(storeId);
        int newRating = requestDto.getRating();

        validateExistRating(user.getId(), storeId);
        reservationService.validateCreateRating(user.getId(), storeId);

        Rating rating = Rating.builder()
                .user(user)
                .store(store)
                .rating(newRating)
                .build();
        Rating savedRating = ratingRepository.save(rating);
        store.applyRating(newRating);

        return RatingCreateResponseDto.fromRating(savedRating);
    }

    @Transactional
    public RatingUpdateResponseDto updateRating(AuthUser authUser, Long storeId, RatingRequestDto requestDto) {
        User user = User.fromAuthUser(authUser);
        Store store = storeService.getStore(storeId);
        Rating rating = getRating(user, store);

        int oldRating = rating.getRating();
        int newRating = requestDto.getRating();

        rating.updateRating(newRating);
        store.updateRating(oldRating, newRating);

        return RatingUpdateResponseDto.fromRating(rating);
    }

    @Transactional
    public RatingDeleteResponseDto deleteRating(AuthUser authUser, Long storeId) {
        User user = User.fromAuthUser(authUser);
        Store store = storeService.getStore(storeId);
        Rating rating = getRating(user, store);

        store.removeRating(rating.getRating());
        ratingRepository.delete(rating);

        return RatingDeleteResponseDto.fromRating(rating.getId());
    }

    private Rating getRating(User user, Store store) {
        return ratingRepository.findByUserAndStore(user, store)
                .orElseThrow(() -> new HandledException(ErrorCode.RATING_NOT_FOUND));
    }

    private void validateExistRating(Long userId, Long storeId) {
        if (ratingRepository.existsByUserAndStore(userId, storeId)) {
            throw new HandledException(ErrorCode.RATING_ALREADY_EXISTS);
        }
    }
}
