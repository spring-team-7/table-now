package org.example.tablenow.domain.settlement.controller;

import jakarta.validation.constraints.Positive;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class SettlementController {

    private final SettlementService settlementService;

    @Secured(UserRole.Authority.OWNER)
    @GetMapping("/v1/owner/settlements")
    public ResponseEntity<SettlementSummaryPageDto> getMyStoreSettlements(
            @AuthenticationPrincipal AuthUser authUser,
            @Positive @RequestParam(defaultValue = "1") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(settlementService.getMyStoreSettlements(authUser, page, size));
    }

    @Secured(UserRole.Authority.ADMIN)
    @GetMapping("/v1/settlements")
    public ResponseEntity<Page<SettlementResponseDto>> getAllSettlements(
            @Positive @RequestParam(defaultValue = "1") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(settlementService.getAllSettlements(page, size));
    }

    @Secured(UserRole.Authority.ADMIN)
    @GetMapping("/v1/settlements/{settlementId}")
    public ResponseEntity<SettlementResponseDto> getSettlement(@PathVariable Long settlementId) {
        return ResponseEntity.ok(settlementService.getSettlement(settlementId));
    }

    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/v1/settlements/{settlementId}/cancel")
    public ResponseEntity<SettlementOperationResponseDto> cancelSettlement(@PathVariable Long settlementId) {
        return ResponseEntity.ok(settlementService.cancelSettlement(settlementId));
    }
}
