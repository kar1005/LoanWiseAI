package com.loanwise.backend.models.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.loanwise.backend.models.application.LoanApplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;
    
    private String name;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Using @DBRef to reference loan applications
    // Alternative approach would be to embed loan applications directly in the user document
    @DBRef
    private List<LoanApplication> loanApplications;
}