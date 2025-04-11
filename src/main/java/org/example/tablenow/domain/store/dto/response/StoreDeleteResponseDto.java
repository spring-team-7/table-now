package org.example.tablenow.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreDeleteResponseDto {
    private final Long storeId;
    private final String message;

    @Builder
    public StoreDeleteResponseDto(Long storeId, String message) {
        this.storeId = storeId;
        this.message = message;
    }

    public static StoreDeleteResponseDto fromStore(Long storeId) {
        return StoreDeleteResponseDto.builder()
                .storeId(storeId)
                .message("삭제되었습니다.")
                .build();
    }
}
