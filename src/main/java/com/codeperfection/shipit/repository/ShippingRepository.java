package com.codeperfection.shipit.repository;

import com.codeperfection.shipit.entity.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShippingRepository extends JpaRepository<Shipping, UUID> {
}
