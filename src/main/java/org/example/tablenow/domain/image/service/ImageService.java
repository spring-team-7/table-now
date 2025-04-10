package org.example.tablenow.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.image.dto.request.PresignedUrlRequest;
import org.example.tablenow.domain.image.dto.response.PresignedUrlResponse;
import org.example.tablenow.domain.image.enums.ImageDomain;
import org.example.tablenow.global.dto.AuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageService {

    private final S3Presigner presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.presigned-url-expiration}")
    private int expirationMinutes;

    public PresignedUrlResponse generatePresignedUrl(AuthUser authUser, ImageDomain imageDomain, PresignedUrlRequest request) {
        Long userId = authUser.getId();
        String fileExtension = extractExtension(request.getFileName());
        String uuid = UUID.randomUUID().toString();
        String key = imageDomain.name().toLowerCase() + "/" + userId + "/" + uuid + fileExtension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(request.getFileType().getMimeType())
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(expirationMinutes))
                        .putObjectRequest(putObjectRequest)
                        .build()
        );

        String uploadUrl = presignedRequest.url().toString();
        String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + key;

        return PresignedUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .fileUrl(fileUrl)
                .build();
    }

    private String extractExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.')); // 예: ".png"
    }
}