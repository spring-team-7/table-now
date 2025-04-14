package org.example.tablenow.domain.image.service;

import org.example.tablenow.domain.image.dto.request.PresignedUrlRequest;
import org.example.tablenow.domain.image.dto.response.PresignedUrlResponse;
import org.example.tablenow.domain.image.enums.FileType;
import org.example.tablenow.domain.image.enums.ImageDomain;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private S3Presigner presigner;
    @Mock
    private S3Client s3Client;

    private final Long userId = 1L;
    private final AuthUser authUser = new AuthUser(userId, "user@test.com", UserRole.ROLE_USER, "일반회원");
    private final String bucketName = "test-bucket";

    @Nested
    class 이미지업로드_Url_발급 {

        @BeforeEach
        void setup() {
            ReflectionTestUtils.setField(imageService, "bucketName", bucketName);
            ReflectionTestUtils.setField(imageService, "expirationMinutes", 3);
        }

        @Test
        void presignedUrl_발급_성공() throws Exception {
            // given
            PresignedUrlRequest request = PresignedUrlRequest.builder()
                    .fileName("test.png")
                    .fileType(FileType.PNG)
                    .build();

            PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);

            URL mockUrl = new URL("https://upload-url.com");
            when(presignedRequest.url()).thenReturn(mockUrl);

            when(presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                    .thenReturn(presignedRequest);

            // when
            PresignedUrlResponse response = imageService.generatePresignedUrl(authUser, ImageDomain.USER, request);

            // then
            assertAll(
                    () -> assertTrue(response.getUploadUrl().startsWith("https://upload-url.com")),
                    () -> assertTrue(response.getFileUrl().startsWith("https://" + bucketName + ".s3.amazonaws.com/" + "user/1/")),
                    () -> assertTrue(response.getFileUrl().endsWith(".png"))
            );
        }
    }

    @Nested
    class S3_이미지_리소스_삭제 {

        @Test
        void URL이_적절하지_않으면_예외_발생() {
            // given
            String invalidUrl = "invalid-url";

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    imageService.delete(invalidUrl)
            );
            assertEquals(ErrorCode.INVALID_IMAGE_URL.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 삭제_성공() {
            // given
            String imageUrl = "https://" + bucketName + ".s3.amazonaws.com/user/1/test.png";

            // when
            imageService.delete(imageUrl);

            // then
            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        }
    }
}