package com.document.example.control;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.document.example.control.dto.S3UrlAccess;
import com.document.example.control.dto.UserDocumentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserDocumentsS3Service {

    private final AmazonS3 amazonS3;

    @Value("${s3.userDocuments.bucketName}")
    private String bucketName;

    public <T extends UserDocumentDto> S3UrlAccess<T> wrapWithS3Access(T userDocumentDto) {
        return S3UrlAccess.<T>builder()
                .value(userDocumentDto)
                .preSignedUrl(generatePreSignedUrl(userDocumentDto))
                .build();
    }

    private <T extends UserDocumentDto> URL generatePreSignedUrl(T userDocumentDto) {
        return amazonS3.generatePresignedUrl(bucketName, createS3Path(userDocumentDto), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    }

    public PutObjectResult uploadToS3(UserDocumentDto userDocumentDto, InputStream userDocumentInputStream) throws IOException {
        try (InputStream ignored = userDocumentInputStream) {
            final ObjectMetadata metadata = createObjectMetaData(userDocumentDto);
            return amazonS3.putObject(bucketName, createS3Path(userDocumentDto), userDocumentInputStream, metadata);
        } catch (Exception e) {
            log.error("failed to upload to s3Bucket={}", bucketName, e);
            throw e;
        }

    }

    private ObjectMetadata createObjectMetaData(UserDocumentDto userDocumentDto) {
        final ObjectMetadata metadata = new ObjectMetadata();
        final HashMap<String, String> userMetadata = new HashMap<>();
        userMetadata.put("documentType", userDocumentDto.getDocumentType().name());
        metadata.setUserMetadata(userMetadata);
        return metadata;
    }

    private String createS3Path(UserDocumentDto userDocument) {
        return new StringBuilder(userDocument.getUserId().toString())
                .append("/")
                .append(userDocument.getDocumentId())
                .toString();
    }
}
