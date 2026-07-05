package com.placement.placement_intelligence.dto;

import java.util.List;

public class ProgressResponse {
    private List<ProgressPoint> timeline;
    private SectionPerformance sectionPerformance;
    private List<String> suggestions;

    public List<ProgressPoint> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<ProgressPoint> timeline) {
        this.timeline = timeline;
    }

    public SectionPerformance getSectionPerformance() {
        return sectionPerformance;
    }

    public void setSectionPerformance(SectionPerformance sectionPerformance) {
        this.sectionPerformance = sectionPerformance;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public static class ProgressPoint {
        private String date;
        private Double readiness;

        public ProgressPoint() {
        }

        public ProgressPoint(String date, Double readiness) {
            this.date = date;
            this.readiness = readiness;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Double getReadiness() {
            return readiness;
        }

        public void setReadiness(Double readiness) {
            this.readiness = readiness;
        }
    }

    public static class SectionPerformance {
        private Double aptitude;
        private Double coding;
        private Double softSkills;

        public SectionPerformance() {
        }

        public SectionPerformance(Double aptitude, Double coding, Double softSkills) {
            this.aptitude = aptitude;
            this.coding = coding;
            this.softSkills = softSkills;
        }

        public Double getAptitude() {
            return aptitude;
        }

        public void setAptitude(Double aptitude) {
            this.aptitude = aptitude;
        }

        public Double getCoding() {
            return coding;
        }

        public void setCoding(Double coding) {
            this.coding = coding;
        }

        public Double getSoftSkills() {
            return softSkills;
        }

        public void setSoftSkills(Double softSkills) {
            this.softSkills = softSkills;
        }
    }
}
