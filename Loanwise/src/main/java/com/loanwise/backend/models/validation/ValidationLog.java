package com.loanwise.backend.models.validation;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Note: This is not annotated with @Document since it will be embedded in LoanApplication
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationLog {
    @Id
    private String id;
    
    private String reason;
    
    private String ruleTriggered;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}