package com.incident.classification.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {
    private Long id;
    private String title;
    private List<String> mandatoryKeywords;
    private List<String> optionalKeywords;
    private LocalDateTime createdAt;
}
