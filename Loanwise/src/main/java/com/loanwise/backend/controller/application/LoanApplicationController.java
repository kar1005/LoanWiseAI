package com.loanwise.backend.controller.application;

import com.loanwise.backend.models.application.LoanApplication;
import com.loanwise.backend.models.document.Documents;
import com.loanwise.backend.models.validation.ValidationLog;
import com.loanwise.backend.service.CloudinaryService;
import com.loanwise.backend.service.DocumentProcessingService;
import com.loanwise.backend.service.LoanApplicationService;
import com.loanwise.backend.service.PythonScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/application")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final PythonScriptService pythonScriptService;
    private final DocumentProcessingService documentProcessingService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/new")
    public String newApplication(Model model) {
        model.addAttribute("application", new LoanApplication());
        return "application/new";
    }

    @PostMapping("/submit")
    public String submitApplication(
            @ModelAttribute LoanApplication application,
            @RequestParam("files") MultipartFile[] files,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Save the application first to get the ID
            LoanApplication savedApplication = loanApplicationService.submitApplication(application);
            
            // Process each file and upload to Cloudinary
            List<Documents> documentsList = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Upload file to Cloudinary
                    String cloudinaryUrl = cloudinaryService.uploadFile(file);
                    
                    // Create document record
                    Documents document = new Documents();
                    document.setDocumentType(file.getContentType());
                    document.setCloudinaryUrl(cloudinaryUrl);
                    document.setApplicationId(savedApplication.getId());
                    
                    documentsList.add(document);
                }
            }
            
            // Save documents to MongoDB
            documentsList = documentProcessingService.saveDocuments(documentsList);
            
            // Associate documents with the application
            savedApplication.setDocuments(documentsList);
            loanApplicationService.updateApplication(savedApplication);
            
            // Process documents with Python script
            Map<String, Object> results = documentProcessingService.processDocuments(documentsList);
            
            // Create validation log
            ValidationLog validationLog = new ValidationLog();
            validationLog.setApplicationId(savedApplication.getId());
            validationLog.setValidationResults(results);
            documentProcessingService.saveValidationLog(validationLog);
            
            // Redirect to results page
            redirectAttributes.addFlashAttribute("applicationId", savedApplication.getId());
            return "redirect:/application/results";
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error uploading files: " + e.getMessage());
            return "redirect:/application/new";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing application: " + e.getMessage());
            return "redirect:/application/new";
        }
    }
    
    @GetMapping("/results")
    public String showResults(@ModelAttribute("applicationId") String applicationId, Model model) {
        LoanApplication application = loanApplicationService.getApplicationById(applicationId);
        ValidationLog validationLog = documentProcessingService.getValidationLogByApplicationId(applicationId);
        
        model.addAttribute("application", application);
        model.addAttribute("validationLog", validationLog);
        
        return "application/results";
    }
}