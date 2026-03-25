package com.incident.classification.repository;

import com.incident.classification.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TopicRepository extends JpaRepository<Topic,Long> {

    boolean existsByTitleIgnoreCase(String title);

    List<Topic> findAllByOrderByCreatedAtDesc();

    Optional<Topic> findByTitleIgnoreCase(String title);

}
