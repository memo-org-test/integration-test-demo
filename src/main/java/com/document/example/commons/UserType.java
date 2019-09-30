package com.document.example.commons;

import com.document.example.entity.model.DocumentType;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserType {
    STAFF(      true,   ImmutableList.of(DocumentType.ID, DocumentType.CV)),
    TENANT(     true,   ImmutableList.of(DocumentType.ID, DocumentType.PROOF_OF_INCOME)),
    OPERATOR(   false,  Collections.emptyList());

    private final boolean requiredToUploadDocuments;

    private final List<DocumentType> requiredDocuments;
}
