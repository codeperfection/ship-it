package com.codeperfection.shipit.repository;

import com.codeperfection.shipit.entity.Shipping;
import com.codeperfection.shipit.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShippingRepository extends JpaRepository<Shipping, UUID> {

    Page<Shipping> findByUser(User user, Pageable pageable);

    Optional<Shipping> findByUuidAndUser(UUID uuid, User user);
}
