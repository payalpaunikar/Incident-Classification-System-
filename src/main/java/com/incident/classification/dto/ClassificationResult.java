package com.incident.classification.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResult {
    private Long id;
    private String text;
    private String assignedTopic;
    private Double confidence;
    private Integer chunkIndex;
}
