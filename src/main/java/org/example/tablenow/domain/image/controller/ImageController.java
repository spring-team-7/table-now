package org.example.tablenow.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.image.dto.request.PresignedUrlRequest;
import org.example.tablenow.domain.image.dto.response.PresignedUrlResponse;
import org.example.tablenow.domain.image.enums.ImageDomain;
import org.example.tablenow.domain.image.service.ImageService;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이미지 API")
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "Presigned URL 발급")
    @PostMapping("/v1/images/upload/{domain}")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("domain") String domain,
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        ImageDomain imageDomain = ImageDomain.of(domain);
        return ResponseEntity.ok(imageService.generatePresignedUrl(authUser, imageDomain, request));
    }
}
