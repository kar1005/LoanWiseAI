package com.loanwise.backend.repository.interfaces.validation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.loanwise.backend.models.validation.ValidationLog;

@Repository
public interface IValidationLogRepository extends MongoRepository<ValidationLog, String> {
    List<ValidationLog> findByValidationStatus(String validationStatus);
    Optional<ValidationLog> findByApplicationId(Long applicationId);
}