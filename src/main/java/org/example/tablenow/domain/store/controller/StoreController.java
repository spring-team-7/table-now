package org.example.tablenow.domain.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.request.StoreUpdateRequestDto;
import org.example.tablenow.domain.store.dto.response.*;
import org.example.tablenow.domain.store.service.StoreService;
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
    @Secured("ROLE_OWNER")
    @PostMapping("/v1/owner/stores")
    public ResponseEntity<StoreCreateResponseDto> saveStore(@AuthenticationPrincipal AuthUser authUser,
                                                            @Valid @RequestBody StoreCreateRequestDto requestDto) {
        return ResponseEntity.ok(storeService.saveStore(authUser, requestDto));
    }

    // 내 가게 목록 조회
    @Secured("ROLE_OWNER")
    @GetMapping("/v1/owner/stores")
    public ResponseEntity<List<StoreResponseDto>> findMyStores(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(storeService.findMyStores(authUser));
    }

    // 가게 수정
    @Secured("ROLE_OWNER")
    @PatchMapping("/v1/owner/stores/{id}")
    public ResponseEntity<StoreUpdateResponseDto> updateStore(@PathVariable Long id,
                                                              @AuthenticationPrincipal AuthUser authUser,
                                                              @Valid @RequestBody StoreUpdateRequestDto requestDto) {
        return ResponseEntity.ok(storeService.updateStore(id, authUser, requestDto));
    }

    // 가게 삭제
    @Secured("ROLE_OWNER")
    @DeleteMapping("/v1/owner/stores/{id}")
    public ResponseEntity<StoreDeleteResponseDto> deleteStore(@PathVariable Long id,
                                                              @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(storeService.deleteStore(id, authUser));
    }

    // 가게 목록 조회
    @GetMapping("/v1/stores")
    public ResponseEntity<Page<StoreSearchResponseDto>> getStores(@RequestParam(defaultValue = "1") int page,
                                                                      @RequestParam(defaultValue = "10") int size,
                                                                      @RequestParam(defaultValue = "name") String sort,
                                                                      @RequestParam(defaultValue = "asc") String direction,
                                                                      @RequestParam(required = false) Long categoryId,
                                                                      @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(storeService.findAllStores(page, size, sort, direction, categoryId, search));
    }

    // 가게 정보 조회
    @GetMapping("/v1/stores/{id}")
    public ResponseEntity<StoreResponseDto> getStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.findStore(id));
    }

    // 가게 인기 검색 랭킹 조회
    @GetMapping("/v1/stores/ranking")
    public ResponseEntity<List<StoreRankingResponseDto>> getStoreRanking(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(storeService.getStoreRanking(limit));
    }
}
