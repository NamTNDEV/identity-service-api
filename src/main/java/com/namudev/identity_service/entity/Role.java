package com.namudev.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "roles")
public class Role {
    @Id
    String name;
    String description;

    @ManyToMany
            @JoinTable(
                    name = "roles_permissions",
                    joinColumns = @JoinColumn(name = "role_name"),
                    inverseJoinColumns = @JoinColumn(name = "permission_name")
            )
    Set<Permission> permissions;
}
