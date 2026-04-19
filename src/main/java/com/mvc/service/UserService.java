package com.mvc.service;

import com.mvc.entity.Order;
import com.mvc.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<User> findAllUsers(Pageable pageable);
    User findUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    Order addOrderToUser(Long userId, Order order);
    Order findOrderById(Long orderId);
}
