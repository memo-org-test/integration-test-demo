package com.document.example.control;

import com.document.example.control.dto.S3UrlAccess;
import com.document.example.control.dto.UserDocumentDto;
import com.document.example.entity.repository.UserDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDocumentService {

    private final UserDocumentRepository userDocumentRepository;

    private final UserDocumentsS3Service userDocumentsS3Service;

    public List<S3UrlAccess<UserDocumentDto>> getUserDocuments(UUID userId) {
        return userDocumentRepository.findAllByUserId(userId).stream()
                .map(UserDocumentDto::of)
                .map(userDocumentsS3Service::wrapWithS3Access)
                .collect(Collectors.toList());
    }
}
