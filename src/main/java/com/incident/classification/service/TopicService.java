package com.incident.classification.service;


import com.incident.classification.dto.TopicRequest;
import com.incident.classification.dto.TopicResponse;
import com.incident.classification.entity.Topic;
import com.incident.classification.exception.DuplicateResourceException;
import com.incident.classification.exception.ResourceNotFoundException;
import com.incident.classification.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

    private final TopicRepository topicRepository;


     @Transactional
     public TopicResponse createTopic(TopicRequest topicRequest){
       validateTopicRequest(topicRequest);

       if (topicRepository.existsByTitleIgnoreCase(topicRequest.getTitle())){
           throw new DuplicateResourceException("A topic with title : "+topicRequest.getTitle()+" already exits.");
       }

       Topic topic = topicRequestConvertToTopic(new Topic(),topicRequest);

       Topic savedTopic = topicRepository.save(topic);
       log.info("Created Topic : "+topic);

       return topicToTopicResponse(savedTopic);
     }


     @Transactional
     public List<TopicResponse> getAllTopic(){
         return topicRepository.findAllByOrderByCreatedAtDesc()
                 .stream()
                 .map(this::topicToTopicResponse)
                 .collect(Collectors.toList());
     }



     public TopicResponse getTopicById(Long topicId){
         return topicRepository.findById(topicId)
                 .map(this::topicToTopicResponse)
                 .orElseThrow(()-> new ResourceNotFoundException("Topic not found with id : "+topicId));
     }


     @Transactional
     public TopicResponse updateTopic(Long topicId,TopicRequest topicRequest){
      Topic topic = findTopicById(topicId);

         //check if the tile exists in our database
         topicRepository.findByTitleIgnoreCase(topicRequest.getTitle())
                 .filter(existing -> !existing.getTopicId().equals(topicId))
                 .ifPresent(existing -> {
                     throw new DuplicateResourceException(
                             "Another topic with title '" + topicRequest.getTitle() + "' already exists.");
                 });

         topicRequestConvertToTopic(topic,topicRequest);

         Topic saveTopic = topicRepository.save(topic);

         return topicToTopicResponse(saveTopic);
     }


     public void deleteTopic(Long topicId){
     Topic topic =  findTopicById(topicId);

       topicRepository.delete(topic);
       log.info("Topic delete with id : "+topicId);

     }


     public Topic findTopicById(Long id){
         return  topicRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id : "+id));

     }

    @Transactional(readOnly = true)
    public List<Topic> getAllTopicEntities() {
        return topicRepository.findAll();
    }



    private void validateTopicRequest(TopicRequest topicRequest){
         if (topicRequest.getTitle()==null || topicRequest.getTitle().isBlank()){
             throw new IllegalArgumentException("Topic title must not be blank.");
         }
         if (topicRequest.getMandatoryKeywords()==null || topicRequest.getMandatoryKeywords().isEmpty()){
             throw new IllegalArgumentException("At least one keyword is required in mandatory field.");
         }
     }


     protected Topic topicRequestConvertToTopic(Topic topic,TopicRequest request){
         topic.setTitle(request.getTitle().trim());
         topic.setMandatoryKeywords(
                 request.getMandatoryKeywords().stream()
                         .map(String::trim)
                         .filter(k -> !k.isBlank())
                         .distinct()
                         .collect(Collectors.toList())
         );

         topic.setOptionalKeywords(
                 request.getOptionalKeywords().stream()
                         .map(String::trim)
                         .filter(k -> !k.isBlank())
                         .distinct()
                         .collect(Collectors.toList())
         );

         return topic;
     }

     protected TopicResponse topicToTopicResponse(Topic topic){
         return TopicResponse.builder()
                 .id(topic.getTopicId())
                 .title(topic.getTitle())
                 .mandatoryKeywords(topic.getMandatoryKeywords())
                 .optionalKeywords(topic.getOptionalKeywords())
                 .createdAt(topic.getCreatedAt())
                 .build();

     }

}
