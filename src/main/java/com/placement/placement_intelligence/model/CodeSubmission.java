package com.placement.placement_intelligence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private CodingProblem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "language", nullable = false, length = 20)
    private String language; // JAVA, PYTHON, JAVASCRIPT

    @Column(name = "code_content", nullable = false, columnDefinition = "TEXT")
    private String codeContent;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // ACCEPTED, WRONG_ANSWER, TIME_LIMIT, RUNTIME_ERROR, COMPILE_ERROR

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "memory_used_mb", precision = 8, scale = 2)
    private BigDecimal memoryUsedMb;

    @Column(name = "test_cases_passed", nullable = false)
    private Integer testCasesPassed;

    @Column(name = "test_cases_total", nullable = false)
    private Integer testCasesTotal;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionTestResult> testResults = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
