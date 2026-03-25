package com.incident.classification.service;



import com.incident.classification.dto.ClassificationResult;
import com.incident.classification.dto.DocumentClassificationResponse;
import com.incident.classification.dto.DocumentResponse;
import com.incident.classification.entity.ClassifiedText;
import com.incident.classification.entity.Document;
import com.incident.classification.entity.Topic;
import com.incident.classification.exception.DocumentProcessingException;
import com.incident.classification.exception.ResourceNotFoundException;
import com.incident.classification.repository.ClassifiedTextRepository;
import com.incident.classification.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ClassifiedTextRepository classifiedTextRepository;
    private final PdfExtractorService pdfExtractorService;
    private final TextChunker textChunker;
    private final ClassificationEngine classificationEngine;
    private final TopicService topicService;

    @Transactional
    public DocumentResponse uploadPdf(MultipartFile file) {
        String extractedText = pdfExtractorService.extractText(file);
        Document document = documentRepository.save(Document.builder()
                .fileName(file.getOriginalFilename())
                .sourceType(Document.SourceType.PDF)
                .originalText(extractedText)
                .status(Document.ProcessingStatus.PROCESSING)
                .build());
        return processAndClassify(document);
    }

    @Transactional
    public DocumentResponse uploadText(String rawText, String fileName) {
        if (rawText == null || rawText.isBlank())
            throw new DocumentProcessingException("Text content must not be empty.");
        String name = (fileName != null && !fileName.isBlank()) ? fileName : "text-input.txt";
        Document document = documentRepository.save(Document.builder()
                .fileName(name)
                .sourceType(Document.SourceType.TEXT)
                .originalText(rawText)
                .status(Document.ProcessingStatus.PROCESSING)
                .build());
        return processAndClassify(document);
    }

    private DocumentResponse processAndClassify(Document document) {
        try {
            List<Topic> topics = topicService.getAllTopicEntities();
            if (topics.isEmpty()) log.warn("No topics found — all chunks will be UNCLASSIFIED.");

            List<String> chunks = textChunker.chunk(document.getOriginalText());
            log.info("Document '{}' split into {} chunks", document.getFileName(), chunks.size());

            List<ClassifiedText> results = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                ClassificationEngine.ClassificationResult result =
                        classificationEngine.classify(chunks.get(i), topics);
                results.add(ClassifiedText.builder()
                        .document(document)
                        .textChunk(chunks.get(i))
                        .assignedTopic(result.getTopic())
                        .confidenceScore(result.getConfidence())
                        .chunkIndex(i)
                        .build());
            }

            classifiedTextRepository.saveAll(results);
            document.setStatus(Document.ProcessingStatus.COMPLETED);
            document = documentRepository.save(document);
            log.info("Document id={} processed: {} chunks", document.getDocumentId(), results.size());
            return toResponse(document, results.size());

        } catch (Exception ex) {
            document.setStatus(Document.ProcessingStatus.FAILED);
            documentRepository.save(document);
            throw new DocumentProcessingException("Classification failed: " + ex.getMessage(), ex);
        }
    }

    @Transactional(readOnly = true)
    public DocumentClassificationResponse getResults(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));

        List<ClassificationResult> results = classifiedTextRepository
                .findByDocumentOrderByChunkIndex(document).stream()
                .map(ct -> ClassificationResult.builder()
                        .id(ct.getTextId())
                        .text(ct.getTextChunk())
                        .assignedTopic(ct.getAssignedTopic() != null
                                ? ct.getAssignedTopic().getTitle()
                                : ClassificationEngine.UNCLASSIFIED)
                        .confidence(ct.getConfidenceScore())
                        .chunkIndex(ct.getChunkIndex())
                        .build())
                .collect(Collectors.toList());

        return DocumentClassificationResponse.builder()
                .documentId(document.getDocumentId())
                .fileName(document.getFileName())
                .totalChunks(results.size())
                .results(results)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ClassificationResult> getResultsPaginated(Long documentId, Pageable pageable) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        return classifiedTextRepository.findByDocument(document, pageable)
                .map(ct -> ClassificationResult.builder()
                        .id(ct.getTextId())
                        .text(ct.getTextChunk())
                        .assignedTopic(ct.getAssignedTopic() != null
                                ? ct.getAssignedTopic().getTitle()
                                : ClassificationEngine.UNCLASSIFIED)
                        .confidence(ct.getConfidenceScore())
                        .chunkIndex(ct.getChunkIndex())
                        .build());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(doc -> toResponse(doc, (int) classifiedTextRepository.countByDocument(doc)))
                .collect(Collectors.toList());
    }

    private DocumentResponse toResponse(Document document, int totalChunks) {
        return DocumentResponse.builder()
                .id(document.getDocumentId())
                .fileName(document.getFileName())
                .sourceType(document.getSourceType())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .totalChunks(totalChunks)
                .build();
    }
}