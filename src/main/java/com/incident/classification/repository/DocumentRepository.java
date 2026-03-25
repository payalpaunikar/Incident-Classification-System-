package com.incident.classification.repository;

import com.incident.classification.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
    long countByStatus(Document.ProcessingStatus status);

    @Query("SELECT COUNT(d) FROM Document d")
    long countAllDocuments();
}
