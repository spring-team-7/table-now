package org.example.tablenow.domain.store.dto.response;

import lombok.Getter;

@Getter
public class StoreDeleteResponseDto {
    private final Long storeId;
    private final String message;

    public StoreDeleteResponseDto(Long storeId, String message) {
        this.storeId = storeId;
        this.message = message;
    }

    public static StoreDeleteResponseDto fromStore(Long storeId) {
        return new StoreDeleteResponseDto(storeId, "삭제되었습니다.");
    }
}
