package com.mvc.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.mvc.dto.Views;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.UserSummary.class)
    private Long id;

    @NotBlank
    @JsonView(Views.UserSummary.class)
    private String name;

    @Email
    @NotBlank
    @Column(unique = true)
    @JsonView(Views.UserSummary.class)
    private String email;

    @NotNull
    @Positive
    private Integer age;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonView(Views.UserDetails.class)
    private List<Order> orders = new ArrayList<>();
}
