package com.loanwise.backend.models.validation;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "validation_logs")
@Data
public class ValidationLog {

    @Id
    private String id;
    
    private String applicationId;
    
    private Map<String, Object> validationResults;
    
    private String validationStatus;
    
    private String timestamp;
}