package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    @Query("select a.question.difficultyLevel as difficulty, "
            + "sum(case when a.correct = true then 1 else 0 end) as correctCount, "
            + "count(a) as totalCount "
            + "from StudentAnswer a group by a.question.difficultyLevel")
    List<Object[]> aggregateDifficultyPerformance();

    @Query("select a.question.topic as topic, "
            + "sum(case when a.correct = true then 1 else 0 end) as correctCount, "
            + "count(a) as totalCount "
            + "from StudentAnswer a group by a.question.topic")
    List<Object[]> aggregateTopicPerformance();

    @Query("select a.question.topic as topic, "
            + "sum(case when a.correct = true then 1 else 0 end) as correctCount, "
            + "count(a) as totalCount "
            + "from StudentAnswer a where a.attempt.student.id = :studentId group by a.question.topic")
    List<Object[]> aggregateTopicPerformanceByStudent(@Param("studentId") Long studentId);
}
