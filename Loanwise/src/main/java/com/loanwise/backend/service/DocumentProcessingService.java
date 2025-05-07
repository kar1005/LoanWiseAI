package com.loanwise.backend.service;

import com.loanwise.backend.models.document.Documents;
import com.loanwise.backend.models.validation.ValidationLog;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.loanwise.backend.repository.interfaces.document.IDocumentRepository;
import com.loanwise.backend.repository.interfaces.validation.IValidationLogRepository;

@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final IDocumentRepository documentsRepository;
    private final IValidationLogRepository validationLogRepository;
    private final PythonScriptService pythonScriptService;

    public List<Documents> saveDocuments(List<Documents> documents) {
        return documentsRepository.saveAll(documents);
    }

    public Map<String, Object> processDocuments(List<Documents> documents) {
        // Prepare document URLs for Python script
        List<String> documentUrls = documents.stream()
                .map(Documents::getCloudinaryUrl)
                .toList();

        // Run Python script with document URLs
        return pythonScriptService.runDocumentProcessorScript(documentUrls);
    }

    public ValidationLog saveValidationLog(ValidationLog validationLog) {
        return validationLogRepository.save(validationLog);
    }

    public ValidationLog getValidationLogByApplicationId(String applicationId) {
        return validationLogRepository.findByApplicationId(applicationId);
    }
}