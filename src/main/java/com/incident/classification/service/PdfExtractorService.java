package com.incident.classification.service;



import com.incident.classification.exception.DocumentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class PdfExtractorService {

    public String extractText(MultipartFile file) {
        validatePdf(file);
        try {
            byte[] bytes = file.getBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                if (document.isEncrypted()) {
                    throw new DocumentProcessingException("PDF is encrypted. Please provide an unencrypted file.");
                }
                if (document.getNumberOfPages() == 0) {
                    throw new DocumentProcessingException("PDF has no pages.");
                }

                log.info("Extracting text from PDF: {} ({} pages)", file.getOriginalFilename(), document.getNumberOfPages());

                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                String text = stripper.getText(document);

                if (text == null || text.isBlank()) {
                    throw new DocumentProcessingException("No text could be extracted. PDF may be image-only.");
                }

                log.info("Extracted {} characters from PDF", text.length());
                return text;
            }
        } catch (DocumentProcessingException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new DocumentProcessingException("Failed to read PDF: " + ex.getMessage(), ex);
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentProcessingException("No file provided or file is empty.");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".pdf")) {
            throw new DocumentProcessingException("Only PDF files are accepted. Received: " + name);
        }
    }
}