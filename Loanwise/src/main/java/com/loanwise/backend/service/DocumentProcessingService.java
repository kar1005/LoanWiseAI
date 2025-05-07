package com.loanwise.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.loanwise.backend.models.document.Documents;
import com.loanwise.backend.repository.interfaces.document.IDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {

    private final IDocumentRepository documentRepository;
    private final Cloudinary cloudinary;

    /**
     * Upload multiple files to Cloudinary
     * @param files List of files to upload
     * @return Map of original filenames to Cloudinary URLs
     */
    public Map<String, String> uploadToCloudinary(List<MultipartFile> files) throws IOException {
        Map<String, String> urlMap = new HashMap<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                log.info("Uploading file {} to Cloudinary", file.getOriginalFilename());
                Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                        "folder", "loanwise",
                        "resource_type", "auto"
                    )
                );
                
                String publicId = (String) uploadResult.get("public_id");
                String url = (String) uploadResult.get("secure_url");
                urlMap.put(file.getOriginalFilename(), url + "||" + publicId);
                log.info("File {} uploaded successfully", file.getOriginalFilename());
            }
        }
        
        return urlMap;
    }
    
    /**
     * Process uploaded documents and save their metadata
     * @param applicationId ID of the loan application
     * @param cloudinaryUrls Map of filenames to Cloudinary URLs with public IDs
     * @param documentTypes List of document types
     */
    public void processAndSaveDocuments(String applicationId, Map<String, String> cloudinaryUrls, List<String> documentTypes) {
        List<Documents> documents = new ArrayList<>();
        
        int i = 0;
        for (Map.Entry<String, String> entry : cloudinaryUrls.entrySet()) {
            String[] parts = entry.getValue().split("\\|\\|");
            String url = parts[0];
            String publicId = parts[1];
            
            String documentType = (i < documentTypes.size()) ? documentTypes.get(i) : "UNKNOWN";
            
            Documents document = new Documents(applicationId, documentType, url, publicId);
            documents.add(document);
            i++;
        }
        
        documentRepository.saveAll(documents);
        log.info("Saved {} documents for application ID: {}", documents.size(), applicationId);
    }
    
    /**
     * Delete a document from Cloudinary and database
     * @param documentId ID of the document to delete
     */
    public void deleteDocument(String documentId) throws IOException {
        Documents document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));
        
        // Delete from Cloudinary
        log.info("Deleting document with public ID {} from Cloudinary", document.getCloudinaryPublicId());
        cloudinary.uploader().destroy(
            document.getCloudinaryPublicId(),
            ObjectUtils.emptyMap()
        );
        
        // Delete from database
        documentRepository.delete(document);
        log.info("Document with ID {} deleted successfully", documentId);
    }
    
    /**
     * Get all documents for a loan application
     * @param applicationId ID of the loan application
     * @return List of documents
     */
    public List<Documents> getDocumentsByApplicationId(String applicationId) {
        List<Documents> documents = documentRepository.findByApplicationId(applicationId);
        log.info("Found {} documents for application ID: {}", documents.size(), applicationId);
        return documents;
    }
}