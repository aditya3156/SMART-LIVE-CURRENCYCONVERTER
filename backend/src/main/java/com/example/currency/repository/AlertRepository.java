package com.example.currency.repository;

import com.example.currency.model.User;
import com.example.currency.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserAndEnabledTrue(User user);
    List<Alert> findByEnabledTrue();
    Optional<Alert> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
}