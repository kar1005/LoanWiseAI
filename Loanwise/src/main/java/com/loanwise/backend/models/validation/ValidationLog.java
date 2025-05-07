package com.loanwise.backend.models.validation;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Document(collection = "validation_logs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationLog {
    @Id
    private String id;
    
    private String applicationId;
    private String processingDate;
    private String validationStatus;
    
    private Integer validDocuments;
    private Integer invalidDocuments;
    private List<String> missingDocuments;
    
    // Full JSON result from Python script
    private String fullResult;
}