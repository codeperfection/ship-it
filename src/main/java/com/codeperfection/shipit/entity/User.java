package com.codeperfection.shipit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "auth_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private UUID uuid;

    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    private OffsetDateTime passwordChangeDate;

    @NotNull
    private String email;

    @NotNull
    private String name;

    @NotNull
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @ManyToMany
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_uuid"),
            inverseJoinColumns = @JoinColumn(name = "role_uuid"))
    private Set<Role> roles;
}
