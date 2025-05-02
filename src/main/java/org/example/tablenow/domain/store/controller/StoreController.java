package org.example.tablenow.domain.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.request.StoreSearchRequestDto;
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

@Tag(name = "가게 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 가게 등록
    @Operation(summary = "가게 등록")
    @Secured(UserRole.Authority.OWNER)
    @PostMapping("/v1/owner/stores")
    public ResponseEntity<StoreCreateResponseDto> createStore(@AuthenticationPrincipal AuthUser authUser,
                                                              @Valid @RequestBody StoreCreateRequestDto requestDto) {
        return ResponseEntity.ok(storeService.createStore(authUser, requestDto));
    }

    // 내 가게 목록 조회
    @Operation(summary = "내 가게 목록 조회")
    @Secured(UserRole.Authority.OWNER)
    @GetMapping("/v1/owner/stores")
    public ResponseEntity<List<StoreResponseDto>> findMyStores(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(storeService.findMyStores(authUser));
    }

    // 가게 수정
    @Operation(summary = "가게 수정")
    @Secured(UserRole.Authority.OWNER)
    @PatchMapping("/v1/owner/stores/{storeId}")
    public ResponseEntity<StoreUpdateResponseDto> updateStore(@PathVariable Long storeId,
                                                              @AuthenticationPrincipal AuthUser authUser,
                                                              @Valid @RequestBody StoreUpdateRequestDto requestDto) {
        return ResponseEntity.ok(storeService.updateStore(storeId, authUser, requestDto));
    }

    // 가게 삭제
    @Operation(summary = "가게 삭제")
    @Secured(UserRole.Authority.OWNER)
    @DeleteMapping("/v1/owner/stores/{storeId}")
    public ResponseEntity<StoreDeleteResponseDto> deleteStore(@PathVariable Long storeId,
                                                              @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(storeService.deleteStore(storeId, authUser));
    }

    // 가게 검색 v1 (RDBMS)
    @Operation(summary = "가게 검색 V1 (RDB)")
    @GetMapping("/v1/stores")
    public ResponseEntity<Page<StoreSearchResponseDto>> getStoresV1(@AuthenticationPrincipal AuthUser authUser,
                                                                    @ModelAttribute StoreSearchRequestDto request
    ) {
        return ResponseEntity.ok(storeService.getStoresV1(authUser, request.getPage(), request.getSize(), request.getSort(), request.getDirection(), request.getCategoryId(), request.getKeyword()));
    }

    // 가게 검색 v2 (Redis)
    @Operation(summary = "가게 검색 V2 (Redis)")
    @GetMapping("/v2/stores")
    public ResponseEntity<Page<StoreSearchResponseDto>> getStoresV2(@AuthenticationPrincipal AuthUser authUser,
                                                                    @ModelAttribute StoreSearchRequestDto request
    ) {
        return ResponseEntity.ok(storeService.getStoresV2(authUser, request.getPage(), request.getSize(), request.getSort(), request.getDirection(), request.getCategoryId(), request.getKeyword()));
    }

    // 가게 정보 조회
    @Operation(summary = "가게 정보 조회")
    @GetMapping("/v1/stores/{storeId}")
    public ResponseEntity<StoreResponseDto> getStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeService.findStore(storeId));
    }

    // 가게 인기 검색 랭킹 조회
    @Operation(summary = "가게 인기 랭킹 조회")
    @GetMapping("/v1/stores/ranking")
    public ResponseEntity<List<StoreRankingResponseDto>> getStoreRanking(@RequestParam(defaultValue = "10") int limit,
                                                                         @RequestParam(required = false) String timeKey) {
        return ResponseEntity.ok(storeService.getStoreRanking(limit, timeKey));
    }
}
