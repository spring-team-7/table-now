package org.example.tablenow.domain.store.controller;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.response.StoreDocumentResponseDto;
import org.example.tablenow.domain.store.service.StoreSearchService;
import org.example.tablenow.global.dto.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoreSearchController {

    private final StoreSearchService storeSearchService;

    @GetMapping("/v3/stores")
    public ResponseEntity<PageResponse<StoreDocumentResponseDto>> getStoresV3(@RequestParam(defaultValue = "1") int page,
                                                                              @RequestParam(defaultValue = "10") int size,
                                                                              @RequestParam(defaultValue = "ratingCount") String sort,
                                                                              @RequestParam(defaultValue = "desc") String direction,
                                                                              @RequestParam(required = false) Long categoryId,
                                                                              @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(storeSearchService.getStoresV3(page, size, sort, direction, categoryId, keyword));
    }
}
