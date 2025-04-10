package org.example.tablenow.domain.image.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PresignedUrlResponse {
    private final String uploadUrl;
    private final String fileUrl;

    @Builder
    public PresignedUrlResponse(String uploadUrl, String fileUrl) {
        this.uploadUrl = uploadUrl;
        this.fileUrl = fileUrl;
    }
}
