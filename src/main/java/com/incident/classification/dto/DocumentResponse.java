package com.incident.classification.dto;

import com.incident.classification.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String fileName;
    private Document.SourceType sourceType;
    private Document.ProcessingStatus status;
    private LocalDateTime createdAt;
    private Integer totalChunks;

}
