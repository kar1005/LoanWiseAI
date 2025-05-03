package com.loanwise.backend.models.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.loanwise.backend.models.user.User;
import com.loanwise.backend.models.validation.ValidationLog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "loan_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {
    @Id
    private String id;
    
    @DBRef
    private User user;
    
    private Double income;
    private Integer age;
    
    private Integer creditScore;
    
    private String employmentStatus;
    
    private Double requestedAmount;
    
    private Double existingLoans;
    
    private String status; // Approved, Rejected, Needs Review
    
    private Double confidenceScore;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Documents can be embedded directly within the loan application
    private List<Document> documents;
    
    private List<ValidationLog> validationLogs;
    
}