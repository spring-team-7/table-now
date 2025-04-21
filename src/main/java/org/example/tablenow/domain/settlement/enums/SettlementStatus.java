package org.example.tablenow.domain.settlement.enums;

public enum SettlementStatus {
    READY("정산 대기"),
    DONE("정산 완료"),
    CANCELED("정산 취소");

    private final String description;

    SettlementStatus(String description){
        this.description = description;
    }
}
