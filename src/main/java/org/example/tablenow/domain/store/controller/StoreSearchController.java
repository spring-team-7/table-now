package org.example.tablenow.domain.store.controller;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.response.StoreDocumentResponseDto;
import org.example.tablenow.domain.store.service.StoreBulkIndexer;
import org.example.tablenow.domain.store.service.StoreSearchService;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoreSearchController {

    private final StoreSearchService storeSearchService;
    private final StoreBulkIndexer storeBulkIndexer;


    // Elastic Index 수동 갱신
    @Secured(UserRole.Authority.ADMIN)
    @PostMapping("/v3/stores/_index")
    public ResponseEntity<Void> bulkIndexing() {
        storeBulkIndexer.bulkIndex();
        return ResponseEntity.ok().build();
    }

    // 가게 목록 조회 (ElasticSearch + Redis)
    @GetMapping("/v3/stores")
    public ResponseEntity<PageResponse<StoreDocumentResponseDto>> getStoresV3(@AuthenticationPrincipal AuthUser authUser,
                                                                              @RequestParam(defaultValue = "1") int page,
                                                                              @RequestParam(defaultValue = "10") int size,
                                                                              @RequestParam(defaultValue = "ratingCount") String sort,
                                                                              @RequestParam(defaultValue = "desc") String direction,
                                                                              @RequestParam(required = false) Long categoryId,
                                                                              @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(storeSearchService.getStoresV3(authUser, page, size, sort, direction, categoryId, keyword));
    }
}
