package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.dto.AddEducationRequest;
import com.placement.placement_intelligence.dto.ProfileSkillDto;
import com.placement.placement_intelligence.dto.UpdateUserProfileRequest;
import com.placement.placement_intelligence.dto.UserProfileResponse;
import com.placement.placement_intelligence.model.Profile;
import com.placement.placement_intelligence.model.ProfileAnalytics;
import com.placement.placement_intelligence.model.ProfileEducation;
import com.placement.placement_intelligence.model.ProfileResume;
import com.placement.placement_intelligence.model.ProfileSkill;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.StudentTestAttempt;
import com.placement.placement_intelligence.repository.ProfileAnalyticsRepository;
import com.placement.placement_intelligence.repository.ProfileEducationRepository;
import com.placement.placement_intelligence.repository.ProfileRepository;
import com.placement.placement_intelligence.repository.ProfileResumeRepository;
import com.placement.placement_intelligence.repository.ProfileSkillRepository;
import com.placement.placement_intelligence.repository.StudentAnswerRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentTestAttemptRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserProfileService {

    private static final Path RESUME_DIR = Paths.get("uploads", "profile-resumes");
    private static final Path IMAGE_DIR = Paths.get("uploads", "profile-images");

    private final StudentRepository studentRepository;
    private final ProfileRepository profileRepository;
    private final ProfileEducationRepository educationRepository;
    private final ProfileSkillRepository skillRepository;
    private final ProfileResumeRepository resumeRepository;
    private final ProfileAnalyticsRepository analyticsRepository;
    private final StudentTestAttemptRepository attemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public UserProfileService(StudentRepository studentRepository,
                              ProfileRepository profileRepository,
                              ProfileEducationRepository educationRepository,
                              ProfileSkillRepository skillRepository,
                              ProfileResumeRepository resumeRepository,
                              ProfileAnalyticsRepository analyticsRepository,
                              StudentTestAttemptRepository attemptRepository,
                              StudentAnswerRepository studentAnswerRepository) {
        this.studentRepository = studentRepository;
        this.profileRepository = profileRepository;
        this.educationRepository = educationRepository;
        this.skillRepository = skillRepository;
        this.resumeRepository = resumeRepository;
        this.analyticsRepository = analyticsRepository;
        this.attemptRepository = attemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
    }

    @Transactional
    public UserProfileResponse getProfile(Long studentId) {
        Student student = requireStudent(studentId);
        Profile profile = ensureProfile(student);
        ProfileAnalytics analytics = refreshAnalytics(profile);
        return mapToResponse(student, profile, analytics);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long studentId, UpdateUserProfileRequest request) {
        Student student = requireStudent(studentId);
        Profile profile = ensureProfile(student);

        profile.setFirstName(trimToNull(request.getFirstName()));
        profile.setLastName(trimToNull(request.getLastName()));
        profile.setPersonalEmail(trimToNull(request.getPersonalEmail()));
        profile.setCollegeEmail(trimToNull(request.getCollegeEmail()));
        profile.setMobileNumber(trimToNull(request.getMobileNumber()));
        profile.setAlternateMobileNumber(trimToNull(request.getAlternateMobileNumber()));
        profile.setWhatsappNumber(trimToNull(request.getWhatsappNumber()));
        profile.setVisibleToHr(request.isVisibleToHr());

        profile.setAddressLine1(trimToNull(request.getAddressLine1()));
        profile.setAddressLine2(trimToNull(request.getAddressLine2()));
        profile.setCity(trimToNull(request.getCity()));
        profile.setState(trimToNull(request.getState()));
        profile.setPincode(trimToNull(request.getPincode()));

        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(trimToNull(request.getGender()));

        profile.setFatherName(trimToNull(request.getFatherName()));
        profile.setFatherContactNumber(trimToNull(request.getFatherContactNumber()));
        profile.setMotherName(trimToNull(request.getMotherName()));
        profile.setMotherContactNumber(trimToNull(request.getMotherContactNumber()));
        profile.setAboutMe(trimToNull(request.getAboutMe()));
        profile.setProfileImageVisibleToHr(request.isProfileImageVisibleToHr());

        profileRepository.save(profile);

        skillRepository.deleteByProfile_Id(profile.getId());
        List<ProfileSkill> skillEntities = new ArrayList<>();
        for (ProfileSkillDto skill : request.getSkills()) {
            String skillName = trimToNull(skill.getSkillName());
            if (skillName == null) {
                continue;
            }
            ProfileSkill entity = new ProfileSkill();
            entity.setProfile(profile);
            entity.setSkillName(skillName);
            entity.setSkillCategory(skill.getSkillCategory());
            entity.setSkillLevel(skill.getSkillLevel());
            skillEntities.add(entity);
        }
        if (!skillEntities.isEmpty()) {
            skillRepository.saveAll(skillEntities);
        }

        ProfileAnalytics analytics = refreshAnalytics(profile);
        return mapToResponse(student, profile, analytics);
    }

    @Transactional
    public UserProfileResponse.EducationItem addEducation(Long studentId, AddEducationRequest request) {
        request.validateYear();
        Profile profile = ensureProfile(requireStudent(studentId));
        ProfileEducation education = new ProfileEducation();
        education.setProfile(profile);
        education.setInstitutionName(request.getInstitutionName().trim());
        education.setDegree(request.getDegree().trim());
        education.setYear(request.getYear());
        education.setCgpaOrPercentage(request.getCgpaOrPercentage().trim());
        ProfileEducation saved = educationRepository.save(education);
        return mapEducation(saved);
    }

    @Transactional
    public UserProfileResponse.EducationItem updateEducation(Long studentId, Long educationId, AddEducationRequest request) {
        request.validateYear();
        Profile profile = ensureProfile(requireStudent(studentId));
        ProfileEducation education = educationRepository.findByIdAndProfile_Id(educationId, profile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Education record not found"));
        education.setInstitutionName(request.getInstitutionName().trim());
        education.setDegree(request.getDegree().trim());
        education.setYear(request.getYear());
        education.setCgpaOrPercentage(request.getCgpaOrPercentage().trim());
        return mapEducation(educationRepository.save(education));
    }

    @Transactional
    public void deleteEducation(Long studentId, Long educationId) {
        Profile profile = ensureProfile(requireStudent(studentId));
        ProfileEducation education = educationRepository.findByIdAndProfile_Id(educationId, profile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Education record not found"));
        educationRepository.delete(education);
    }

    @Transactional
    public UserProfileResponse.ResumeInfo uploadResume(Long studentId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lower = originalName.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF resumes are supported");
        }

        Profile profile = ensureProfile(requireStudent(studentId));
        Files.createDirectories(RESUME_DIR);

        ProfileResume resume = resumeRepository.findByProfile_Id(profile.getId()).orElseGet(ProfileResume::new);
        if (resume.getFilePath() != null) {
            deleteIfExists(resume.getFilePath());
        }

        String safeName = sanitizeFileName(originalName);
        String serverName = "resume_" + profile.getId() + "_" + System.currentTimeMillis() + "_" + safeName;
        Path target = RESUME_DIR.resolve(serverName).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        resume.setProfile(profile);
        resume.setFilePath(target.toString());
        resume.setFileName(safeName);
        resume.setContentType("application/pdf");
        resume.setSizeBytes(file.getSize());
        resumeRepository.save(resume);

        Student student = profile.getStudent();
        student.setResumePath(target.toString());
        studentRepository.save(student);

        return mapResume(resume);
    }

    @Transactional
    public void deleteResume(Long studentId) {
        Profile profile = ensureProfile(requireStudent(studentId));
        Optional<ProfileResume> existing = resumeRepository.findByProfile_Id(profile.getId());
        if (existing.isPresent()) {
            ProfileResume resume = existing.get();
            deleteIfExists(resume.getFilePath());
            resumeRepository.delete(resume);
        }
        Student student = profile.getStudent();
        student.setResumePath(null);
        studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public FilePayload resumePayload(Long studentId) throws IOException {
        Profile profile = ensureProfile(requireStudent(studentId));
        ProfileResume resume = resumeRepository.findByProfile_Id(profile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Resume not uploaded yet"));
        Path path = Paths.get(resume.getFilePath());
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Resume file not found on server");
        }
        return new FilePayload(
                new ByteArrayResource(Files.readAllBytes(path)),
                resume.getFileName(),
                MediaType.APPLICATION_PDF
        );
    }

    @Transactional
    public UserProfileResponse.ProfileImageInfo uploadProfileImage(Long studentId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile image is required");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lower = originalName.toLowerCase(Locale.ROOT);
        String ext = extension(lower);
        if (!List.of("png", "jpg", "jpeg", "webp").contains(ext)) {
            throw new IllegalArgumentException("Supported image formats: png, jpg, jpeg, webp");
        }

        Profile profile = ensureProfile(requireStudent(studentId));
        Files.createDirectories(IMAGE_DIR);

        if (profile.getProfileImagePath() != null) {
            deleteIfExists(profile.getProfileImagePath());
        }

        String safeName = sanitizeFileName(originalName);
        String serverName = "profile_" + profile.getId() + "_" + System.currentTimeMillis() + "_" + safeName;
        Path target = IMAGE_DIR.resolve(serverName).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        profile.setProfileImagePath(target.toString());
        profileRepository.save(profile);
        return mapProfileImage(profile);
    }

    @Transactional
    public void deleteProfileImage(Long studentId) {
        Profile profile = ensureProfile(requireStudent(studentId));
        if (profile.getProfileImagePath() != null) {
            deleteIfExists(profile.getProfileImagePath());
            profile.setProfileImagePath(null);
            profileRepository.save(profile);
        }
    }

    @Transactional(readOnly = true)
    public FilePayload profileImagePayload(Long studentId) throws IOException {
        Profile profile = ensureProfile(requireStudent(studentId));
        if (profile.getProfileImagePath() == null || profile.getProfileImagePath().isBlank()) {
            throw new IllegalArgumentException("Profile image not uploaded yet");
        }
        Path path = Paths.get(profile.getProfileImagePath());
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Profile image not found on server");
        }
        String ext = extension(path.getFileName().toString().toLowerCase(Locale.ROOT));
        MediaType mediaType = switch (ext) {
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
        return new FilePayload(
                new ByteArrayResource(Files.readAllBytes(path)),
                path.getFileName().toString(),
                mediaType
        );
    }

    private Student requireStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }

    private Profile ensureProfile(Student student) {
        return profileRepository.findByStudent_Id(student.getId())
                .orElseGet(() -> {
                    Profile profile = new Profile();
                    profile.setStudent(student);
                    if (student.getName() != null) {
                        String[] names = student.getName().trim().split("\\s+", 2);
                        profile.setFirstName(names[0]);
                        profile.setLastName(names.length > 1 ? names[1] : "");
                    }
                    profile.setCollegeEmail(student.getUser() == null ? null : student.getUser().getEmail());
                    profile.setMobileNumber(student.getPhone());
                    profile.setAboutMe(student.getAchievements());
                    return profileRepository.save(profile);
                });
    }

    private ProfileAnalytics refreshAnalytics(Profile profile) {
        List<StudentTestAttempt> attempts = attemptRepository.findByStudent_IdOrderByTestDateDesc(profile.getStudent().getId());
        double totalScore = 0;
        double totalQuestions = 0;
        double totalSeconds = 0;
        for (StudentTestAttempt attempt : attempts) {
            int questions = attempt.getTotalQuestions() == null ? 0 : attempt.getTotalQuestions();
            int score = attempt.getScore() == null ? 0 : attempt.getScore();
            int duration = attempt.getSession() == null || attempt.getSession().getDurationMinutes() == null
                    ? 0 : attempt.getSession().getDurationMinutes() * 60;
            totalScore += score;
            totalQuestions += questions;
            if (duration > 0) {
                totalSeconds += duration;
            }
        }

        double scorePct = totalQuestions == 0 ? defaultReadiness(profile.getStudent()) : round((totalScore * 100.0) / totalQuestions);
        double accuracyPct = scorePct;
        double avgTime = totalQuestions == 0 ? 0.0 : round(totalSeconds / totalQuestions);

        String strengths = topTopics(profile.getStudent().getId(), true);
        String weaknesses = topTopics(profile.getStudent().getId(), false);
        String insight = "You are strong in " + strengths + " but weak in " + weaknesses + ".";
        String strategy = "Follow 45-minute daily drills, alternating weak-topic practice with mixed mock revision.";
        String weakAreas = weaknesses + ", optimization problems";
        String suggestedTopics = "Time complexity, dynamic programming basics, and verbal precision practice";

        ProfileAnalytics analytics = analyticsRepository.findByProfile_Id(profile.getId()).orElseGet(ProfileAnalytics::new);
        analytics.setProfile(profile);
        analytics.setTestScores(scorePct);
        analytics.setAccuracyPercentage(accuracyPct);
        analytics.setAverageTimePerQuestion(avgTime);
        analytics.setStrengths(strengths);
        analytics.setWeaknesses(weaknesses);
        analytics.setInsightSummary(insight);
        analytics.setRecommendedLearningStrategy(strategy);
        analytics.setWeakAreas(weakAreas);
        analytics.setSuggestedTopics(suggestedTopics);
        return analyticsRepository.save(analytics);
    }

    private String topTopics(Long studentId, boolean strong) {
        List<Object[]> rows = studentAnswerRepository.aggregateTopicPerformanceByStudent(studentId);
        if (rows.isEmpty()) {
            return strong ? "pattern recognition" : "optimization problems";
        }
        String selected = null;
        double best = strong ? -1 : 101;
        for (Object[] row : rows) {
            String topic = String.valueOf(row[0]);
            double correct = row[1] == null ? 0 : ((Number) row[1]).doubleValue();
            double total = row[2] == null ? 0 : ((Number) row[2]).doubleValue();
            double pct = total == 0 ? 0 : (correct * 100.0 / total);
            if (strong && pct > best) {
                best = pct;
                selected = topic;
            }
            if (!strong && pct < best) {
                best = pct;
                selected = topic;
            }
        }
        return selected == null ? (strong ? "pattern recognition" : "optimization problems") : selected;
    }

    private double defaultReadiness(Student student) {
        return round(student.getReadinessScore() == null ? 0.0 : student.getReadinessScore());
    }

    private UserProfileResponse mapToResponse(Student student, Profile profile, ProfileAnalytics analytics) {
        UserProfileResponse response = new UserProfileResponse();
        response.setStudentId(student.getId());
        response.setStudentName(student.getName());
        response.setRole("Student");

        UserProfileResponse.PersonalInfo personal = new UserProfileResponse.PersonalInfo();
        personal.setFirstName(profile.getFirstName());
        personal.setLastName(profile.getLastName());
        personal.setPersonalEmail(profile.getPersonalEmail());
        personal.setCollegeEmail(profile.getCollegeEmail());
        personal.setMobileNumber(profile.getMobileNumber());
        personal.setAlternateMobileNumber(profile.getAlternateMobileNumber());
        personal.setWhatsappNumber(profile.getWhatsappNumber());
        personal.setVisibleToHr(profile.isVisibleToHr());
        response.setPersonalInfo(personal);

        UserProfileResponse.AddressInfo address = new UserProfileResponse.AddressInfo();
        address.setAddressLine1(profile.getAddressLine1());
        address.setAddressLine2(profile.getAddressLine2());
        address.setCity(profile.getCity());
        address.setState(profile.getState());
        address.setPincode(profile.getPincode());
        response.setAddress(address);

        UserProfileResponse.DemographicInfo demographic = new UserProfileResponse.DemographicInfo();
        demographic.setDateOfBirth(profile.getDateOfBirth() == null ? "" : profile.getDateOfBirth().toString());
        demographic.setAge(calculateAge(profile.getDateOfBirth()));
        demographic.setGender(profile.getGender());
        response.setDemographic(demographic);

        UserProfileResponse.ParentInfo parentInfo = new UserProfileResponse.ParentInfo();
        parentInfo.setFatherName(profile.getFatherName());
        parentInfo.setFatherContactNumber(profile.getFatherContactNumber());
        parentInfo.setMotherName(profile.getMotherName());
        parentInfo.setMotherContactNumber(profile.getMotherContactNumber());
        response.setParentInfo(parentInfo);

        UserProfileResponse.AboutInfo about = new UserProfileResponse.AboutInfo();
        String aboutText = profile.getAboutMe() == null ? "" : profile.getAboutMe();
        about.setContent(aboutText);
        about.setCharacterCount(aboutText.length());
        about.setMaxCharacters(2000);
        response.setAbout(about);

        response.setEducationHistory(educationRepository.findByProfile_IdOrderByYearDesc(profile.getId())
                .stream()
                .map(this::mapEducation)
                .toList());

        response.setResume(mapResume(resumeRepository.findByProfile_Id(profile.getId()).orElse(null)));
        response.setProfileImage(mapProfileImage(profile));

        response.setSkills(skillRepository.findByProfile_IdOrderBySkillNameAsc(profile.getId()).stream()
                .map(this::mapSkill)
                .toList());

        UserProfileResponse.PerformanceAnalytics performance = new UserProfileResponse.PerformanceAnalytics();
        performance.setTestScores(round(analytics.getTestScores()));
        performance.setAverageTimePerQuestion(round(analytics.getAverageTimePerQuestion()));
        performance.setAccuracyPercentage(round(analytics.getAccuracyPercentage()));
        performance.setStrengths(analytics.getStrengths());
        performance.setWeaknesses(analytics.getWeaknesses());
        performance.setInsightSummary(analytics.getInsightSummary());
        response.setAnalytics(performance);

        UserProfileResponse.LearningInsights learning = new UserProfileResponse.LearningInsights();
        learning.setRecommendedLearningStrategy(analytics.getRecommendedLearningStrategy());
        learning.setWeakAreas(analytics.getWeakAreas());
        learning.setSuggestedTopics(analytics.getSuggestedTopics());
        response.setLearningInsights(learning);
        return response;
    }

    private UserProfileResponse.EducationItem mapEducation(ProfileEducation education) {
        UserProfileResponse.EducationItem dto = new UserProfileResponse.EducationItem();
        dto.setId(education.getId());
        dto.setInstitutionName(education.getInstitutionName());
        dto.setDegree(education.getDegree());
        dto.setYear(education.getYear());
        dto.setCgpaOrPercentage(education.getCgpaOrPercentage());
        return dto;
    }

    private UserProfileResponse.SkillItem mapSkill(ProfileSkill skill) {
        UserProfileResponse.SkillItem dto = new UserProfileResponse.SkillItem();
        dto.setId(skill.getId());
        dto.setSkillName(skill.getSkillName());
        dto.setSkillCategory(skill.getSkillCategory());
        dto.setSkillLevel(skill.getSkillLevel());
        return dto;
    }

    private UserProfileResponse.ResumeInfo mapResume(ProfileResume resume) {
        UserProfileResponse.ResumeInfo dto = new UserProfileResponse.ResumeInfo();
        if (resume == null) {
            dto.setUploaded(false);
            return dto;
        }
        dto.setUploaded(true);
        dto.setFileName(resume.getFileName());
        dto.setContentType(resume.getContentType());
        dto.setSizeBytes(resume.getSizeBytes());
        dto.setPreviewUrl("/api/student/user-profile/resume/view");
        return dto;
    }

    private UserProfileResponse.ProfileImageInfo mapProfileImage(Profile profile) {
        UserProfileResponse.ProfileImageInfo dto = new UserProfileResponse.ProfileImageInfo();
        dto.setUploaded(profile.getProfileImagePath() != null && !profile.getProfileImagePath().isBlank());
        dto.setVisibleToHr(profile.isProfileImageVisibleToHr());
        dto.setPreviewUrl(dto.isUploaded() ? "/api/student/user-profile/profile-image/view" : null);
        return dto;
    }

    private Integer calculateAge(LocalDate dob) {
        if (dob == null) {
            return null;
        }
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String extension(String fileName) {
        int idx = fileName.lastIndexOf(".");
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx + 1);
    }

    private void deleteIfExists(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException ignored) {
            // Intentionally ignored to keep profile operations resilient to stale files.
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record FilePayload(ByteArrayResource resource, String fileName, MediaType contentType) {
    }
}
