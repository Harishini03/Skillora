package com.placement.placement_intelligence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "completion_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
    }
}
