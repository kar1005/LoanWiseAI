package com.loanwise.backend.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanwise.backend.models.application.LoanApplication;
import com.loanwise.backend.models.document.Documents;

@Service
public class PythonScriptService {

    private final String PYTHON_PATH = "python";
    private final String SCRIPT_PATH = "src/main/java/com/loanwise/backend/scripts/validate_documents.py";

    public boolean validateLoanApplication(LoanApplication application, List<Documents> documents) throws IOException, InterruptedException {
        // Create a temporary JSON file with application and document details
        ObjectMapper mapper = new ObjectMapper();
        
        // Prepare data to be sent to Python script
        Map<String, Object> data = new HashMap<>();
        data.put("application", application);
        
        List<Map<String, String>> documentsList = new ArrayList<>();
        for (Documents doc : documents) {
            Map<String, String> docMap = new HashMap<>();
            docMap.put("type", doc.getDocumentType());
            docMap.put("path", doc.getCloudinaryUrl());
            documentsList.add(docMap);
        }
        data.put("documents", documentsList);
        
        // Create a temporary file for the JSON data
        File tempFile = File.createTempFile("loan_data_", ".json");
        mapper.writeValue(tempFile, data);
        
        // Build command to run the Python script
        ProcessBuilder processBuilder = new ProcessBuilder(
            PYTHON_PATH, 
            SCRIPT_PATH, 
            "--input", tempFile.getAbsolutePath()
        );
        
        processBuilder.redirectErrorStream(true);
        
        // Execute the Python script
        Process process = processBuilder.start();
        
        // Read the output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // Wait for the process to complete
        int exitCode = process.waitFor();
        
        // Clean up temp file
        tempFile.delete();
        
        if (exitCode != 0) {
            throw new IOException("Python script failed with exit code " + exitCode + ": " + output.toString());
        }
        
        // Parse the output to determine if the loan was approved
        String result = output.toString().trim();
        return result.contains("APPROVED");
    }
}