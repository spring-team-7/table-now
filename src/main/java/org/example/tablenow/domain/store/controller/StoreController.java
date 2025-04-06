package org.example.tablenow.domain.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.request.StoreUpdateRequestDto;
import org.example.tablenow.domain.store.dto.response.*;
import org.example.tablenow.domain.store.service.StoreService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 가게 등록
    @Secured("ROLE_OWNER")
    @PostMapping("/owner/stores")
    public ResponseEntity<StoreCreateResponseDto> saveStore(@Valid @RequestBody StoreCreateRequestDto requestDto) {
        return ResponseEntity.ok(storeService.saveStore(requestDto));
    }

    // 내 가게 목록 조회
    @Secured("ROLE_OWNER")
    @GetMapping("/owner/stores")
    public ResponseEntity<List<StoreResponseDto>> findMyStores() {
        return ResponseEntity.ok(storeService.findMyStores());
    }

    // 가게 수정
    @Secured("ROLE_OWNER")
    @PatchMapping("/owner/stores/{id}")
    public ResponseEntity<StoreUpdateResponseDto> updateStore(@PathVariable Long id, @Valid @RequestBody StoreUpdateRequestDto requestDto) {
        return ResponseEntity.ok(storeService.updateStore(id, requestDto));
    }

    // 가게 삭제
    @Secured("ROLE_OWNER")
    @DeleteMapping("/owner/stores/{id}")
    public ResponseEntity<StoreDeleteResponseDto> deleteStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.deleteStore(id));
    }

    // 가게 목록 조회
    @GetMapping("/stores")
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
    @GetMapping("/stores/{id}")
    public ResponseEntity<StoreResponseDto> getStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.findStore(id));
    }

    // 가게 인기 검색 랭킹 조회

}
