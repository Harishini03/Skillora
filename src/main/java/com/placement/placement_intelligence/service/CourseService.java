package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.model.*;
import com.placement.placement_intelligence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Production-ready Course Management Service
 * Handles all course operations with proper validation and error handling
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final CourseLessonRepository lessonRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonCompletionRepository completionRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    // ============= COURSE CRUD OPERATIONS =============

    @Transactional
    public Course createCourse(String title, String description, String category, 
                              String difficultyLevel, Long createdByUserId) {
        log.info("Creating course: {} by user: {}", title, createdByUserId);
        
        User creator = userRepository.findById(createdByUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (creator.getRole() != Role.STAFF && creator.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("Only STAFF and RECRUITER can create courses");
        }

        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setCategory(category);
        course.setDifficultyLevel(difficultyLevel);
        course.setCreatedBy(creator);
        course.setIsPublished(false);
        
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, String title, String description, 
                              String category, String difficultyLevel) {
        log.info("Updating course: {}", courseId);
        
        Course course = getCourseOrThrow(courseId);
        
        if (title != null) course.setTitle(title);
        if (description != null) course.setDescription(description);
        if (category != null) course.setCategory(category);
        if (difficultyLevel != null) course.setDifficultyLevel(difficultyLevel);
        
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        log.info("Deleting course: {}", courseId);
        
        Course course = getCourseOrThrow(courseId);
        
        // Check if course has enrollments
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse_Id(courseId);
        if (!enrollments.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete course with active enrollments");
        }
        
        courseRepository.delete(course);
    }

    @Transactional
    public Course publishCourse(Long courseId) {
        log.info("Publishing course: {}", courseId);
        
        Course course = getCourseOrThrow(courseId);
        
        // Validate course has content
        List<CourseModule> modules = moduleRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("Cannot publish course without modules");
        }
        
        course.setIsPublished(true);
        return courseRepository.save(course);
    }

    @Transactional
    public Course unpublishCourse(Long courseId) {
        log.info("Unpublishing course: {}", courseId);
        
        Course course = getCourseOrThrow(courseId);
        course.setIsPublished(false);
        return courseRepository.save(course);
    }

    // ============= MODULE OPERATIONS =============

    @Transactional
    public CourseModule addModule(Long courseId, String title, String description, Integer orderIndex) {
        log.info("Adding module to course: {}", courseId);
        
        Course course = getCourseOrThrow(courseId);
        
        CourseModule module = new CourseModule();
        module.setCourse(course);
        module.setTitle(title);
        module.setDescription(description);
        module.setOrderIndex(orderIndex != null ? orderIndex : getNextModuleOrderIndex(courseId));
        
        return moduleRepository.save(module);
    }

    @Transactional
    public CourseModule updateModule(Long moduleId, String title, String description, Integer orderIndex) {
        log.info("Updating module: {}", moduleId);
        
        CourseModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        if (title != null) module.setTitle(title);
        if (description != null) module.setDescription(description);
        if (orderIndex != null) module.setOrderIndex(orderIndex);
        
        return moduleRepository.save(module);
    }

    @Transactional
    public void deleteModule(Long moduleId) {
        log.info("Deleting module: {}", moduleId);
        
        CourseModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        moduleRepository.delete(module);
    }

    // ============= LESSON OPERATIONS =============

    @Transactional
    public CourseLesson addLesson(Long moduleId, String title, String contentType, 
                                 String contentData, Integer orderIndex, Integer durationMinutes) {
        log.info("Adding lesson to module: {}", moduleId);
        
        CourseModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        CourseLesson lesson = new CourseLesson();
        lesson.setModule(module);
        lesson.setTitle(title);
        lesson.setContentType(contentType);
        lesson.setContentData(contentData);
        lesson.setOrderIndex(orderIndex != null ? orderIndex : getNextLessonOrderIndex(moduleId));
        lesson.setDurationMinutes(durationMinutes);
        
        return lessonRepository.save(lesson);
    }

    @Transactional
    public CourseLesson updateLesson(Long lessonId, String title, String contentType, 
                                    String contentData, Integer orderIndex, Integer durationMinutes) {
        log.info("Updating lesson: {}", lessonId);
        
        CourseLesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        
        if (title != null) lesson.setTitle(title);
        if (contentType != null) lesson.setContentType(contentType);
        if (contentData != null) lesson.setContentData(contentData);
        if (orderIndex != null) lesson.setOrderIndex(orderIndex);
        if (durationMinutes != null) lesson.setDurationMinutes(durationMinutes);
        
        return lessonRepository.save(lesson);
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        log.info("Deleting lesson: {}", lessonId);
        
        CourseLesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        
        lessonRepository.delete(lesson);
    }

    // ============= ENROLLMENT OPERATIONS =============

    @Transactional
    public CourseEnrollment enrollStudent(Long courseId, Long studentId) {
        log.info("Enrolling student {} in course {}", studentId, courseId);
        
        Course course = getCourseOrThrow(courseId);
        
        if (!course.getIsPublished()) {
            throw new IllegalArgumentException("Cannot enroll in unpublished course");
        }
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        // Check if already enrolled
        if (enrollmentRepository.existsByCourse_IdAndStudent_Id(courseId, studentId)) {
            throw new IllegalArgumentException("Student already enrolled in this course");
        }
        
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setCompletionPercentage(BigDecimal.ZERO);
        
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public LessonCompletion markLessonComplete(Long enrollmentId, Long lessonId) {
        log.info("Marking lesson {} complete for enrollment {}", lessonId, enrollmentId);
        
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        
        CourseLesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        
        // Check if already completed
        if (completionRepository.existsByEnrollment_IdAndLesson_Id(enrollmentId, lessonId)) {
            throw new IllegalArgumentException("Lesson already completed");
        }
        
        LessonCompletion completion = new LessonCompletion();
        completion.setEnrollment(enrollment);
        completion.setLesson(lesson);
        
        LessonCompletion saved = completionRepository.save(completion);
        
        // Update enrollment progress
        updateEnrollmentProgress(enrollmentId);
        
        // Update student's last accessed time
        enrollment.setLastAccessedAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);
        
        return saved;
    }

    @Transactional
    public void updateEnrollmentProgress(Long enrollmentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        
        Long courseId = enrollment.getCourse().getId();
        
        // Get total lessons in course
        List<CourseModule> modules = moduleRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
        long totalLessons = modules.stream()
            .mapToLong(module -> lessonRepository.findByModule_IdOrderByOrderIndexAsc(module.getId()).size())
            .sum();
        
        if (totalLessons == 0) {
            enrollment.setCompletionPercentage(BigDecimal.ZERO);
        } else {
            // Get completed lessons
            long completedLessons = completionRepository.findByEnrollment_Id(enrollmentId).size();
            
            BigDecimal percentage = BigDecimal.valueOf(completedLessons)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);
            
            enrollment.setCompletionPercentage(percentage);
        }
        
        enrollmentRepository.save(enrollment);
    }

    // ============= QUERY OPERATIONS =============

    @Transactional(readOnly = true)
    public List<Course> getAllPublishedCourses() {
        return courseRepository.findByIsPublishedTrue();
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByCategory(String category) {
        return courseRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByDifficulty(String difficultyLevel) {
        return courseRepository.findByDifficultyLevel(difficultyLevel);
    }

    @Transactional(readOnly = true)
    public Course getCourse(Long courseId) {
        return getCourseOrThrow(courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseModule> getCourseModules(Long courseId) {
        return moduleRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseLesson> getModuleLessons(Long moduleId) {
        return lessonRepository.findByModule_IdOrderByOrderIndexAsc(moduleId);
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollment> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudent_Id(studentId);
    }

    @Transactional(readOnly = true)
    public CourseEnrollment getEnrollment(Long courseId, Long studentId) {
        return enrollmentRepository.findByCourse_IdAndStudent_Id(courseId, studentId)
            .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
    }

    @Transactional(readOnly = true)
    public List<LessonCompletion> getCompletedLessons(Long enrollmentId) {
        return completionRepository.findByEnrollment_Id(enrollmentId);
    }

    // ============= HELPER METHODS =============

    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
    }

    private Integer getNextModuleOrderIndex(Long courseId) {
        List<CourseModule> modules = moduleRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
        return modules.isEmpty() ? 0 : modules.get(modules.size() - 1).getOrderIndex() + 1;
    }

    private Integer getNextLessonOrderIndex(Long moduleId) {
        List<CourseLesson> lessons = lessonRepository.findByModule_IdOrderByOrderIndexAsc(moduleId);
        return lessons.isEmpty() ? 0 : lessons.get(lessons.size() - 1).getOrderIndex() + 1;
    }
}
