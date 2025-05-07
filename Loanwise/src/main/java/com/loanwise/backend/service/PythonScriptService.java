package com.loanwise.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PythonScriptService {

    @Value("${python.interpreter.path}")
    private String pythonInterpreterPath;

    @Value("${python.script.path}")
    private String pythonScriptPath;

    private final ObjectMapper objectMapper;

    public Map<String, Object> runDocumentProcessorScript(List<String> documentUrls) {
        try {
            List<String> command = new ArrayList<>();
            command.add(pythonInterpreterPath);
            command.add(pythonScriptPath);
            
            // Add document URLs as arguments
            for (String url : documentUrls) {
                command.add(url);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            // Read output from Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for Python script to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            }

            // Parse JSON output from Python script
            return objectMapper.readValue(output.toString(), HashMap.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error executing Python script: " + e.getMessage(), e);
        }
    }
}