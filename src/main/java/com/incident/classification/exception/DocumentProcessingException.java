package com.incident.classification.exception;

public class DocumentProcessingException extends RuntimeException{

    public DocumentProcessingException(String message) {
        super(message);
    }

    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
