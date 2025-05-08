package com.loanwise.backend.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.loanwise.backend.models.application.LoanApplication;
import com.loanwise.backend.repository.interfaces.application.ILoanApplicationRepository;

@Service
public class PythonScriptService {

    private static final Logger logger = LoggerFactory.getLogger(PythonScriptService.class);

    @Value("${python.script.path}")
    private String pythonScriptPath;
    
    @Value("${python.executable.path:python3}")
    private String pythonExecutablePath;
    
    @Value("${python.script.timeout:60}")
    private int scriptTimeoutSeconds;
    
    @Autowired
    private ILoanApplicationRepository loanApplicationRepository;

    /**
     * Run the loan analysis Python script for a specific loan application
     * 
     * @param applicationId The ID of the loan application
     * @return True if the script executed successfully, false otherwise
     */
    public boolean runLoanAnalysisScript(String applicationId) {
        logger.info("Running loan analysis script for application ID: {}", applicationId);
        
        // Check if application exists
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + applicationId));
        
        // Check if Python script path exists
        File scriptFile = new File(pythonScriptPath);
        if (!scriptFile.exists() || !scriptFile.isFile()) {
            logger.error("Python script not found at path: {}", pythonScriptPath);
            throw new RuntimeException("Python script not found at path: " + pythonScriptPath);
        }
        
        try {
            // Build command to execute Python script
            List<String> command = new ArrayList<>();
            command.add(pythonExecutablePath);
            command.add(pythonScriptPath);
            command.add("--application_id");
            command.add(String.valueOf(applicationId));
            
            // Add additional parameters as needed
            command.add("--first_name");
            command.add(application.getApplicantName());
            command.add("--loan_amount");
            command.add(String.valueOf(application.getLoanAmount()));
            command.add("--annual_income");
            command.add(String.valueOf(application.getAnnualIncome()));
            command.add("--loan_purpose");
            command.add(application.getLoanPurpose());
            
            if (application.getAadharUrl() != null) {
                command.add("--identity_proof_url");
                command.add(application.getAadharUrl());
            }
            
            if (application.getPanUrl() != null) {
                command.add("--address_proof_url");
                command.add(application.getPanUrl());
            }
            
            if (application.getItrUrl() != null) {
                command.add("--income_proof_url");
                command.add(application.getItrUrl());
            }
            
            // Execute the command
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            
            logger.info("Executing command: {}", String.join(" ", command));
            Process process = processBuilder.start();
            
            // Read the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Wait for the process to complete with timeout
            boolean completed = process.waitFor(scriptTimeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                logger.error("Python script execution timed out after {} seconds", scriptTimeoutSeconds);
                throw new RuntimeException("Python script execution timed out");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.error("Python script execution failed with exit code: {}. Output: {}", exitCode, output.toString());
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            }
            
            logger.info("Python script executed successfully. Output: {}", output.toString());
            
            // Refresh application data from database to get updated values from Python script
            loanApplicationRepository.findById(applicationId).ifPresent(updatedApplication -> {
                logger.info("Updated loan application status: {}, approval chance: {}", 
                            updatedApplication.getStatus());
            });
            
            return true;
        } catch (IOException e) {
            logger.error("IO error executing Python script", e);
            throw new RuntimeException("IO error executing Python script: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while executing Python script", e);
            throw new RuntimeException("Interrupted while executing Python script: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error executing Python script", e);
            throw new RuntimeException("Error executing Python script: " + e.getMessage(), e);
        }
    }
}