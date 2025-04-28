package org.example.tablenow.domain.settlement.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.settlement.dto.response.SettlementOperationResponseDto;
import org.example.tablenow.domain.settlement.dto.response.SettlementResponseDto;
import org.example.tablenow.domain.settlement.dto.response.SettlementSummaryPageDto;
import org.example.tablenow.domain.settlement.entity.Settlement;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;
import org.example.tablenow.domain.settlement.repository.SettlementRepository;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    @Transactional(readOnly = true)
    public Page<SettlementResponseDto> getAllSettlements(int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        return settlementRepository.findAll(pageable)
                .map(SettlementResponseDto::fromSettlement);
    }

    @Transactional(readOnly = true)
    public SettlementResponseDto getSettlement(Long id) {

        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.SETTLEMENT_NOT_FOUND));

        return SettlementResponseDto.fromSettlement(settlement);
    }

    @Transactional(readOnly = true)
    public SettlementSummaryPageDto getMyStoreSettlements(AuthUser authUser, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Settlement> settlements = settlementRepository.findByStoreOwnerId(authUser.getId(), pageable);
        Page<SettlementResponseDto> dtoPage = settlements.map(SettlementResponseDto::fromSettlement);

        Map<SettlementStatus, Integer> totalAmountByStatus = calculateAmountByStatus(settlements.getContent());

        int doneAmount = totalAmountByStatus.getOrDefault(SettlementStatus.DONE, 0);
        int readyAmount = totalAmountByStatus.getOrDefault(SettlementStatus.READY, 0);
        int canceledAmount = totalAmountByStatus.getOrDefault(SettlementStatus.CANCELED, 0);

        return SettlementSummaryPageDto.of(doneAmount, readyAmount, canceledAmount, dtoPage);
    }

    @Transactional
    public SettlementOperationResponseDto cancelSettlement(Long id) {

        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.SETTLEMENT_NOT_FOUND));

        settlement.cancel();

        return new SettlementOperationResponseDto(1, SettlementStatus.CANCELED);
    }

    private Map<SettlementStatus, Integer> calculateAmountByStatus(List<Settlement> settlements) {

        return settlements.stream()
                .collect(Collectors.groupingBy(
                        Settlement::getStatus,
                        Collectors.summingInt(Settlement::getAmount)
                ));
    }
}
