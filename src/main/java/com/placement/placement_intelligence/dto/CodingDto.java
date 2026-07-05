package com.placement.placement_intelligence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CodingDto {

    @Data
    public static class CreateProblemRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        private String title;
        
        @NotBlank(message = "Description is required")
        private String description;
        
        @NotBlank(message = "Difficulty level is required")
        private String difficultyLevel; // EASY, MEDIUM, HARD
        
        private String topicTags; // Comma-separated
        private Integer timeLimitSeconds = 5;
        private Integer memoryLimitMb = 256;
    }

    @Data
    public static class CreateTestCaseRequest {
        @NotBlank(message = "Input data is required")
        private String inputData;
        
        @NotBlank(message = "Expected output is required")
        private String expectedOutput;
        
        private Boolean isSample = false;
        private Integer orderIndex;
    }

    @Data
    public static class ExecuteCodeRequest {
        @NotNull(message = "Problem ID is required")
        private Long problemId;
        
        @NotBlank(message = "Language is required")
        private String language; // JAVA, PYTHON, JAVASCRIPT
        
        @NotBlank(message = "Code is required")
        private String code;
    }

    @Data
    public static class ProblemResponse {
        private Long id;
        private String title;
        private String description;
        private String difficultyLevel;
        private String topicTags;
        private Integer timeLimitSeconds;
        private Integer memoryLimitMb;
        private String createdByName;
        private LocalDateTime createdAt;
        private Integer totalTestCases;
        private Integer sampleTestCases;
        private Integer totalSubmissions;
        private Integer acceptedSubmissions;
    }

    @Data
    public static class TestCaseResponse {
        private Long id;
        private String inputData;
        private String expectedOutput;
        private Boolean isSample;
        private Integer orderIndex;
    }

    @Data
    public static class SubmissionResponse {
        private Long id;
        private Long problemId;
        private String problemTitle;
        private Long studentId;
        private String studentName;
        private String language;
        private String codeContent;
        private String status;
        private Integer executionTimeMs;
        private BigDecimal memoryUsedMb;
        private Integer testCasesPassed;
        private Integer testCasesTotal;
        private LocalDateTime submittedAt;
        private List<TestResultResponse> testResults;
    }

    @Data
    public static class TestResultResponse {
        private Long id;
        private Long testCaseId;
        private Boolean passed;
        private String actualOutput;
        private String errorMessage;
        private Integer executionTimeMs;
        private String inputData;
        private String expectedOutput;
        private Boolean isSample;
    }

    @Data
    public static class ExecutionResultResponse {
        private String status;
        private String output;
        private String error;
        private Integer executionTimeMs;
        private Integer testCasesPassed;
        private Integer testCasesTotal;
        private List<TestResultResponse> testResults;
    }

    @Data
    public static class ProblemListResponse {
        private Long id;
        private String title;
        private String difficultyLevel;
        private String topicTags;
        private Integer totalSubmissions;
        private Integer acceptedSubmissions;
        private Double acceptanceRate;
        private Boolean solved; // Has the current student solved it?
    }
}
