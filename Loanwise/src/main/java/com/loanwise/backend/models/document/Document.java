package com.loanwise.backend.models.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    private String id;
    
    private String fileName;
    
    private String type; // PAN, Aadhaar, Salary Slip, Bank Statement, ITR
    
    private String filePath;
    
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}