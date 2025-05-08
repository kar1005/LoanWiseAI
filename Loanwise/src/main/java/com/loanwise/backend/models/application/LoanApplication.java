package com.loanwise.backend.models.application;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "loan_applications")
public class LoanApplication {
    
    @Id
    private String id;
    
    private String userId;  // Added userId field to track which user submitted the application
    
    private String applicantName;
    
    private String applicantEmail;
    
    private String applicantPhone;

    private Double annualIncome;
    
    private Double loanAmount;
    
    private Integer loanTermMonths;
    
    private String loanPurpose;
    
    private String status;
    
    private LocalDateTime submittedAt;
    
    private LocalDateTime updatedAt;

    private String employmentStatus;
    

    private String aadharUrl;

    private String panUrl;

    private String itrUrl;

    private String bankStatementUrl; 

    private String address;

    private String city;

    private String state;

    private String zipCode;


    public LoanApplication(String userId, String applicantName, String applicantEmail, String applicantPhone,  Double loanAmount, Integer loanTermMonths, String loanPurpose) {
        this.userId = userId;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
        this.loanAmount = loanAmount;
        this.loanTermMonths = loanTermMonths;
        this.loanPurpose = loanPurpose;
        this.status = "NEW";
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Custom setter for status to update the timestamp automatically
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}