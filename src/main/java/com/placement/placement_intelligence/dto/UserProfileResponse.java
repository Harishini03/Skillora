package com.placement.placement_intelligence.dto;

import com.placement.placement_intelligence.model.ProfileSkillCategory;
import com.placement.placement_intelligence.model.ProfileSkillLevel;

import java.util.ArrayList;
import java.util.List;

public class UserProfileResponse {
    private Long studentId;
    private String studentName;
    private String role = "Student";
    private PersonalInfo personalInfo;
    private AddressInfo address;
    private DemographicInfo demographic;
    private ParentInfo parentInfo;
    private AboutInfo about;
    private List<EducationItem> educationHistory = new ArrayList<>();
    private ResumeInfo resume;
    private ProfileImageInfo profileImage;
    private List<SkillItem> skills = new ArrayList<>();
    private PerformanceAnalytics analytics;
    private LearningInsights learningInsights;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public PersonalInfo getPersonalInfo() {
        return personalInfo;
    }

    public void setPersonalInfo(PersonalInfo personalInfo) {
        this.personalInfo = personalInfo;
    }

    public AddressInfo getAddress() {
        return address;
    }

    public void setAddress(AddressInfo address) {
        this.address = address;
    }

    public DemographicInfo getDemographic() {
        return demographic;
    }

    public void setDemographic(DemographicInfo demographic) {
        this.demographic = demographic;
    }

    public ParentInfo getParentInfo() {
        return parentInfo;
    }

    public void setParentInfo(ParentInfo parentInfo) {
        this.parentInfo = parentInfo;
    }

    public AboutInfo getAbout() {
        return about;
    }

    public void setAbout(AboutInfo about) {
        this.about = about;
    }

    public List<EducationItem> getEducationHistory() {
        return educationHistory;
    }

    public void setEducationHistory(List<EducationItem> educationHistory) {
        this.educationHistory = educationHistory;
    }

    public ResumeInfo getResume() {
        return resume;
    }

    public void setResume(ResumeInfo resume) {
        this.resume = resume;
    }

    public ProfileImageInfo getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(ProfileImageInfo profileImage) {
        this.profileImage = profileImage;
    }

    public List<SkillItem> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillItem> skills) {
        this.skills = skills;
    }

    public PerformanceAnalytics getAnalytics() {
        return analytics;
    }

    public void setAnalytics(PerformanceAnalytics analytics) {
        this.analytics = analytics;
    }

    public LearningInsights getLearningInsights() {
        return learningInsights;
    }

    public void setLearningInsights(LearningInsights learningInsights) {
        this.learningInsights = learningInsights;
    }

    public static class PersonalInfo {
        private String firstName;
        private String lastName;
        private String personalEmail;
        private String collegeEmail;
        private String mobileNumber;
        private String alternateMobileNumber;
        private String whatsappNumber;
        private boolean visibleToHr;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPersonalEmail() {
            return personalEmail;
        }

        public void setPersonalEmail(String personalEmail) {
            this.personalEmail = personalEmail;
        }

        public String getCollegeEmail() {
            return collegeEmail;
        }

        public void setCollegeEmail(String collegeEmail) {
            this.collegeEmail = collegeEmail;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public String getAlternateMobileNumber() {
            return alternateMobileNumber;
        }

        public void setAlternateMobileNumber(String alternateMobileNumber) {
            this.alternateMobileNumber = alternateMobileNumber;
        }

        public String getWhatsappNumber() {
            return whatsappNumber;
        }

        public void setWhatsappNumber(String whatsappNumber) {
            this.whatsappNumber = whatsappNumber;
        }

        public boolean isVisibleToHr() {
            return visibleToHr;
        }

        public void setVisibleToHr(boolean visibleToHr) {
            this.visibleToHr = visibleToHr;
        }
    }

    public static class AddressInfo {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;

        public String getAddressLine1() {
            return addressLine1;
        }

        public void setAddressLine1(String addressLine1) {
            this.addressLine1 = addressLine1;
        }

        public String getAddressLine2() {
            return addressLine2;
        }

        public void setAddressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPincode() {
            return pincode;
        }

        public void setPincode(String pincode) {
            this.pincode = pincode;
        }
    }

    public static class DemographicInfo {
        private String dateOfBirth;
        private Integer age;
        private String gender;

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }
    }

    public static class ParentInfo {
        private String fatherName;
        private String fatherContactNumber;
        private String motherName;
        private String motherContactNumber;

        public String getFatherName() {
            return fatherName;
        }

        public void setFatherName(String fatherName) {
            this.fatherName = fatherName;
        }

        public String getFatherContactNumber() {
            return fatherContactNumber;
        }

        public void setFatherContactNumber(String fatherContactNumber) {
            this.fatherContactNumber = fatherContactNumber;
        }

        public String getMotherName() {
            return motherName;
        }

        public void setMotherName(String motherName) {
            this.motherName = motherName;
        }

        public String getMotherContactNumber() {
            return motherContactNumber;
        }

        public void setMotherContactNumber(String motherContactNumber) {
            this.motherContactNumber = motherContactNumber;
        }
    }

    public static class AboutInfo {
        private String content;
        private int characterCount;
        private int maxCharacters = 2000;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getCharacterCount() {
            return characterCount;
        }

        public void setCharacterCount(int characterCount) {
            this.characterCount = characterCount;
        }

        public int getMaxCharacters() {
            return maxCharacters;
        }

        public void setMaxCharacters(int maxCharacters) {
            this.maxCharacters = maxCharacters;
        }
    }

    public static class EducationItem {
        private Long id;
        private String institutionName;
        private String degree;
        private Integer year;
        private String cgpaOrPercentage;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getInstitutionName() {
            return institutionName;
        }

        public void setInstitutionName(String institutionName) {
            this.institutionName = institutionName;
        }

        public String getDegree() {
            return degree;
        }

        public void setDegree(String degree) {
            this.degree = degree;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public String getCgpaOrPercentage() {
            return cgpaOrPercentage;
        }

        public void setCgpaOrPercentage(String cgpaOrPercentage) {
            this.cgpaOrPercentage = cgpaOrPercentage;
        }
    }

    public static class ResumeInfo {
        private boolean uploaded;
        private String fileName;
        private String contentType;
        private Long sizeBytes;
        private String previewUrl;

        public boolean isUploaded() {
            return uploaded;
        }

        public void setUploaded(boolean uploaded) {
            this.uploaded = uploaded;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Long getSizeBytes() {
            return sizeBytes;
        }

        public void setSizeBytes(Long sizeBytes) {
            this.sizeBytes = sizeBytes;
        }

        public String getPreviewUrl() {
            return previewUrl;
        }

        public void setPreviewUrl(String previewUrl) {
            this.previewUrl = previewUrl;
        }
    }

    public static class ProfileImageInfo {
        private boolean uploaded;
        private String previewUrl;
        private boolean visibleToHr;

        public boolean isUploaded() {
            return uploaded;
        }

        public void setUploaded(boolean uploaded) {
            this.uploaded = uploaded;
        }

        public String getPreviewUrl() {
            return previewUrl;
        }

        public void setPreviewUrl(String previewUrl) {
            this.previewUrl = previewUrl;
        }

        public boolean isVisibleToHr() {
            return visibleToHr;
        }

        public void setVisibleToHr(boolean visibleToHr) {
            this.visibleToHr = visibleToHr;
        }
    }

    public static class SkillItem {
        private Long id;
        private String skillName;
        private ProfileSkillCategory skillCategory;
        private ProfileSkillLevel skillLevel;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSkillName() {
            return skillName;
        }

        public void setSkillName(String skillName) {
            this.skillName = skillName;
        }

        public ProfileSkillCategory getSkillCategory() {
            return skillCategory;
        }

        public void setSkillCategory(ProfileSkillCategory skillCategory) {
            this.skillCategory = skillCategory;
        }

        public ProfileSkillLevel getSkillLevel() {
            return skillLevel;
        }

        public void setSkillLevel(ProfileSkillLevel skillLevel) {
            this.skillLevel = skillLevel;
        }
    }

    public static class PerformanceAnalytics {
        private double testScores;
        private double averageTimePerQuestion;
        private double accuracyPercentage;
        private String strengths;
        private String weaknesses;
        private String insightSummary;

        public double getTestScores() {
            return testScores;
        }

        public void setTestScores(double testScores) {
            this.testScores = testScores;
        }

        public double getAverageTimePerQuestion() {
            return averageTimePerQuestion;
        }

        public void setAverageTimePerQuestion(double averageTimePerQuestion) {
            this.averageTimePerQuestion = averageTimePerQuestion;
        }

        public double getAccuracyPercentage() {
            return accuracyPercentage;
        }

        public void setAccuracyPercentage(double accuracyPercentage) {
            this.accuracyPercentage = accuracyPercentage;
        }

        public String getStrengths() {
            return strengths;
        }

        public void setStrengths(String strengths) {
            this.strengths = strengths;
        }

        public String getWeaknesses() {
            return weaknesses;
        }

        public void setWeaknesses(String weaknesses) {
            this.weaknesses = weaknesses;
        }

        public String getInsightSummary() {
            return insightSummary;
        }

        public void setInsightSummary(String insightSummary) {
            this.insightSummary = insightSummary;
        }
    }

    public static class LearningInsights {
        private String recommendedLearningStrategy;
        private String weakAreas;
        private String suggestedTopics;

        public String getRecommendedLearningStrategy() {
            return recommendedLearningStrategy;
        }

        public void setRecommendedLearningStrategy(String recommendedLearningStrategy) {
            this.recommendedLearningStrategy = recommendedLearningStrategy;
        }

        public String getWeakAreas() {
            return weakAreas;
        }

        public void setWeakAreas(String weakAreas) {
            this.weakAreas = weakAreas;
        }

        public String getSuggestedTopics() {
            return suggestedTopics;
        }

        public void setSuggestedTopics(String suggestedTopics) {
            this.suggestedTopics = suggestedTopics;
        }
    }
}
