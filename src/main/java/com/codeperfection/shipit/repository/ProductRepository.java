package com.codeperfection.shipit.repository;

import com.codeperfection.shipit.entity.Product;
import com.codeperfection.shipit.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByUserAndIsActiveTrue(User user);

    Page<Product> findByUserAndIsActiveTrue(User user, Pageable pageable);

    Optional<Product> findByUuidAndUser(UUID uuid, User user);
}
