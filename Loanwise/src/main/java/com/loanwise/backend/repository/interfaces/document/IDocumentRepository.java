package com.loanwise.backend.repository.interfaces.document;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.loanwise.backend.models.document.Documents;


@Repository
public interface IDocumentRepository extends MongoRepository<Documents, String> {
   List<Documents> findByApplicationId(String applicationId);
}