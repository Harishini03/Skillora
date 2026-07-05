package com.placement.placement_intelligence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StudentSkillId implements Serializable {

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "skill_id")
    private Long skillId;

    public StudentSkillId() {
    }

    public StudentSkillId(Long studentId, Long skillId) {
        this.studentId = studentId;
        this.skillId = skillId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StudentSkillId that = (StudentSkillId) o;
        return Objects.equals(studentId, that.studentId) && Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, skillId);
    }
}
