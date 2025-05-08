package com.loanwise.backend.controller.application;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.loanwise.backend.models.application.LoanApplication;
import com.loanwise.backend.service.LoanApplicationService;

@RestController
@RequestMapping("/api/application")
public class LoanApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationController.class);

    @Autowired
    private LoanApplicationService loanApplicationService;

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitLoanApplication(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("loanAmount") String loanAmount,
            @RequestParam("loanPurpose") String loanPurpose,
            @RequestParam("employmentStatus") String employmentStatus,
            @RequestParam("annualIncome") String annualIncome,
            @RequestParam("address") String address,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("zipCode") String zipCode,
            @RequestParam("identityProof") MultipartFile identityProof,
            @RequestParam("addressProof") MultipartFile addressProof,
            @RequestParam("incomeProof") MultipartFile incomeProof) {
        
        try {
            logger.info("Received loan application submission for: {} {}", firstName, lastName);
            
            // Validate inputs
            if (firstName == null || lastName == null || email == null) {
                return createErrorResponse("Required fields are missing", HttpStatus.BAD_REQUEST);
            }
            
            if (identityProof == null || addressProof == null || incomeProof == null) {
                return createErrorResponse("Required documents are missing", HttpStatus.BAD_REQUEST);
            }
            
            // Create loan application object
            LoanApplication application = new LoanApplication();
            application.setApplicantName(lastName);
            application.setApplicantEmail(email);
            application.setApplicantPhone(phoneNumber);
            application.setLoanAmount(Double.parseDouble(loanAmount));
            application.setLoanPurpose(loanPurpose);
            application.setEmploymentStatus(employmentStatus);
            application.setAnnualIncome(Double.parseDouble(annualIncome));
            application.setAddress(address);
            application.setCity(city);
            application.setState(state);
            application.setZipCode(zipCode);
            
            // Process loan application and documents
            Map<String, MultipartFile> documents = new HashMap<>();
            documents.put("identityProof", identityProof);
            documents.put("addressProof", addressProof);
            documents.put("incomeProof", incomeProof);
            
            LoanApplication response = loanApplicationService.processLoanApplication(application, documents);
            
            logger.info("Successfully processed loan application with ID: {}", response.getId());
            return ResponseEntity.ok(response);
            
        } catch (NumberFormatException e) {
            logger.error("Invalid number format in loan application", e);
            return createErrorResponse("Invalid number format for amount or income: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error processing loan application", e);
            return createErrorResponse("Failed to process loan application: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getLoanApplication(@PathVariable String id) {
        try {
            LoanApplication response = loanApplicationService.getLoanApplicationById(id);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return createErrorResponse("Loan application not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error retrieving loan application with ID: {}", id, e);
            return createErrorResponse("Failed to retrieve loan application: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private ResponseEntity<?> createErrorResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("status", status.toString());
        return new ResponseEntity<>(response, status);
    }
}