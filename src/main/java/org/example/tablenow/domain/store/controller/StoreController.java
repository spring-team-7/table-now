package org.example.tablenow.domain.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.request.StoreUpdateRequestDto;
import org.example.tablenow.domain.store.dto.response.*;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 가게 등록
    @Secured(UserRole.Authority.OWNER)
    @PostMapping("/v1/owner/stores")
    public ResponseEntity<StoreCreateResponseDto> createStore(@AuthenticationPrincipal AuthUser authUser,
                                                              @Valid @RequestBody StoreCreateRequestDto requestDto) {
        return ResponseEntity.ok(storeService.createStore(authUser, requestDto));
    }

    // 내 가게 목록 조회
    @Secured(UserRole.Authority.OWNER)
    @GetMapping("/v1/owner/stores")
    public ResponseEntity<List<StoreResponseDto>> findMyStores(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(storeService.findMyStores(authUser));
    }

    // 가게 수정
    @Secured(UserRole.Authority.OWNER)
    @PatchMapping("/v1/owner/stores/{id}")
    public ResponseEntity<StoreUpdateResponseDto> updateStore(@PathVariable Long id,
                                                              @AuthenticationPrincipal AuthUser authUser,
                                                              @Valid @RequestBody StoreUpdateRequestDto requestDto) {
        return ResponseEntity.ok(storeService.updateStore(id, authUser, requestDto));
    }

    // 가게 삭제
    @Secured(UserRole.Authority.OWNER)
    @DeleteMapping("/v1/owner/stores/{id}")
    public ResponseEntity<StoreDeleteResponseDto> deleteStore(@PathVariable Long id,
                                                              @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(storeService.deleteStore(id, authUser));
    }

    // 가게 목록 조회 (RDBMS)
    @GetMapping("/v1/stores")
    public ResponseEntity<Page<StoreSearchResponseDto>> getStoresV1(@AuthenticationPrincipal AuthUser authUser,
                                                                     @RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "10") int size,
                                                                     @RequestParam(defaultValue = "ratingCount") String sort,
                                                                     @RequestParam(defaultValue = "desc") String direction,
                                                                     @RequestParam(required = false) Long categoryId,
                                                                     @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(storeService.getStoresV1(authUser, page, size, sort, direction, categoryId, keyword));
    }

    // 가게 목록 조회 (Redis)
    @GetMapping("/v2/stores")
    public ResponseEntity<Page<StoreSearchResponseDto>> getStoresV2(@AuthenticationPrincipal AuthUser authUser,
                                                                    @RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(defaultValue = "ratingCount") String sort,
                                                                    @RequestParam(defaultValue = "desc") String direction,
                                                                    @RequestParam(required = false) Long categoryId,
                                                                    @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(storeService.getStoresV2(authUser, page, size, sort, direction, categoryId, keyword));
    }

    // 가게 정보 조회
    @GetMapping("/v1/stores/{id}")
    public ResponseEntity<StoreResponseDto> getStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.findStore(id));
    }

    // 가게 인기 검색 랭킹 조회
    @GetMapping("/v1/stores/ranking")
    public ResponseEntity<List<StoreRankingResponseDto>> getStoreRanking(@RequestParam(defaultValue = "10") int limit,
                                                                         @RequestParam(required = false) String timeKey) {
        return ResponseEntity.ok(storeService.getStoreRanking(limit, timeKey));
    }
}
