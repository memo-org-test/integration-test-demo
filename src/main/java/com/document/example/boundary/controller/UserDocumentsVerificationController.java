package com.document.example.boundary.controller;

import com.document.example.boundary.controller.dto.DocumentVerificationTaskResponseDto;
import com.document.example.control.DocumentVerificationTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/verify/documents")
@PreAuthorize("hasRole('OPERATOR')")
public class UserDocumentsVerificationController {

    private final DocumentVerificationTaskService documentVerificationTaskService;

    @PutMapping("/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<DocumentVerificationTaskResponseDto> getVerificationTasks() {
        return documentVerificationTaskService.getUnVerifiedDocuments().stream()
                .map(DocumentVerificationTaskResponseDto::of)
                .collect(Collectors.toList());
    }

    @PutMapping("/{documentId}/accept")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void accept(@PathVariable UUID documentId) {
        documentVerificationTaskService.verifyDocument(documentId, true);
    }

    @PutMapping("/{documentId}/verify/reject")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void reject(@PathVariable UUID documentId) {
        documentVerificationTaskService.verifyDocument(documentId, false);
    }
}
