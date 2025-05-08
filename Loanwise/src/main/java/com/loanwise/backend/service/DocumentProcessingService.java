package com.loanwise.backend.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    /**
     * Process and upload documents for a loan application
     * 
     * @param applicationId The ID of the loan application
     * @param documents Map of document types to MultipartFile objects
     * @return Map of document types to their URLs in cloud storage
     * @throws IOException If file processing fails
     */
    public Map<String, String> processDocuments(String applicationId, Map<String, MultipartFile> documents) throws IOException, Exception {
        logger.info("Processing documents for application ID: {}", applicationId);
        
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("No documents provided for processing");
        }
        
        Map<String, String> documentsUrls = new HashMap<>();
        
        // Process each document file
        for (Map.Entry<String, MultipartFile> entry : documents.entrySet()) {
            String documentType = entry.getKey();
            MultipartFile file = entry.getValue();
            
            if (file == null || file.isEmpty()) {
                logger.warn("Empty or null file for document type: {}", documentType);
                throw new IllegalArgumentException("Empty or null file for document type: " + documentType);
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || 
                !(contentType.equals("application/pdf") || 
                contentType.startsWith("image/jpeg") ||
                contentType.startsWith("image/png") ||
                contentType.startsWith("image/jpg"))) {
                
                logger.error("Invalid file type for document: {}, contentType: {}", documentType, contentType);
                throw new IllegalArgumentException("Invalid file type for " + documentType + 
                                                ". Supported types: PDF, JPEG, PNG");
            }
            
            // Create a unique filename for the document
            String filename = String.format("application_%s_%s_%s", 
                                        applicationId,
                                        documentType,
                                        file.getOriginalFilename());
            
            try {
                // Upload to CloudStore
                String url = cloudinaryService.uploadFile(file);
                logger.info("Document uploaded successfully to CloudStore for application ID: {}, type: {}", 
                        applicationId, documentType);
                documentsUrls.put(documentType, url);
            } catch (Exception cse) {
                // Can be replaced with more specific catch clauses capturing specific exceptions
                logger.error("Failed to upload {} to CloudStore for application ID: {}", documentType, applicationId, cse);
                throw new RuntimeException("Failed to upload " + documentType + " - " + cse.getMessage(), cse);
            } 
        }
        
        return documentsUrls;
    }
}