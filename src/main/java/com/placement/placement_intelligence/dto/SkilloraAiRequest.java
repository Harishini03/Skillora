package com.placement.placement_intelligence.dto;

import java.util.List;

public class SkilloraAiRequest {
    private String mode;
    private String topic;
    private String subtopic;
    private String difficulty;
    private Integer numberOfQuestions;
    private String studentLevel;
    private String company;
    private String examType;
    private Integer targetScore;
    private List<String> previousTopicsCovered;
    private List<String> previouslyGeneratedQuestions;
    private List<String> weakTopics;
    private Integer correct;
    private Integer wrong;
    private Double accuracy;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getSubtopic() { return subtopic; }
    public void setSubtopic(String subtopic) { this.subtopic = subtopic; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getNumberOfQuestions() { return numberOfQuestions; }
    public void setNumberOfQuestions(Integer numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }
    public String getStudentLevel() { return studentLevel; }
    public void setStudentLevel(String studentLevel) { this.studentLevel = studentLevel; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public Integer getTargetScore() { return targetScore; }
    public void setTargetScore(Integer targetScore) { this.targetScore = targetScore; }
    public List<String> getPreviousTopicsCovered() { return previousTopicsCovered; }
    public void setPreviousTopicsCovered(List<String> l) { this.previousTopicsCovered = l; }
    public List<String> getPreviouslyGeneratedQuestions() { return previouslyGeneratedQuestions; }
    public void setPreviouslyGeneratedQuestions(List<String> l) { this.previouslyGeneratedQuestions = l; }
    public List<String> getWeakTopics() { return weakTopics; }
    public void setWeakTopics(List<String> weakTopics) { this.weakTopics = weakTopics; }
    public Integer getCorrect() { return correct; }
    public void setCorrect(Integer correct) { this.correct = correct; }
    public Integer getWrong() { return wrong; }
    public void setWrong(Integer wrong) { this.wrong = wrong; }
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
}
