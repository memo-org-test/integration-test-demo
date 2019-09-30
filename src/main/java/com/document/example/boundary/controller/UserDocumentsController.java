package com.document.example.boundary.controller;

import com.document.example.boundary.controller.dto.UploadDocumentRequestDto;
import com.document.example.commons.security.UserPrincipal;
import com.document.example.control.UserDocumentService;
import com.document.example.boundary.controller.dto.UserDocumentResponseDto;
import com.document.example.control.UserDocumentUploadService;
import com.document.example.control.dto.UserDocumentUploadTaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/documents")
@PreAuthorize("hasAnyRole('STAFF','TENANT')")
public class UserDocumentsController {

    private final UserDocumentUploadService userDocumentUploadService;

    private final UserDocumentService userDocumentService;

    @GetMapping("/upload/tasks")
    public ResponseEntity<List<UserDocumentUploadTaskDto>> getUserDocumentUploadTasks(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Optional.ofNullable(userDocumentUploadService.getUploadTasks(userPrincipal.getUserId()))
                .filter(taskList -> !taskList.isEmpty())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{documentId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void uploadDocument(@PathVariable UUID documentId,
                               @RequestBody UploadDocumentRequestDto uploadDocumentRequestDto,
                               @AuthenticationPrincipal UserPrincipal userPrincipal) throws IOException {
        userDocumentUploadService.uploadDocument(uploadDocumentRequestDto.toUserDocumentUploadTaskDto(userPrincipal.getUserId(), documentId));
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<UserDocumentResponseDto> getDocument(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userDocumentService.getUserDocuments(userPrincipal.getUserId()).stream()
                .map(UserDocumentResponseDto::of)
                .collect(Collectors.toList());
    }

}
