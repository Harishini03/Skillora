package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.model.PortalNotification;
import com.placement.placement_intelligence.model.User;
import com.placement.placement_intelligence.repository.PortalNotificationRepository;
import com.placement.placement_intelligence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private PortalNotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserService currentUserService;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository, currentUserService);
    }

    @Test
    void createJobApplicationNotification_savesCorrectType() {
        User user = new User(); user.setId(1L); user.setEmail("s@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        PortalNotification saved = new PortalNotification();
        saved.setNotificationType("JOB_APPLICATION");
        saved.setMessage("Your application for \"Software Engineer\" has been submitted.");
        when(notificationRepository.save(any())).thenReturn(saved);

        PortalNotification result = notificationService.createJobApplicationNotification(1L, "Software Engineer");

        assertEquals("JOB_APPLICATION", result.getNotificationType());
        assertTrue(result.getMessage().contains("Software Engineer"));
    }

    @Test
    void createInterviewNotification_savesCorrectType() {
        User user = new User(); user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        PortalNotification saved = new PortalNotification();
        saved.setNotificationType("INTERVIEW_SCHEDULED");
        saved.setMessage("Interview scheduled with TechCorp on 2025-01-15.");
        when(notificationRepository.save(any())).thenReturn(saved);

        PortalNotification result = notificationService.createInterviewNotification(1L, "TechCorp", "2025-01-15");

        assertEquals("INTERVIEW_SCHEDULED", result.getNotificationType());
        assertTrue(result.getMessage().contains("TechCorp"));
    }

    @Test
    void getUnreadCount_returnsRepositoryValue() {
        User currentUser = new User(); currentUser.setId(5L);
        when(currentUserService.currentUser()).thenReturn(currentUser);
        when(notificationRepository.countByUser_IdAndReadFalse(5L)).thenReturn(3L);

        long count = notificationService.getUnreadCountForCurrentUser();

        assertEquals(3L, count);
    }

    @Test
    void markAsRead_wrongUser_throwsException() {
        User owner = new User(); owner.setId(10L);
        User currentUser = new User(); currentUser.setId(99L); // different user
        PortalNotification notification = new PortalNotification();
        notification.setId(1L);
        notification.setUser(owner);

        when(currentUserService.currentUser()).thenReturn(currentUser);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(IllegalArgumentException.class, () -> notificationService.markAsRead(1L));
    }

    @Test
    void markAsRead_correctUser_setsReadTrue() {
        User owner = new User(); owner.setId(7L);
        PortalNotification notification = new PortalNotification();
        notification.setId(2L);
        notification.setUser(owner);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(currentUserService.currentUser()).thenReturn(owner);
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAsRead(2L);

        verify(notificationRepository).save(argThat(n -> Boolean.TRUE.equals(n.getRead())));
    }

    @Test
    void createStatusUpdateNotification_savesCorrectType() {
        User user = new User(); user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        PortalNotification saved = new PortalNotification();
        saved.setNotificationType("STATUS_UPDATE");
        saved.setMessage("Your application for \"Backend Dev\" status updated to: SHORTLISTED.");
        when(notificationRepository.save(any())).thenReturn(saved);

        PortalNotification result = notificationService.createStatusUpdateNotification(1L, "Backend Dev", "SHORTLISTED");

        assertEquals("STATUS_UPDATE", result.getNotificationType());
        assertTrue(result.getMessage().contains("SHORTLISTED"));
    }
}
