package org.example.tablenow.domain.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.image.config.S3Properties;
import org.example.tablenow.domain.image.dto.request.PresignedUrlRequest;
import org.example.tablenow.domain.image.dto.response.PresignedUrlResponse;
import org.example.tablenow.domain.image.enums.ImageDomain;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageService {

    private final S3Presigner presigner;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    private static final String S3_URL_PREFIX = "https://";
    private static final String S3_URL_SUFFIX = ".s3.amazonaws.com/";

    public PresignedUrlResponse generatePresignedUrl(AuthUser authUser, ImageDomain imageDomain, PresignedUrlRequest request) {
        Long userId = authUser.getId();
        String fileExtension = extractExtension(request.getFileName());
        String key = generateKey(imageDomain, userId, fileExtension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .contentType(request.getFileType().getMimeType())
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(s3Properties.getPresignedUrlExpiration()))
                        .putObjectRequest(putObjectRequest)
                        .build()
        );

        String uploadUrl = presignedRequest.url().toString();
        String fileUrl = getFileUrl(key);

        return PresignedUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .fileUrl(fileUrl)
                .build();
    }

    public void delete(String objectPath) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(getImageKey(objectPath))
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (SdkServiceException e) {
            log.error(String.valueOf(e.getCause()));
        }
    }

    private String generateKey(ImageDomain imageDomain, Long userId, String fileExtension) {
        return imageDomain.name().toLowerCase() + "/" + userId + "/" + UUID.randomUUID() + fileExtension;
    }

    private String getFileUrl(String key) {
        return S3_URL_PREFIX + s3Properties.getBucket() + S3_URL_SUFFIX + key;
    }

    private String extractExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.')); // 예: ".png"
    }

    private String getImageKey(String objectPath) {
        try {
            URL url = new URL(objectPath);
            return url.getPath().substring(1);
        } catch (MalformedURLException e) {
            throw new HandledException(ErrorCode.INVALID_IMAGE_URL);
        }
    }
}