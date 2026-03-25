package com.incident.classification.repository;

import com.incident.classification.entity.ClassifiedText;
import com.incident.classification.entity.Document;
import com.incident.classification.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassifiedTextRepository extends JpaRepository<ClassifiedText,Long> {
    List<ClassifiedText> findByDocumentOrderByChunkIndex(Document document);

    Page<ClassifiedText> findByDocument(Document document, Pageable pageable);

    long countByDocument(Document document);

    long countByAssignedTopicIsNull();

    long countByAssignedTopicIsNotNull();

    @Query("SELECT ct.assignedTopic, COUNT(ct) FROM ClassifiedText ct " +
            "WHERE ct.assignedTopic IS NOT NULL GROUP BY ct.assignedTopic")
    List<Object[]> countByTopic();

    @Query("SELECT COUNT(ct) FROM ClassifiedText ct WHERE ct.assignedTopic = :topic")
    long countByAssignedTopic(Topic topic);

}
