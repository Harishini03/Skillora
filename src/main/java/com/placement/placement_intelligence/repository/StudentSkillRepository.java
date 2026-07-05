package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.StudentSkill;
import com.placement.placement_intelligence.model.StudentSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, StudentSkillId> {
    List<StudentSkill> findByStudent_Id(Long studentId);
}
