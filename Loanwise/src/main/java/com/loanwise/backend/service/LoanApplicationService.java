package com.loanwise.backend.service;

import com.loanwise.backend.repository.interfaces.application.ILoanApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import com.loanwise.backend.models.application.LoanApplication;

@Service
public class LoanApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationService.class);

    @Autowired
    private ILoanApplicationRepository loanApplicationRepository;
    
    @Autowired
    private DocumentProcessingService documentProcessingService;
    
    @Autowired
    private PythonScriptService pythonScriptService;

    @Transactional
    public LoanApplication processLoanApplication(LoanApplication application, Map<String, MultipartFile> documents) {
        try {
            logger.info("Processing loan application for: {} {}", application.getApplicantName());
            
            // 1. Save the application first to get an ID
            LoanApplication savedApplication = loanApplicationRepository.save(application);
            logger.info("Saved loan application with ID: {}", savedApplication.getId());
            
            // 2. Process and upload documents
            Map<String, String> documentUrls = new HashMap<>();
            try {
                documentUrls = documentProcessingService.processDocuments(savedApplication.getId(), documents);
                logger.info("Successfully processed and uploaded documents for application ID: {}", savedApplication.getId());
            } catch (Exception e) {
                logger.error("Failed to process documents for application ID: {}", savedApplication.getId(), e);
                throw new RuntimeException("Document processing failed: " + e.getMessage(), e);
            }
            
            // 3. Update application with document URLs
            savedApplication.setAadharUrl(documentUrls.get("aadharUrl"));
            savedApplication.setPanUrl(documentUrls.get("panUrl"));
            savedApplication.setItrUrl(documentUrls.get("itrUrl"));
            
            // 4. Update the application in the database
            savedApplication = loanApplicationRepository.save(savedApplication);
            logger.info("Updated loan application with document URLs, ID: {}", savedApplication.getId());
            
            // 5. Run Python script for ML analysis
            try {
                boolean scriptResult = pythonScriptService.runLoanAnalysisScript(savedApplication.getId());
                logger.info("Python script execution result: {}", scriptResult);
            } catch (Exception e) {
                logger.error("Failed to run Python script for application ID: {}", savedApplication.getId(), e);
                throw new RuntimeException("Python script execution failed: " + e.getMessage(), e);
            }
            
            // 6. Create and return response
            LoanApplication response = new LoanApplication();
            response.setId(savedApplication.getId());
            response.setStatus("SUBMITTED");
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error in loan application processing", e);
            throw new RuntimeException("Failed to process loan application: " + e.getMessage(), e);
        }
    }
    
    public LoanApplication getLoanApplicationById(String id) {
        try {
            LoanApplication application = loanApplicationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + id));
            
            LoanApplication response = new LoanApplication();
            response.setId(application.getId());
            response.setStatus(application.getStatus());
            
            return response;
        } catch (Exception e) {
            logger.error("Error retrieving loan application with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve loan application: " + e.getMessage(), e);
        }
    }
}