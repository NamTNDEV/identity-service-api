package com.namudev.identity_service.entity;

import com.namudev.identity_service.validator.DobConstraint;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor()
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String username;

    String password;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    @NotNull
    LocalDate dob;

    @ManyToMany
    Set<Role> roles;
}
