package org.example.tablenow.domain.settlement.controller;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.settlement.dto.response.SettlementOperationResponseDto;
import org.example.tablenow.domain.settlement.dto.response.SettlementResponseDto;
import org.example.tablenow.domain.settlement.dto.response.SettlementSummaryPageDto;
import org.example.tablenow.domain.settlement.service.SettlementService;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SettlementController {

    private final SettlementService settlementService;

    // 결제 완료 되었고, settlement repository에 존재하지 않는 결제들을
    // READY 상태로 settlement repository에 저장
    @Secured(UserRole.Authority.ADMIN)
    @PostMapping("/v1/settlements/pending")
    public ResponseEntity<SettlementOperationResponseDto> registerPendingSettlements() {
        return ResponseEntity.ok(settlementService.registerPendingSettlements());
    }

    // settlement repository에 저장된 결제들의 상태(READY -> DONE)를 변경
    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/v1/settlements/pending/complete")
    public ResponseEntity<SettlementOperationResponseDto> completePendingSettlements() {
        return ResponseEntity.ok(settlementService.completePendingSettlements());
    }

    @Secured(UserRole.Authority.ADMIN)
    @GetMapping("/v1/settlements")
    public ResponseEntity<Page<SettlementResponseDto>> getAllSettlements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(settlementService.getAllSettlements(page, size));
    }

    @Secured(UserRole.Authority.ADMIN)
    @GetMapping("/v1/settlements/{settlementId}")
    public ResponseEntity<SettlementResponseDto> getSettlement(@PathVariable Long settlementId) {
        return ResponseEntity.ok(settlementService.getSettlement(settlementId));
    }

    @Secured(UserRole.Authority.OWNER)
    @GetMapping("/v1/owner/settlements")
    public ResponseEntity<SettlementSummaryPageDto> getMyStoreSettlements(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(settlementService.getMyStoreSettlements(authUser, page, size));
    }

    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/v1/settlements/{settlementId}/cancel")
    public ResponseEntity<SettlementOperationResponseDto> cancelSettlement(@PathVariable Long settlementId) {
        return ResponseEntity.ok(settlementService.cancelSettlement(settlementId));
    }
}
