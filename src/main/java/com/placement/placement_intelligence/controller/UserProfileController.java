package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.AddEducationRequest;
import com.placement.placement_intelligence.dto.UpdateUserProfileRequest;
import com.placement.placement_intelligence.dto.UserProfileResponse;
import com.placement.placement_intelligence.service.CurrentUserService;
import com.placement.placement_intelligence.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/student/user-profile")
public class UserProfileController {

    private final CurrentUserService currentUserService;
    private final UserProfileService userProfileService;

    public UserProfileController(CurrentUserService currentUserService,
                                 UserProfileService userProfileService) {
        this.currentUserService = currentUserService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userProfileService.getProfile(currentUserService.currentStudentId()));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(currentUserService.currentStudentId(), request));
    }

    @PostMapping("/education")
    public ResponseEntity<UserProfileResponse.EducationItem> addEducation(@Valid @RequestBody AddEducationRequest request) {
        return ResponseEntity.ok(userProfileService.addEducation(currentUserService.currentStudentId(), request));
    }

    @PutMapping("/education/{educationId}")
    public ResponseEntity<UserProfileResponse.EducationItem> updateEducation(@PathVariable Long educationId,
                                                                             @Valid @RequestBody AddEducationRequest request) {
        return ResponseEntity.ok(userProfileService.updateEducation(currentUserService.currentStudentId(), educationId, request));
    }

    @DeleteMapping("/education/{educationId}")
    public ResponseEntity<Map<String, String>> deleteEducation(@PathVariable Long educationId) {
        userProfileService.deleteEducation(currentUserService.currentStudentId(), educationId);
        return ResponseEntity.ok(Map.of("message", "Education deleted"));
    }

    @PostMapping("/resume")
    public ResponseEntity<UserProfileResponse.ResumeInfo> uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(userProfileService.uploadResume(currentUserService.currentStudentId(), file));
    }

    @DeleteMapping("/resume")
    public ResponseEntity<Map<String, String>> deleteResume() {
        userProfileService.deleteResume(currentUserService.currentStudentId());
        return ResponseEntity.ok(Map.of("message", "Resume deleted"));
    }

    @GetMapping("/resume/view")
    public ResponseEntity<?> viewResume() throws IOException {
        UserProfileService.FilePayload payload = userProfileService.resumePayload(currentUserService.currentStudentId());
        return ResponseEntity.ok()
                .contentType(payload.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + payload.fileName() + "\"")
                .body(payload.resource());
    }

    @PostMapping("/profile-image")
    public ResponseEntity<UserProfileResponse.ProfileImageInfo> uploadProfileImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(userProfileService.uploadProfileImage(currentUserService.currentStudentId(), file));
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<Map<String, String>> deleteProfileImage() {
        userProfileService.deleteProfileImage(currentUserService.currentStudentId());
        return ResponseEntity.ok(Map.of("message", "Profile image deleted"));
    }

    @GetMapping("/profile-image/view")
    public ResponseEntity<?> viewProfileImage() throws IOException {
        UserProfileService.FilePayload payload = userProfileService.profileImagePayload(currentUserService.currentStudentId());
        return ResponseEntity.ok()
                .contentType(payload.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + payload.fileName() + "\"")
                .body(payload.resource());
    }
}
