package com.incident.classification.controller;

import com.incident.classification.dto.ApiResponse;
import com.incident.classification.dto.TopicRequest;
import com.incident.classification.dto.TopicResponse;
import com.incident.classification.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/topics")
public class TopicController {

    private final TopicService topicService;

    @PostMapping
    @Operation(summary = "Create a new topic",
            description = "Accepts a single topic or a list of topics. " +
                    "Duplicate titles are skipped gracefully when using the batch endpoint.")
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(
            @Valid @RequestBody TopicRequest request) {

        TopicResponse response = topicService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Topic created successfully", response));
    }


    @GetMapping
    @Operation(summary = "Get all topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAllTopics() {
        return ResponseEntity.ok(ApiResponse.success(topicService.getAllTopic()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a topic by ID")
    public ResponseEntity<ApiResponse<TopicResponse>> getTopicById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(topicService.getTopicById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a topic by ID")
    public ResponseEntity<ApiResponse<TopicResponse>> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Topic updated successfully", topicService.updateTopic(id, request)));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a topic")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ResponseEntity.ok(ApiResponse.success("Topic deleted successfully", null));
    }


}
