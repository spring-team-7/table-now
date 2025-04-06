package org.example.tablenow.domain.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.response.StoreCreateResponseDto;
import org.example.tablenow.domain.store.dto.response.StoreResponseDto;
import org.example.tablenow.domain.store.service.StoreService;
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

    // 가게 삭제

    // 가게 목록 조회

    // 가게 정보 조회

    // 가게 인기 검색 랭킹 조회

}
