package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.CourseDto;
import com.placement.placement_intelligence.model.*;
import com.placement.placement_intelligence.service.CourseService;
import com.placement.placement_intelligence.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174"})
public class CourseController {

    private final CourseService courseService;
    private final CurrentUserService currentUserService;

    // ============= STAFF ENDPOINTS =============

    @PostMapping
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CourseDto.CourseResponse> createCourse(@Valid @RequestBody CourseDto.CreateCourseRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        Course course = courseService.createCourse(
            request.getTitle(),
            request.getDescription(),
            request.getCategory(),
            request.getDifficultyLevel(),
            userId
        );
        return ResponseEntity.ok(mapToCourseResponse(course));
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CourseDto.CourseResponse> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseDto.UpdateCourseRequest request) {
        Course course = courseService.updateCourse(
            courseId,
            request.getTitle(),
            request.getDescription(),
            request.getCategory(),
            request.getDifficultyLevel()
        );
        return ResponseEntity.ok(mapToCourseResponse(course));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/publish")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CourseDto.CourseResponse> publishCourse(@PathVariable Long courseId) {
        Course course = courseService.publishCourse(courseId);
        return ResponseEntity.ok(mapToCourseResponse(course));
    }

    @PostMapping("/{courseId}/unpublish")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CourseDto.CourseResponse> unpublishCourse(@PathVariable Long courseId) {
        Course course = courseService.unpublishCourse(courseId);
        return ResponseEntity.ok(mapToCourseResponse(course));
    }

    @PostMapping("/{courseId}/modules")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CourseDto.ModuleResponse> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseDto.CreateModuleRequest request) {
        CourseModule module = courseService.addModule(
            courseId,
            request.getTitle(),
            request.getDescription(),
            request.getOrderIndex()
        );
        return ResponseEntity.ok(mapToModuleResponse(module));
    }

    @PutMapping("/modules/{moduleId}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CourseDto.ModuleResponse> updateModule(
            @PathVariable Long moduleId,
            @Valid @RequestBody CourseDto.CreateModuleRequest request) {
        CourseModule module = courseService.updateModule(
            moduleId,
            request.getTitle(),
            request.getDescription(),
            request.getOrderIndex()
        );
        return ResponseEntity.ok(mapToModuleResponse(module));
    }

    @DeleteMapping("/modules/{moduleId}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<Void> deleteModule(@PathVariable Long moduleId) {
        courseService.deleteModule(moduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/modules/{moduleId}/lessons")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CourseDto.LessonResponse> addLesson(
            @PathVariable Long moduleId,
            @Valid @RequestBody CourseDto.CreateLessonRequest request) {
        CourseLesson lesson = courseService.addLesson(
            moduleId,
            request.getTitle(),
            request.getContentType(),
            request.getContentData(),
            request.getOrderIndex(),
            request.getDurationMinutes()
        );
        return ResponseEntity.ok(mapToLessonResponse(lesson, false));
    }

    @PutMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<CourseDto.LessonResponse> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody CourseDto.CreateLessonRequest request) {
        CourseLesson lesson = courseService.updateLesson(
            lessonId,
            request.getTitle(),
            request.getContentType(),
            request.getContentData(),
            request.getOrderIndex(),
            request.getDurationMinutes()
        );
        return ResponseEntity.ok(mapToLessonResponse(lesson, false));
    }

    @DeleteMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'RECRUITER')")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        courseService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    // ============= STUDENT ENDPOINTS =============

    @GetMapping
    public ResponseEntity<List<CourseDto.CourseResponse>> getAllCourses() {
        List<Course> courses = courseService.getAllPublishedCourses();
        return ResponseEntity.ok(
            courses.stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDto.CourseResponse> getCourse(@PathVariable Long courseId) {
        Course course = courseService.getCourse(courseId);
        return ResponseEntity.ok(mapToCourseResponse(course));
    }

    @GetMapping("/{courseId}/content")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<CourseDto.CourseContentResponse> getCourseContent(@PathVariable Long courseId) {
        Long studentId = currentUserService.getCurrentStudentId();
        
        Course course = courseService.getCourse(courseId);
        List<CourseModule> modules = courseService.getCourseModules(courseId);
        CourseEnrollment enrollment = courseService.getEnrollment(courseId, studentId);
        List<LessonCompletion> completions = courseService.getCompletedLessons(enrollment.getId());
        
        CourseDto.CourseContentResponse response = new CourseDto.CourseContentResponse();
        response.setCourse(mapToCourseResponse(course));
        response.setEnrollment(mapToEnrollmentResponse(enrollment));
        response.setModules(modules.stream()
            .map(module -> mapToModuleWithLessons(module, completions))
            .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<CourseDto.EnrollmentResponse> enrollInCourse(@PathVariable Long courseId) {
        Long studentId = currentUserService.getCurrentStudentId();
        CourseEnrollment enrollment = courseService.enrollStudent(courseId, studentId);
        return ResponseEntity.ok(mapToEnrollmentResponse(enrollment));
    }

    @PostMapping("/enrollments/{enrollmentId}/lessons/{lessonId}/complete")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<Void> markLessonComplete(
            @PathVariable Long enrollmentId,
            @PathVariable Long lessonId) {
        courseService.markLessonComplete(enrollmentId, lessonId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-enrollments")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<CourseDto.EnrollmentResponse>> getMyEnrollments() {
        Long studentId = currentUserService.getCurrentStudentId();
        List<CourseEnrollment> enrollments = courseService.getStudentEnrollments(studentId);
        return ResponseEntity.ok(
            enrollments.stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList())
        );
    }

    // ============= MAPPERS =============

    private CourseDto.CourseResponse mapToCourseResponse(Course course) {
        CourseDto.CourseResponse response = new CourseDto.CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setCategory(course.getCategory());
        response.setDifficultyLevel(course.getDifficultyLevel());
        response.setIsPublished(course.getIsPublished());
        response.setCreatedByName(course.getCreatedBy().getName());
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());
        return response;
    }

    private CourseDto.ModuleResponse mapToModuleResponse(CourseModule module) {
        CourseDto.ModuleResponse response = new CourseDto.ModuleResponse();
        response.setId(module.getId());
        response.setTitle(module.getTitle());
        response.setDescription(module.getDescription());
        response.setOrderIndex(module.getOrderIndex());
        return response;
    }

    private CourseDto.ModuleResponse mapToModuleWithLessons(CourseModule module, List<LessonCompletion> completions) {
        CourseDto.ModuleResponse response = mapToModuleResponse(module);
        List<CourseLesson> lessons = courseService.getModuleLessons(module.getId());
        response.setLessons(lessons.stream()
            .map(lesson -> {
                boolean completed = completions.stream()
                    .anyMatch(c -> c.getLesson().getId().equals(lesson.getId()));
                return mapToLessonResponse(lesson, completed);
            })
            .collect(Collectors.toList()));
        return response;
    }

    private CourseDto.LessonResponse mapToLessonResponse(CourseLesson lesson, boolean completed) {
        CourseDto.LessonResponse response = new CourseDto.LessonResponse();
        response.setId(lesson.getId());
        response.setTitle(lesson.getTitle());
        response.setContentType(lesson.getContentType());
        response.setContentData(lesson.getContentData());
        response.setOrderIndex(lesson.getOrderIndex());
        response.setDurationMinutes(lesson.getDurationMinutes());
        response.setCompleted(completed);
        return response;
    }

    private CourseDto.EnrollmentResponse mapToEnrollmentResponse(CourseEnrollment enrollment) {
        CourseDto.EnrollmentResponse response = new CourseDto.EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setCourseId(enrollment.getCourse().getId());
        response.setCourseTitle(enrollment.getCourse().getTitle());
        response.setStudentId(enrollment.getStudent().getId());
        response.setStudentName(enrollment.getStudent().getName());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setLastAccessedAt(enrollment.getLastAccessedAt());
        response.setCompletionPercentage(enrollment.getCompletionPercentage());
        return response;
    }
}
