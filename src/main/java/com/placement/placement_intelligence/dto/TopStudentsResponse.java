package com.placement.placement_intelligence.dto;

import java.util.List;
import java.util.Map;

public class TopStudentsResponse {
    private Map<String, List<TopStudent>> topByDepartment;

    public Map<String, List<TopStudent>> getTopByDepartment() {
        return topByDepartment;
    }

    public void setTopByDepartment(Map<String, List<TopStudent>> topByDepartment) {
        this.topByDepartment = topByDepartment;
    }

    public static class TopStudent {
        private Long studentId;
        private String name;
        private Double readiness;
        private Double score;

        public TopStudent() {
        }

        public TopStudent(Long studentId, String name, Double readiness, Double score) {
            this.studentId = studentId;
            this.name = name;
            this.readiness = readiness;
            this.score = score;
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

        public Double getReadiness() {
            return readiness;
        }

        public void setReadiness(Double readiness) {
            this.readiness = readiness;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }
}
