package com.incident.classification.controller;


import com.incident.classification.dto.*;
import com.incident.classification.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Upload documents and retrieve classification results")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a PDF for classification")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadPdf(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document classified successfully", documentService.uploadPdf(file)));
    }

    @PostMapping("/text")
    @Operation(summary = "Submit raw text for classification")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadText(@RequestBody TextClassificationRequest classificationRequest) {
        String text = classificationRequest.getText();
        if (text == null || text.isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("'text' field is required."));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Text classified successfully",
                        documentService.uploadText(text, classificationRequest.getFileName())));
    }

    @GetMapping
    @Operation(summary = "List all documents")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAllDocuments() {
        return ResponseEntity.ok(ApiResponse.success(documentService.getAllDocuments()));
    }

    @GetMapping("/{id}/results")
    @Operation(summary = "Get full classification results for a document")
    public ResponseEntity<ApiResponse<DocumentClassificationResponse>> getResults(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getResults(id)));
    }

    @GetMapping("/{id}/results/paged")
    @Operation(summary = "Get paginated classification results")
    public ResponseEntity<ApiResponse<Page<ClassificationResult>>> getResultsPaged(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (size > 100) size = 100;
        return ResponseEntity.ok(ApiResponse.success(
                documentService.getResultsPaginated(id, PageRequest.of(page, size))));
    }
}
