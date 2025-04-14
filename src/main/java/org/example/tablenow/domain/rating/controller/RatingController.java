package org.example.tablenow.domain.rating.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.rating.dto.request.RatingRequestDto;
import org.example.tablenow.domain.rating.dto.response.RatingCreateResponseDto;
import org.example.tablenow.domain.rating.dto.response.RatingDeleteResponseDto;
import org.example.tablenow.domain.rating.dto.response.RatingUpdateResponseDto;
import org.example.tablenow.domain.rating.service.RatingService;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Secured(UserRole.Authority.USER)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // 평점 등록
    @PostMapping("/v1/stores/{storeId}/ratings")
    public ResponseEntity<RatingCreateResponseDto> createRating(@AuthenticationPrincipal AuthUser authUser,
                                                                @PathVariable Long storeId,
                                                                @Valid @RequestBody RatingRequestDto requestDto) {
        return ResponseEntity.ok(ratingService.createRating(authUser, storeId, requestDto));
    }

    // 평점 수정
    @PatchMapping("/v1/stores/{storeId}/ratings")
    public ResponseEntity<RatingUpdateResponseDto> updateRating(@AuthenticationPrincipal AuthUser authUser,
                                                                @PathVariable Long storeId,
                                                                @Valid @RequestBody RatingRequestDto requestDto) {
        return ResponseEntity.ok(ratingService.updateRating(authUser, storeId, requestDto));
    }

    // 평점 삭제
    @DeleteMapping("/v1/stores/{storeId}/ratings")
    public ResponseEntity<RatingDeleteResponseDto> deleteRating(@AuthenticationPrincipal AuthUser authUser,
                                                                @PathVariable Long storeId) {
        return ResponseEntity.ok(ratingService.deleteRating(authUser, storeId));
    }
}
