package com.document.example.entity.repository;

import com.document.example.commons.DocumentState;
import com.document.example.entity.model.UserDocument;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserDocumentRepository extends CrudRepository<UserDocument, UUID> {

    List<UserDocument> findAllByUserIdAndState(UUID userId, DocumentState documentState);

    List<UserDocument> findAllByUserId(UUID userId);

    List<UserDocument> findAllByState(DocumentState documentState);
}
