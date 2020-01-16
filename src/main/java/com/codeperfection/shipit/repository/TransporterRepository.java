package com.codeperfection.shipit.repository;

import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransporterRepository extends JpaRepository<Transporter, UUID> {

    Page<Transporter> findByUserAndIsActiveTrue(User user, Pageable pageable);

    Optional<Transporter> findByUuidAndUser(UUID uuid, User user);
}
