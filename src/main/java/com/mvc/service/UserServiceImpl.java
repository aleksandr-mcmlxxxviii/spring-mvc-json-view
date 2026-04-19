package com.mvc.service;

import com.mvc.entity.Order;
import com.mvc.entity.User;
import com.mvc.exception.DuplicateEmailException;
import com.mvc.exception.ResourceNotFoundException;
import com.mvc.repository.OrderRepository;
import com.mvc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private static final String USER_NOT_FOUND = "Пользователь";

    @Override
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findByIdWithOrders(id).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND, id));
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        String email = userDetails.getEmail();

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND, id));

        if (!existingUser.getEmail().equals(email)) {
            userRepository.findByEmail(email)
                    .ifPresent(user -> { throw new DuplicateEmailException(email); });
        }
        existingUser.setName(userDetails.getName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setAge(userDetails.getAge());
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND, id));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public Order addOrderToUser(Long userId, Order order) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND, userId));
        order.setUser(user);
        return orderRepository.save(order);
    }

    @Override
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Заказ", orderId));
    }
}
