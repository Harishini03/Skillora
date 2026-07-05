package com.placement.placement_intelligence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CourseDto {

    @Data
    public static class CreateCourseRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        private String title;
        
        @NotBlank(message = "Description is required")
        private String description;
        
        @NotBlank(message = "Category is required")
        @Size(max = 60)
        private String category;
        
        @NotBlank(message = "Difficulty level is required")
        private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    }

    @Data
    public static class UpdateCourseRequest {
        private String title;
        private String description;
        private String category;
        private String difficultyLevel;
    }

    @Data
    public static class CourseResponse {
        private Long id;
        private String title;
        private String description;
        private String category;
        private String difficultyLevel;
        private Boolean isPublished;
        private String createdByName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer totalModules;
        private Integer totalLessons;
        private Integer enrolledStudents;
    }

    @Data
    public static class CreateModuleRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        private String title;
        
        private String description;
        private Integer orderIndex;
    }

    @Data
    public static class ModuleResponse {
        private Long id;
        private String title;
        private String description;
        private Integer orderIndex;
        private Integer totalLessons;
        private List<LessonResponse> lessons;
    }

    @Data
    public static class CreateLessonRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        private String title;
        
        @NotBlank(message = "Content type is required")
        private String contentType; // VIDEO, TEXT, QUIZ, CODE
        
        @NotBlank(message = "Content data is required")
        private String contentData; // JSON string
        
        private Integer orderIndex;
        private Integer durationMinutes;
    }

    @Data
    public static class LessonResponse {
        private Long id;
        private String title;
        private String contentType;
        private String contentData;
        private Integer orderIndex;
        private Integer durationMinutes;
        private Boolean completed;
    }

    @Data
    public static class EnrollmentResponse {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private Long studentId;
        private String studentName;
        private LocalDateTime enrolledAt;
        private LocalDateTime lastAccessedAt;
        private BigDecimal completionPercentage;
    }

    @Data
    public static class CourseContentResponse {
        private CourseResponse course;
        private List<ModuleResponse> modules;
        private EnrollmentResponse enrollment;
    }
}
