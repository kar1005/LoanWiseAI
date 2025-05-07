package com.loanwise.backend.models.document;

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
@Document(collection = "documents")
public class Documents {
    
    @Id
    private String id;
    
    private String applicationId;
    
    private String documentType;
    
    private String cloudinaryUrl;
    
    private String cloudinaryPublicId;
    
    private boolean verified;
    
    private LocalDateTime uploadedAt;
    
    private LocalDateTime verifiedAt;

    // Custom constructor with core fields
    public Documents(String applicationId, String documentType, String cloudinaryUrl, String cloudinaryPublicId) {
        this.applicationId = applicationId;
        this.documentType = documentType;
        this.cloudinaryUrl = cloudinaryUrl;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.uploadedAt = LocalDateTime.now();
        this.verified = false;
    }
    
    // Custom setter for verified field to also update verifiedAt timestamp
    public void setVerified(boolean verified) {
        this.verified = verified;
        if (verified) {
            this.verifiedAt = LocalDateTime.now();
        }
    }
}