package org.example.tablenow.domain.store.dto.response;

import lombok.Builder;

public class StoreRankingResponseDto {
    private final Integer rank;
    private final String keyword;
    private final Integer score;

    @Builder
    private StoreRankingResponseDto(Integer rank, String keyword, Integer score) {
        this.rank = rank;
        this.keyword = keyword;
        this.score = score;
    }
}
