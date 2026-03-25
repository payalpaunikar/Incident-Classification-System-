package com.incident.classification.dto;


import lombok.*;
import lombok.Data;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentClassificationResponse {
    private Long documentId;
    private String fileName;
    private Integer totalChunks;
    private List<ClassificationResult> results;

}
