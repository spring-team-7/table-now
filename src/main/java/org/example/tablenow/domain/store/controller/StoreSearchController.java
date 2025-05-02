package org.example.tablenow.domain.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.request.StoreSearchRequestDto;
import org.example.tablenow.domain.store.dto.response.StoreDocumentResponseDto;
import org.example.tablenow.domain.store.service.StoreElasticsearchIndexer;
import org.example.tablenow.domain.store.service.StoreSearchService;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.dto.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "가게 검색 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoreSearchController {

    private final StoreSearchService storeSearchService;
    private final StoreElasticsearchIndexer storeElasticsearchIndexer;

    // Elastic Index 수동 갱신
    @Operation(summary = "Elastic 인덱스 수동 갱신 (관리자)")
    @Secured(UserRole.Authority.ADMIN)
    @PostMapping("/v3/admin/stores/index")
    public ResponseEntity<Void> reindexAllStores() {
        storeElasticsearchIndexer.reindexAllStores();
        return ResponseEntity.ok().build();
    }

    // 가게 검색 v3 (ElasticSearch + Redis)
    @Operation(summary = "가게 검색 V3 (Elastic + Redis)")
    @GetMapping("/v3/stores")
    public ResponseEntity<PageResponse<StoreDocumentResponseDto>> getStoresV3(@AuthenticationPrincipal AuthUser authUser,
                                                                              @ModelAttribute StoreSearchRequestDto request) {
        return ResponseEntity.ok(storeSearchService.getStoresV3(authUser, request.getPage(), request.getSize(), request.getSort(), request.getDirection(), request.getCategoryId(), request.getKeyword()));
    }
}
