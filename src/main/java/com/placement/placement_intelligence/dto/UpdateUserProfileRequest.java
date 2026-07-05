package com.placement.placement_intelligence.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UpdateUserProfileRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email(message = "Invalid personal email")
    @Size(max = 160)
    private String personalEmail;

    @Email(message = "Invalid college email")
    @Size(max = 160)
    private String collegeEmail;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Alternate mobile number must be 10 digits")
    private String alternateMobileNumber;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "WhatsApp number must be 10 digits")
    private String whatsappNumber;

    private boolean visibleToHr = true;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 120)
    private String city;

    @Size(max = 120)
    private String state;

    @Pattern(regexp = "^$|^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    private LocalDate dateOfBirth;

    @Size(max = 20)
    private String gender;

    @Size(max = 150)
    private String fatherName;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Father contact number must be 10 digits")
    private String fatherContactNumber;

    @Size(max = 150)
    private String motherName;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Mother contact number must be 10 digits")
    private String motherContactNumber;

    @Size(max = 2000, message = "About section must be at most 2000 characters")
    private String aboutMe;

    private boolean profileImageVisibleToHr = true;

    @Valid
    private List<ProfileSkillDto> skills = new ArrayList<>();

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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

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

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public boolean isProfileImageVisibleToHr() {
        return profileImageVisibleToHr;
    }

    public void setProfileImageVisibleToHr(boolean profileImageVisibleToHr) {
        this.profileImageVisibleToHr = profileImageVisibleToHr;
    }

    public List<ProfileSkillDto> getSkills() {
        return skills;
    }

    public void setSkills(List<ProfileSkillDto> skills) {
        this.skills = skills;
    }
}
