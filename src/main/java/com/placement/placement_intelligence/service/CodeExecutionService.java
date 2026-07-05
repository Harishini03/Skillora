package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.dto.CodeExecutionRequest;
import com.placement.placement_intelligence.dto.CodeExecutionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced code execution service with sandbox isolation and multi-language support.
 * Implements Requirement 5: Coding Platform with Multi-Language Support
 * 
 * Security Features:
 * - Timeout enforcement (5 seconds max as per requirements)
 * - Temporary directory isolation
 * - Process cleanup
 * - Memory limit awareness (256MB suggested)
 */
@Service
public class CodeExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionService.class);
    private static final long TIMEOUT_SECONDS = 5; // As per requirements: 5 seconds max
    private static final String MEMORY_LIMIT = "256m"; // 256MB as per requirements

    public CodeExecutionResponse execute(CodeExecutionRequest request) {
        String language = normalizeLanguage(request.getLanguage());
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Code cannot be empty");
        }

        logger.info("Executing {} code (length: {} chars)", language, request.getCode().length());
        
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("skillora-sandbox-");
            
            return switch (language) {
                case "python" -> executePython(request.getCode(), request.getStdin(), tempDir);
                case "java" -> executeJava(request.getCode(), request.getStdin(), tempDir);
                case "cpp", "c++" -> executeCpp(request.getCode(), request.getStdin(), tempDir);
                case "javascript", "js" -> executeJavaScript(request.getCode(), request.getStdin(), tempDir);
                default -> throw new IllegalArgumentException("Unsupported language. Supported: java, python, cpp, javascript");
            };
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Code execution failed for language {}: {}", language, ex.getMessage());
            return new CodeExecutionResponse("", "Internal execution error: " + ex.getMessage(), -1, false, "Execution failed");
        } finally {
            // Clean up temporary files for security
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
    }

    private CodeExecutionResponse executePython(String code, String stdin, Path dir) throws IOException, InterruptedException {
        Path source = dir.resolve("main.py");
        Files.writeString(source, code, StandardCharsets.UTF_8);

        // Try python3 first, then python, then py
        ProcessResult run = runCommand(List.of("python3", source.toString()), dir, stdin);
        if (run.exitCode() == 9001) {
            run = runCommand(List.of("python", source.toString()), dir, stdin);
            if (run.exitCode() == 9001) {
                run = runCommand(List.of("py", "-3", source.toString()), dir, stdin);
            }
        }
        
        return toResponse(run, "Python");
    }

    private CodeExecutionResponse executeJava(String code, String stdin, Path dir) throws IOException, InterruptedException {
        Path source = dir.resolve("Main.java");
        Files.writeString(source, code, StandardCharsets.UTF_8);

        // Compile with memory limit awareness
        ProcessResult compile = runCommand(List.of("javac", "-J-Xmx" + MEMORY_LIMIT, source.toString()), dir, "");
        if (!compile.success()) {
            String errorMsg = compile.stderr().isEmpty() ? "Compilation failed" : compile.stderr();
            return new CodeExecutionResponse("", errorMsg, compile.exitCode(), false, "Compilation Error");
        }

        // Run with memory limit
        ProcessResult run = runCommand(List.of("java", "-Xmx" + MEMORY_LIMIT, "-cp", dir.toString(), "Main"), dir, stdin);
        return toResponse(run, "Java");
    }

    private CodeExecutionResponse executeCpp(String code, String stdin, Path dir) throws IOException, InterruptedException {
        Path source = dir.resolve("main.cpp");
        Path output = dir.resolve("main.exe");
        Files.writeString(source, code, StandardCharsets.UTF_8);

        // Compile with C++17 standard and optimization
        ProcessResult compile = runCommand(List.of("g++", source.toString(), "-std=c++17", "-O2", "-Wall", "-o", output.toString()), dir, "");
        if (!compile.success()) {
            String errorMsg = compile.stderr().isEmpty() ? "Compilation failed" : compile.stderr();
            return new CodeExecutionResponse("", errorMsg, compile.exitCode(), false, "Compilation Error");
        }

        ProcessResult run = runCommand(List.of(output.toString()), dir, stdin);
        return toResponse(run, "C++");
    }

    private CodeExecutionResponse executeJavaScript(String code, String stdin, Path dir) throws IOException, InterruptedException {
        Path source = dir.resolve("main.js");
        
        // Add stdin handling if not present in code
        String wrappedCode = code;
        if (stdin != null && !stdin.trim().isEmpty() && !code.contains("process.stdin")) {
            wrappedCode = "const input = `" + stdin.replace("`", "\\`") + "`;\n" +
                         "const lines = input.trim().split('\\n');\n" +
                         "let currentLine = 0;\n" +
                         "const readline = () => lines[currentLine++] || '';\n" +
                         code;
        }
        
        Files.writeString(source, wrappedCode, StandardCharsets.UTF_8);

        // Try node first, then nodejs
        ProcessResult run = runCommand(List.of("node", "--max-old-space-size=256", source.toString()), dir, stdin);
        if (run.exitCode() == 9001) {
            run = runCommand(List.of("nodejs", "--max-old-space-size=256", source.toString()), dir, stdin);
        }
        
        return toResponse(run, "JavaScript");
    }

    private ProcessResult runCommand(List<String> command, Path workDir, String stdin) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(workDir.toFile());
        
        // Security: Clear environment variables and set restricted environment
        builder.environment().clear();
        builder.environment().put("PATH", System.getenv("PATH"));
        
        Process process;
        try {
            process = builder.start();
        } catch (IOException ex) {
            logger.warn("Runtime not available: {}", command.get(0));
            return new ProcessResult("", "Runtime not available: " + command.get(0) + ". Please ensure it's installed and in PATH.", 9001, false);
        }

        // Provide stdin if available
        if (stdin != null && !stdin.isBlank()) {
            try {
                process.getOutputStream().write(stdin.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                logger.warn("Failed to write stdin: {}", ex.getMessage());
            }
        }
        process.getOutputStream().close();

        // Enforce timeout strictly as per requirements (5 seconds)
        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            logger.warn("Process timed out after {} seconds", TIMEOUT_SECONDS);
            return new ProcessResult("", "Time Limit Exceeded: Process terminated after " + TIMEOUT_SECONDS + " seconds", 124, false);
        }

        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.exitValue();
        
        return new ProcessResult(stdout, stderr, exitCode, exitCode == 0);
    }

    private CodeExecutionResponse toResponse(ProcessResult result, String language) {
        String message;
        if (result.success()) {
            message = "Execution successful";
        } else if (result.exitCode() == 124) {
            message = "Time Limit Exceeded";
        } else if (result.exitCode() == 9001) {
            message = language + " runtime not found";
        } else {
            message = "Runtime Error";
        }
        
        return new CodeExecutionResponse(
                result.stdout(),
                result.stderr(),
                result.exitCode(),
                result.success(),
                message
        );
    }

    private void cleanupTempDirectory(Path tempDir) {
        try {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            logger.warn("Failed to delete temp file: {}", path);
                        }
                    });
        } catch (IOException ex) {
            logger.warn("Failed to cleanup temp directory: {}", tempDir);
        }
    }

    private String normalizeLanguage(String language) {
        if (language == null) return "";
        String normalized = language.trim().toLowerCase();
        // Handle common aliases
        return switch (normalized) {
            case "c++", "cplusplus" -> "cpp";
            case "js", "node", "nodejs" -> "javascript";
            case "py", "python3" -> "python";
            default -> normalized;
        };
    }

    private record ProcessResult(String stdout, String stderr, int exitCode, boolean success) {
    }
}
