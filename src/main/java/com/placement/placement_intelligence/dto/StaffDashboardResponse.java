package com.placement.placement_intelligence.dto;

import java.util.List;
import java.util.Map;

public class StaffDashboardResponse {
    private long totalStudents;
    private long eligibleStudents;
    private double placementRate;
    private double averageCgpa;
    private List<DepartmentSummary> departmentSummaries;
    private List<SkillGapSummary> skillGaps;
    private List<StudentSummary> topStudents;

    public StaffDashboardResponse() {
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getEligibleStudents() {
        return eligibleStudents;
    }

    public void setEligibleStudents(long eligibleStudents) {
        this.eligibleStudents = eligibleStudents;
    }

    public double getPlacementRate() {
        return placementRate;
    }

    public void setPlacementRate(double placementRate) {
        this.placementRate = placementRate;
    }

    public double getAverageCgpa() {
        return averageCgpa;
    }

    public void setAverageCgpa(double averageCgpa) {
        this.averageCgpa = averageCgpa;
    }

    public List<DepartmentSummary> getDepartmentSummaries() {
        return departmentSummaries;
    }

    public void setDepartmentSummaries(List<DepartmentSummary> departmentSummaries) {
        this.departmentSummaries = departmentSummaries;
    }

    public List<SkillGapSummary> getSkillGaps() {
        return skillGaps;
    }

    public void setSkillGaps(List<SkillGapSummary> skillGaps) {
        this.skillGaps = skillGaps;
    }

    public List<StudentSummary> getTopStudents() {
        return topStudents;
    }

    public void setTopStudents(List<StudentSummary> topStudents) {
        this.topStudents = topStudents;
    }

    public static class DepartmentSummary {
        private String department;
        private double eligibilityPercentage;
        private double placementPercentage;
        private double averageFinalScore;

        public DepartmentSummary() {
        }

        public DepartmentSummary(String department, double eligibilityPercentage, double placementPercentage, double averageFinalScore) {
            this.department = department;
            this.eligibilityPercentage = eligibilityPercentage;
            this.placementPercentage = placementPercentage;
            this.averageFinalScore = averageFinalScore;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public double getEligibilityPercentage() {
            return eligibilityPercentage;
        }

        public void setEligibilityPercentage(double eligibilityPercentage) {
            this.eligibilityPercentage = eligibilityPercentage;
        }

        public double getPlacementPercentage() {
            return placementPercentage;
        }

        public void setPlacementPercentage(double placementPercentage) {
            this.placementPercentage = placementPercentage;
        }

        public double getAverageFinalScore() {
            return averageFinalScore;
        }

        public void setAverageFinalScore(double averageFinalScore) {
            this.averageFinalScore = averageFinalScore;
        }
    }

    public static class SkillGapSummary {
        private String skill;
        private long studentCount;

        public SkillGapSummary() {
        }

        public SkillGapSummary(String skill, long studentCount) {
            this.skill = skill;
            this.studentCount = studentCount;
        }

        public String getSkill() {
            return skill;
        }

        public void setSkill(String skill) {
            this.skill = skill;
        }

        public long getStudentCount() {
            return studentCount;
        }

        public void setStudentCount(long studentCount) {
            this.studentCount = studentCount;
        }
    }

    public static class StudentSummary {
        private Long studentId;
        private String name;
        private String department;
        private Double finalScore;
        private Integer rank;

        public StudentSummary() {
        }

        public StudentSummary(Long studentId, String name, String department, Double finalScore, Integer rank) {
            this.studentId = studentId;
            this.name = name;
            this.department = department;
            this.finalScore = finalScore;
            this.rank = rank;
        }

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public Double getFinalScore() {
            return finalScore;
        }

        public void setFinalScore(Double finalScore) {
            this.finalScore = finalScore;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }
    }
}
