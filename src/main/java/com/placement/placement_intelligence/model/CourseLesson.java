package com.placement.placement_intelligence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course_lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseLesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content_type", nullable = false, length = 20)
    private String contentType; // VIDEO, TEXT, QUIZ, CODE

    @Column(name = "content_data", nullable = false, columnDefinition = "TEXT")
    private String contentData; // JSON with type-specific data

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;
}
