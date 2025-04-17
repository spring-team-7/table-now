package org.example.tablenow.domain.image.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "cloud.aws.s3")
public class S3Properties {

    private final String bucket;
    private final int presignedUrlExpiration;

    public S3Properties(String bucket, int presignedUrlExpiration) {
        this.bucket = bucket;
        this.presignedUrlExpiration = presignedUrlExpiration;
    }
}
