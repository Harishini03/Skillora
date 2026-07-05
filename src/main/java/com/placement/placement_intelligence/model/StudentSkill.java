package com.placement.placement_intelligence.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_skills")
public class StudentSkill {

    @EmbeddedId
    private StudentSkillId id;

    @ManyToOne(optional = false)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(optional = false)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "skill_score")
    private Double skillScore;

    public StudentSkill() {
    }

    public StudentSkillId getId() {
        return id;
    }

    public void setId(StudentSkillId id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Double getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(Double skillScore) {
        this.skillScore = skillScore;
    }
}
