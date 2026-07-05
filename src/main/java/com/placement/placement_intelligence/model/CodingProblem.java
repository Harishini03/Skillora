package com.placement.placement_intelligence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coding_problems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "difficulty_level", nullable = false, length = 20)
    private String difficultyLevel; // EASY, MEDIUM, HARD

    @Column(name = "topic_tags", length = 300)
    private String topicTags; // Comma-separated tags

    @Column(name = "time_limit_seconds", nullable = false)
    private Integer timeLimitSeconds = 5;

    @Column(name = "memory_limit_mb", nullable = false)
    private Integer memoryLimitMb = 256;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemTestCase> testCases = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    private List<CodeSubmission> submissions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
