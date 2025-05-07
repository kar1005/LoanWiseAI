package com.loanwise.backend.controller.application;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.loanwise.backend.models.application.LoanApplication;
import com.loanwise.backend.service.DocumentProcessingService;
import com.loanwise.backend.service.LoanApplicationService;

@RestController
@RequestMapping("/api/loans")
public class LoanApplicationController {

    @Autowired
    private LoanApplicationService loanApplicationService;

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @PostMapping("/apply")
    public ResponseEntity<?> submitLoanApplication(@RequestBody LoanApplication application) {
        try {
            LoanApplication submitted = loanApplicationService.submitApplication(application);
            return ResponseEntity.ok(submitted);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting application: " + e.getMessage());
        }
    }

    @PostMapping("/{applicationId}/documents")
    public ResponseEntity<?> uploadDocuments(
            @PathVariable String applicationId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("documentTypes") List<String> documentTypes) {
        try {
            Map<String, String> cloudinaryUrls = documentProcessingService.uploadToCloudinary(files);
            documentProcessingService.processAndSaveDocuments(applicationId, cloudinaryUrls, documentTypes);
            return ResponseEntity.ok(cloudinaryUrls);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading documents to Cloudinary: " + e.getMessage());
        }
    }

    @PostMapping("/{applicationId}/verify")
    public ResponseEntity<?> verifyApplication(@PathVariable String applicationId) {
        try {
            boolean verified = loanApplicationService.verifyApplication(applicationId);
            if (verified) {
                return ResponseEntity.ok("Application verified successfully");
            } else {
                return ResponseEntity.badRequest().body("Verification failed");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error verifying application: " + e.getMessage());
        }
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<?> getApplication(@PathVariable String applicationId) {
        try {
            LoanApplication application = loanApplicationService.getApplicationById(applicationId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving application: " + e.getMessage());
        }
    }

    @GetMapping("/{applicationId}/status")
    public ResponseEntity<?> getApplicationStatus(@PathVariable String applicationId) {
        try {
            String status = loanApplicationService.getApplicationStatus(applicationId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving status: " + e.getMessage());
        }
    }
}