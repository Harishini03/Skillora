package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.PortalNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PortalNotificationRepository extends JpaRepository<PortalNotification, Long> {
    List<PortalNotification> findTop20ByUser_IdOrderByCreatedAtDesc(Long userId);

    List<PortalNotification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndReadFalse(Long userId);

    @Transactional
    void deleteByUser_IdAndReadTrue(Long userId);
}
