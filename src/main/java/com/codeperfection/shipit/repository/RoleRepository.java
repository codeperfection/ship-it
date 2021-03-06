package com.codeperfection.shipit.repository;

import com.codeperfection.shipit.entity.Role;
import com.codeperfection.shipit.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName roleName);
}
