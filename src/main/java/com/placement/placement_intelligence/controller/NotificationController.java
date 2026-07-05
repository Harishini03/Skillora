package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.model.PortalNotification;
import com.placement.placement_intelligence.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        List<NotificationDto> dtos = notificationService.getNotificationsForCurrentUser()
                .stream()
                .map(NotificationController::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCountForCurrentUser();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @DeleteMapping("/read")
    public ResponseEntity<Map<String, String>> deleteReadNotifications() {
        notificationService.deleteReadNotifications();
        return ResponseEntity.ok(Map.of("message", "Read notifications deleted"));
    }

    @PostMapping("/send-to-all")
    @PreAuthorize("hasAnyRole('STAFF','RECRUITER')")
    public ResponseEntity<Map<String, String>> sendToAll(@RequestBody BroadcastRequest request) {
        if (request.type() == null || request.type().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "type is required"));
        }
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "message is required"));
        }
        notificationService.broadcastToAllUsers(request.type(), request.message());
        return ResponseEntity.ok(Map.of("message", "Broadcast sent to all users"));
    }

    private static NotificationDto toDto(PortalNotification n) {
        return new NotificationDto(
                n.getId(),
                n.getNotificationType(),
                n.getMessage(),
                Boolean.TRUE.equals(n.getRead()),
                n.getCreatedAt()
        );
    }

    public record NotificationDto(
            Long id,
            String type,
            String message,
            boolean read,
            LocalDateTime createdAt
    ) {}

    public record BroadcastRequest(
            String type,
            String message
    ) {}
}
