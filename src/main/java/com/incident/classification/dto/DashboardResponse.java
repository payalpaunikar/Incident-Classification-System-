package com.incident.classification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Long totalDocuments;
    private Long totalChunks;
    private Long classifiedChunks;
    private Long unclassifiedChunks;
    private Map<String, Long> topicDistribution;
}
