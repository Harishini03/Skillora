package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.Role;
import com.placement.placement_intelligence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("select count(u) from User u where u.active = true")
    long countActiveUsers();

    long countByLastLoginAtAfter(LocalDateTime time);

    List<User> findByRoleOrderByNameAsc(Role role);
}
