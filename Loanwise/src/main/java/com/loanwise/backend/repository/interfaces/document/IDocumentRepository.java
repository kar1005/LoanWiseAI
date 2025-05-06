package com.loanwise.backend.repository.interfaces.document;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.loanwise.backend.models.document.Document;


@Repository
public interface IDocumentRepository extends MongoRepository<Document, String> {
    // Add custom query methods if needed
}