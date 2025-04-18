package org.example.tablenow.domain.settlement.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;
import org.example.tablenow.domain.payment.repository.PaymentRepository;
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

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;

    @Transactional
    public SettlementOperationResponseDto registerPendingSettlements() {

        List<Payment> payments = paymentRepository.findAll();

        int createdCount = 0;

        for (Payment payment : payments) {
            if (payment.getStatus() != PaymentStatus.DONE) continue;
            if (settlementRepository.existsByPaymentId(payment.getId())) continue;

            Settlement settlement = Settlement.builder()
                    .payment(payment)
                    .amount(payment.getPrice())
                    .status(SettlementStatus.READY)
                    .build();

            settlementRepository.save(settlement);
            createdCount++;
        }

        return new SettlementOperationResponseDto(createdCount, SettlementStatus.READY);
    }

    @Transactional
    public SettlementOperationResponseDto completePendingSettlements() {

        List<Settlement> readySettlements = settlementRepository.findAllByStatus(SettlementStatus.READY);

        int completedCount = 0;

        for (Settlement settlement : readySettlements) {
            settlement.done();
            completedCount++;
        }

        return new SettlementOperationResponseDto(completedCount, SettlementStatus.DONE);
    }

    @Transactional(readOnly = true)
    public Page<SettlementResponseDto> getAllSettlements(int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        return settlementRepository.findAll(pageable)
                .map(SettlementResponseDto::from);
    }

    @Transactional(readOnly = true)
    public SettlementResponseDto getSettlement(Long id) {

        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.SETTLEMENT_NOT_FOUND));

        return SettlementResponseDto.from(settlement);
    }

    @Transactional(readOnly = true)
    public SettlementSummaryPageDto getMyStoreSettlements(AuthUser authUser, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Settlement> settlements = settlementRepository.findByStoreOwnerId(authUser.getId(), pageable);

        Page<SettlementResponseDto> dtoPage = settlements.map(SettlementResponseDto::from);

        int doneAmount = 0;
        int pendingAmount = 0;
        int canceledAmount = 0;

        for (Settlement settlement : settlements) {
            switch (settlement.getStatus()) {
                case DONE -> doneAmount += settlement.getAmount();
                case READY -> pendingAmount += settlement.getAmount();
                case CANCELED -> canceledAmount += settlement.getAmount();
            }
        }

        return SettlementSummaryPageDto.of(dtoPage, doneAmount, pendingAmount, canceledAmount);
    }


    @Transactional
    public SettlementOperationResponseDto cancelSettlement(Long id) {

        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.SETTLEMENT_NOT_FOUND));

        if (settlement.getStatus() == SettlementStatus.CANCELED) {
            throw new HandledException(ErrorCode.ALREADY_CANCELED);
        }

        if (settlement.getStatus() != SettlementStatus.DONE) {
            throw new HandledException(ErrorCode.INVALID_SETTLEMENT_STATUS);
        }

        settlement.cancel();

        return new SettlementOperationResponseDto(1, SettlementStatus.CANCELED);
    }
}
