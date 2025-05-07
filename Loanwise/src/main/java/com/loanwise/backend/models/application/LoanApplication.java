package com.loanwise.backend.models.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.loanwise.backend.models.document.Documents;

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
    
    private String applicantName;
    
    private String applicantEmail;
    
    private String applicantPhone;
    
    private BigDecimal loanAmount;
    
    private Integer loanTermMonths;
    
    private String loanPurpose;
    
    private String status;
    
    private LocalDateTime submittedAt;
    
    private LocalDateTime updatedAt;
    
    @DocumentReference(lazy = true)
    private List<Documents> documents;

    // Custom constructor with fields
    public LoanApplication(String applicantName, String applicantEmail, String applicantPhone, 
                        BigDecimal loanAmount, Integer loanTermMonths, String loanPurpose) {
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