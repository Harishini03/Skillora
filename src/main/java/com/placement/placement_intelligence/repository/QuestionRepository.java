package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTopicIgnoreCase(String topic);

    List<Question> findByTopicIgnoreCaseAndCompanyId(String topic, Long companyId);
}
