package com.loanwise.backend.models.application;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    
    private String applicantName;
    private String email;
    private String phoneNumber;
    
    // Document related fields
    private List<String> documentIds; // Cloudinary public IDs
    private boolean documentsValidated;
    private String documentValidationStatus;
    
    // Extracted information
    private String aadhaarNumber;
    private String panNumber;
    private Integer age;
    private Double annualIncome;
    private List<String> existingLoans;
    
    // Loan details
    private Double loanAmount;
    private String loanPurpose;
    
    // Application status
    private String status; // PENDING, UNDER_REVIEW, APPROVED, REJECTED
    private Date applicationDate;
    private Date lastUpdated;
    
    // Additional fields
    private Double creditScore;
    private String remarks;
    
}