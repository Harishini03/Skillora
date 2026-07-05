package com.placement.placement_intelligence.dto;

import com.placement.placement_intelligence.model.Role;

public class AuthResponse {
    private Long userId;
    private Long studentId;
    private Role role;
    private String name;
    private String email;
    private String token;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(Long userId, Long studentId, Role role, String name, String email, String token, String message) {
        this.userId = userId;
        this.studentId = studentId;
        this.role = role;
        this.name = name;
        this.email = email;
        this.token = token;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
