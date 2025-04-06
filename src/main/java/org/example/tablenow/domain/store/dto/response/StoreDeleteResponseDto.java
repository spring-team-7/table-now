package org.example.tablenow.domain.store.dto.response;

public class StoreDeleteResponseDto {
    private final Long storeId;
    private final String message;

    private StoreDeleteResponseDto(Long storeId, String message) {
        this.storeId = storeId;
        this.message = message;
    }

    public static StoreDeleteResponseDto fromStore(Long storeId) {
        return new StoreDeleteResponseDto(storeId, "삭제되었습니다.");
    }
}
