package com.mvc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.mvc.dto.Views;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @JsonView(Views.UserDetails.class)
    private String productName;

    @NotNull
    @Positive
    @JsonView(Views.UserDetails.class)
    private Integer quantity;

    @NotNull
    @Positive
    @Column(nullable=false, precision=10, scale=2)
    @JsonView(Views.UserDetails.class)
    private BigDecimal price;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonView(Views.UserDetails.class)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
