package org.example.tablenow.global.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;
    private final boolean first;
    private final boolean sorted;

    @Builder
    @JsonCreator
    public PageResponse(@JsonProperty("content") List<T> content,
                        @JsonProperty("pageNumber") int pageNumber,
                        @JsonProperty("pageSize") int pageSize,
                        @JsonProperty("totalElements") long totalElements,
                        @JsonProperty("totalPages") int totalPages,
                        @JsonProperty("last") boolean last,
                        @JsonProperty("first") boolean first,
                        @JsonProperty("sorted") boolean sorted) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
        this.first = first;
        this.sorted = sorted;
    }

    public PageResponse(Page<T> page) {
        this(page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst(),
                page.getSort().isSorted()
        );
    }
}
