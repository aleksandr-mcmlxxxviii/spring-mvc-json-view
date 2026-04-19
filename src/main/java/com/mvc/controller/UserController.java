package com.mvc.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.mvc.dto.PageResponse;
import com.mvc.dto.Views;
import com.mvc.entity.Order;
import com.mvc.entity.User;
import com.mvc.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final static int PAGE = 0;
    private final static int PAGE_SIZE = 10;

    @GetMapping
    @JsonView(Views.UserSummary.class)
    public ResponseEntity<PageResponse<User>> getAllUsers(@PageableDefault(page = PAGE, size = PAGE_SIZE) Pageable pageable) {
        Page<User> userPage = userService.findAllUsers(pageable);
        PageResponse<User> response = new PageResponse<>(userPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @JsonView(Views.UserDetails.class)
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @JsonView(Views.UserSummary.class)
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        System.out.println(user);
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    @JsonView(Views.UserSummary.class)
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<Order> addOrderToUser(
            @PathVariable Long userId,
            @Valid @RequestBody Order order) {
        Order createdOrder = userService.addOrderToUser(userId, order);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }
}
