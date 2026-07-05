package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.model.PortalNotification;
import com.placement.placement_intelligence.model.User;
import com.placement.placement_intelligence.repository.PortalNotificationRepository;
import com.placement.placement_intelligence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final PortalNotificationRepository portalNotificationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public NotificationService(PortalNotificationRepository portalNotificationRepository,
                               UserRepository userRepository,
                               CurrentUserService currentUserService) {
        this.portalNotificationRepository = portalNotificationRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public PortalNotification createNotification(Long userId, String type, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        PortalNotification notification = new PortalNotification();
        notification.setUser(user);
        notification.setNotificationType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return portalNotificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<PortalNotification> getNotificationsForCurrentUser() {
        Long userId = currentUserService.currentUser().getId();
        return portalNotificationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(20)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCountForCurrentUser() {
        Long userId = currentUserService.currentUser().getId();
        return portalNotificationRepository.countByUser_IdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        User user = currentUserService.currentUser();
        PortalNotification notification = portalNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized notification access");
        }
        notification.setRead(true);
        portalNotificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        Long userId = currentUserService.currentUser().getId();
        List<PortalNotification> unread = portalNotificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> !Boolean.TRUE.equals(n.getRead()))
                .toList();
        for (PortalNotification n : unread) {
            n.setRead(true);
        }
        portalNotificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteReadNotifications() {
        Long userId = currentUserService.currentUser().getId();
        portalNotificationRepository.deleteByUser_IdAndReadTrue(userId);
    }

    @Transactional
    public PortalNotification createJobApplicationNotification(Long userId, String jobTitle) {
        return createNotification(userId, "JOB_APPLICATION",
                "Your application for \"" + jobTitle + "\" has been submitted.");
    }

    @Transactional
    public PortalNotification createInterviewNotification(Long userId, String companyName, String scheduledAt) {
        return createNotification(userId, "INTERVIEW_SCHEDULED",
                "Interview scheduled with " + companyName + " on " + scheduledAt + ".");
    }

    @Transactional
    public PortalNotification createStatusUpdateNotification(Long userId, String jobTitle, String status) {
        return createNotification(userId, "STATUS_UPDATE",
                "Your application for \"" + jobTitle + "\" status updated to: " + status + ".");
    }

    @Transactional
    public PortalNotification createNewJobNotification(Long userId, String jobTitle, String company) {
        return createNotification(userId, "NEW_JOB",
                "New job posted: \"" + jobTitle + "\"" + (company != null && !company.isBlank() ? " at " + company : "") + ". Apply now!");
    }

    @Transactional
    public void broadcastToAllUsers(String type, String message) {
        userRepository.findAll().forEach(user -> {
            try {
                createNotification(user.getId(), type, message);
            } catch (Exception ex) {
                // skip users that cannot receive notifications
            }
        });
    }
}
