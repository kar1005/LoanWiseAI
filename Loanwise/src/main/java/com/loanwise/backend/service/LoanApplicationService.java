package com.loanwise.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.loanwise.backend.models.application.LoanApplication;
import com.loanwise.backend.models.document.Documents;
import com.loanwise.backend.repository.interfaces.application.ILoanApplicationRepository;
import com.loanwise.backend.repository.interfaces.document.IDocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationService {

    private final ILoanApplicationRepository loanApplicationRepository;
    private final IDocumentRepository documentRepository;

    /**
     * Submit a new loan application
     * @param application Loan application to submit
     * @return Submitted loan application with generated ID
     */
    public LoanApplication submitApplication(LoanApplication application) {
        log.info("Submitting new loan application for {}", application.getApplicantName());
        application.setStatus("SUBMITTED");
        LoanApplication saved = loanApplicationRepository.save(application);
        log.info("Loan application submitted successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Get a loan application by ID
     * @param applicationId ID of the loan application
     * @return Loan application
     */
    public LoanApplication getApplicationById(String applicationId) {
        log.info("Fetching loan application with ID: {}", applicationId);
        return loanApplicationRepository.findById(applicationId)
            .orElseThrow(() -> {
                log.error("Application not found with ID: {}", applicationId);
                return new IllegalArgumentException("Application not found with ID: " + applicationId);
            });
    }

    /**
     * Get the status of a loan application
     * @param applicationId ID of the loan application
     * @return Status of the loan application
     */
    public String getApplicationStatus(String applicationId) {
        LoanApplication application = getApplicationById(applicationId);
        log.info("Status for application {}: {}", applicationId, application.getStatus());
        return application.getStatus();
    }

    /**
     * Verify a loan application
     * @param applicationId ID of the loan application to verify
     * @return True if verification successful, false otherwise
     */
    public boolean verifyApplication(String applicationId) {
        log.info("Starting verification process for application: {}", applicationId);
        LoanApplication application = getApplicationById(applicationId);
        List<Documents> documents = documentRepository.findByApplicationId(applicationId);
        
        // Basic verification logic
        if (documents.isEmpty()) {
            log.error("No documents found for verification of application: {}", applicationId);
            throw new IllegalStateException("No documents found for verification");
        }
        
        // Update document verification status
        log.info("Verifying {} documents for application: {}", documents.size(), applicationId);
        for (Documents doc : documents) {
            doc.setVerified(true);
        }
        documentRepository.saveAll(documents);
        
        // Update application status
        application.setStatus("VERIFIED");
        loanApplicationRepository.save(application);
        log.info("Application {} successfully verified", applicationId);
        
        return true;
    }
    
    /**
     * Update application status
     * @param applicationId ID of the loan application
     * @param newStatus New status to set
     * @return Updated loan application
     */
    public LoanApplication updateApplicationStatus(String applicationId, String newStatus) {
        log.info("Updating status of application {} to {}", applicationId, newStatus);
        LoanApplication application = getApplicationById(applicationId);
        application.setStatus(newStatus);
        return loanApplicationRepository.save(application);
    }
    public LoanApplication updateApplication(LoanApplication application) {
        log.info("Updating loan application with ID: {}", application.getId());
        
        // Fetch the existing application to validate it exists
        loanApplicationRepository.findById(application.getId())
            .orElseThrow(() -> {
                log.error("Cannot update application. Application not found with ID: {}", application.getId());
                return new IllegalArgumentException("Application not found with ID: " + application.getId());
            });
        
        // The LoanApplication's setStatus method automatically updates the updatedAt timestamp
        // No need to manually set updatedAt here as it should be handled by the model
        
        LoanApplication updated = loanApplicationRepository.save(application);
        log.info("Loan application updated successfully: {}", updated.getId());
        
        return updated;
    }
}