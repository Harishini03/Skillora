package com.placement.placement_intelligence.dto;

public class SkilloraAiResponse {
    private String mode;
    private String topic;
    private String content;
    private boolean aiGenerated;

    public SkilloraAiResponse() {
    }

    public SkilloraAiResponse(String mode, String topic, String content, boolean aiGenerated) {
        this.mode = mode;
        this.topic = topic;
        this.content = content;
        this.aiGenerated = aiGenerated;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }
}
