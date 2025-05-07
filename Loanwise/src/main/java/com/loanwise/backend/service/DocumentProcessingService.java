package com.loanwise.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanwise.backend.models.application.LoanApplication;
import com.loanwise.backend.models.validation.ValidationLog;
import com.loanwise.backend.repository.interfaces.application.ILoanApplicationRepository;
import com.loanwise.backend.repository.interfaces.validation.IValidationLogRepository;

/**
 * Service for processing and validating loan application documents
 */
@Service
public class DocumentProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);

    @Value("${python.script.path}")
    private String pythonScriptPath;
    
    @Value("${python.executable.path:python}")
    private String pythonExecutablePath;
    
    @Value("${config.file.path}")
    private String configFilePath;

    @Autowired
    private ILoanApplicationRepository loanApplicationRepository;
    
    @Autowired
    private IValidationLogRepository validationLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    public CompletableFuture<ValidationLog> processDocumentsAsync(String applicationId) {
        return CompletableFuture.supplyAsync(() -> processDocuments(applicationId));
    }

    public ValidationLog processDocuments(String applicationId) {
        logger.info("Processing documents for application ID: {}", applicationId);
        
        try {
            // Fetch loan application to get document IDs
            LoanApplication application = loanApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new IllegalArgumentException("Loan application not found"));
            
            // Get document IDs from the application
            List<String> documentIds = application.getDocumentIds();
            if (documentIds == null || documentIds.isEmpty()) {
                throw new IllegalStateException("No documents found for processing");
            }
            
            // Construct command to execute Python script
            String documentIdsParam = String.join(",", documentIds);
            List<String> command = new ArrayList<>();
            command.add(pythonExecutablePath);
            command.add(pythonScriptPath);
            command.add("--application_id");
            command.add(applicationId);
            command.add("--document_ids");
            command.add(documentIdsParam);
            command.add("--output");
            command.add("json");
            command.add("--config");
            command.add(configFilePath);
            
            logger.debug("Executing command: {}", String.join(" ", command));
            
            // Execute the Python script
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            
            // Read the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Get the exit code
            int exitCode = process.waitFor();
            logger.debug("Python script execution completed with exit code: {}", exitCode);
            
            // Parse the result
            String jsonOutput = output.toString().trim();
            Map<String, Object> resultMap = objectMapper.readValue(jsonOutput, Map.class);
            
            // Create and save the validation result
            ValidationLog result = createValidationResult(applicationId, resultMap);
            validationLogRepository.save(result);
            
            // Update loan application with extracted information
            updateLoanApplicationWithExtractedInfo(application, resultMap);
            
            return result;
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing Python script: {}", e.getMessage(), e);
            throw new RuntimeException("Document processing failed", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private ValidationLog createValidationResult(String applicationId, Map<String, Object> resultMap) {
        ValidationLog result = new ValidationLog();
        result.setApplicationId(applicationId);
        result.setProcessingDate((String) resultMap.get("processing_date"));
        result.setValidationStatus((String) resultMap.get("validation_status"));
        
        // Get validation summary
        Map<String, Object> summary = (Map<String, Object>) resultMap.get("validation_summary");
        if (summary != null) {
            result.setValidDocuments((Integer) summary.get("valid_documents"));
            result.setInvalidDocuments((Integer) summary.get("invalid_documents"));
            result.setMissingDocuments((List<String>) summary.get("missing_documents"));
        }
        
        // Save the complete result as JSON
        try {
            result.setFullResult(objectMapper.writeValueAsString(resultMap));
        } catch (Exception e) {
            logger.error("Error serializing result: {}", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Update loan application with extracted information
     *
     * @param application the loan application
     * @param resultMap the result map from Python script
     */
    @SuppressWarnings("unchecked")
    private void updateLoanApplicationWithExtractedInfo(LoanApplication application, Map<String, Object> resultMap) {
        Map<String, Object> extractedInfo = (Map<String, Object>) resultMap.get("extracted_info");
        if (extractedInfo == null) {
            return;
        }
        
        // Update with extracted information
        if (extractedInfo.containsKey("aadhaar_number")) {
            application.setAadhaarNumber((String) extractedInfo.get("aadhaar_number"));
        }
        
        if (extractedInfo.containsKey("pan_number")) {
            application.setPanNumber((String) extractedInfo.get("pan_number"));
        }
        
        if (extractedInfo.containsKey("age")) {
            application.setAge((Integer) extractedInfo.get("age"));
        }
        
        if (extractedInfo.containsKey("annual_income")) {
            application.setAnnualIncome((Double) extractedInfo.get("annual_income"));
        }
        
        if (extractedInfo.containsKey("existing_loans")) {
            application.setExistingLoans((List<String>) extractedInfo.get("existing_loans"));
        }
        
        // Update document validation status
        application.setDocumentsValidated(true);
        application.setDocumentValidationStatus((String) resultMap.get("validation_status"));
        
        // Save the updated application
        loanApplicationRepository.save(application);
    }
}