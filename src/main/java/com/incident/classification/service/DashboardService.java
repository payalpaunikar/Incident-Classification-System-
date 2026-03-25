package com.incident.classification.service;



import com.incident.classification.dto.DashboardResponse;
import com.incident.classification.entity.Topic;
import com.incident.classification.repository.ClassifiedTextRepository;
import com.incident.classification.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DocumentRepository documentRepository;
    private final ClassifiedTextRepository classifiedTextRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalDocuments    = documentRepository.count();
        long totalChunks       = classifiedTextRepository.count();
        long classifiedChunks  = classifiedTextRepository.countByAssignedTopicIsNotNull();
        long unclassifiedChunks = classifiedTextRepository.countByAssignedTopicIsNull();

        List<Object[]> topicCounts = classifiedTextRepository.countByTopic();
        Map<String, Long> topicDistribution = new LinkedHashMap<>();
        for (Object[] row : topicCounts) {
            topicDistribution.put(((Topic) row[0]).getTitle(), (Long) row[1]);
        }
        if (unclassifiedChunks > 0) {
            topicDistribution.put(ClassificationEngine.UNCLASSIFIED, unclassifiedChunks);
        }

        return DashboardResponse.builder()
                .totalDocuments(totalDocuments)
                .totalChunks(totalChunks)
                .classifiedChunks(classifiedChunks)
                .unclassifiedChunks(unclassifiedChunks)
                .topicDistribution(topicDistribution)
                .build();
    }
}
