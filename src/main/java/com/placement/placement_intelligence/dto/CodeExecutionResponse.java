package com.placement.placement_intelligence.dto;

public class CodeExecutionResponse {
    private String stdout;
    private String stderr;
    private Integer exitCode;
    private boolean success;
    private String message;

    public CodeExecutionResponse() {
    }

    public CodeExecutionResponse(String stdout, String stderr, Integer exitCode, boolean success, String message) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
        this.success = success;
        this.message = message;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
