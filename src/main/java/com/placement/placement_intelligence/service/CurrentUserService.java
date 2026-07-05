package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.model.Role;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.User;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public CurrentUserService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Unauthenticated user");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Long getCurrentUserId() {
        return currentUser().getId();
    }

    public Long currentStudentId() {
        User user = currentUser();
        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Student account required");
        }
        Student student = studentRepository.findByUser_Id(user.getId());
        if (student == null) {
            throw new IllegalArgumentException("Student profile not found");
        }
        return student.getId();
    }

    public Long getCurrentStudentId() {
        return currentStudentId();
    }
}
