package com.mvc.service;

import com.mvc.entity.Order;
import com.mvc.entity.OrderStatus;
import com.mvc.entity.User;
import com.mvc.exception.DuplicateEmailException;
import com.mvc.exception.ResourceNotFoundException;
import com.mvc.repository.OrderRepository;
import com.mvc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private UserServiceImpl userService;

    User user = new User();
    Order order = new Order();
    Order newOrder = new Order();

    private static final long USER_ID = 1;
    private static final long NEW_USER_ID = 2;
    private static final long ORDER_ID = 1;
    private static final long NEW_ORDER_ID = 2;
    private static final long NON_USER_ID = 999;
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int USER_AGE = 25;
    private static final int ANOTHER_USER_AGE = 26;
    private static final int PAGE = 0;
    private static final int PAGE_SIZE = 10;

    private static final String USER_NOT_FOUND_MESSAGE = "не найден";
    private static final String DUPLICATE_EMAIL_MESSAGE = "уже используется";
    private static final String NAME_IVAN = "Иван";
    private static final String NAME_MASHA = "Маша";
    private static final String NEW_NAME_IVAN = "новый Иван";
    private static final String EMAIL_IVAN = "ivan@example.com";
    private static final String ORDER_PRODUCT_NAME = "Ноутбук";
    private static final String NEW_ORDER_PRODUCT_NAME = "Смартфон";
    private static final Integer ORDER_PRODUCT_PRICE = 50000;
    private static final Integer NEW_ORDER_PRODUCT_PRICE = 150000;

    @BeforeEach
    void setUp() {
        user.setId(USER_ID);
        user.setName(NAME_IVAN);
        user.setEmail(EMAIL_IVAN);
        user.setAge(USER_AGE);

        order.setId(ORDER_ID);
        order.setProductName(ORDER_PRODUCT_NAME);
        order.setQuantity(ONE);
        order.setPrice(new BigDecimal(ORDER_PRODUCT_PRICE));
        order.setStatus(OrderStatus.PENDING);

        newOrder.setId(NEW_ORDER_ID);
        newOrder.setProductName(NEW_ORDER_PRODUCT_NAME);
        newOrder.setQuantity(ONE);
        newOrder.setPrice(new BigDecimal(NEW_ORDER_PRODUCT_PRICE));
        newOrder.setStatus(OrderStatus.PENDING);

        user.setOrders(List.of(order));
    }

    @Nested
    @DisplayName("Позитивные сценарии")
    class PositiveTests {

        @Test
        @DisplayName("При создании пользователя с корректными данными, метод должен сохранить и вернуть пользователя")
        void createUser_WithValidData_ShouldSaveAndReturnUser() {
            User user = new User();
            user.setId(NEW_USER_ID);
            user.setName(NAME_MASHA);
            user.setEmail("masha@ali.ru");
            user.setAge(USER_AGE);

            when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(user);

            User result = userService.createUser(user);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(NEW_USER_ID);
            assertThat(result.getName()).isEqualTo(NAME_MASHA);
            verify(userRepository, times(ONE)).save(user);
        }

        @Test
        @DisplayName("При поиске пользователя с существующим id, метод должен вернуть пользователя")
        void findUserById_WhenUserExists_ShouldReturnUser() {

            when(userRepository.findByIdWithOrders(USER_ID)).thenReturn(Optional.of(user));

            User result = userService.findUserById(USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(NAME_IVAN);

            assertThat(result.getOrders()).isNotEmpty();
            assertThat(result.getOrders().get(ZERO).getProductName()).isEqualTo(ORDER_PRODUCT_NAME);
        }

        @Test
        @DisplayName("При выводе всех пользователей, метод должен вернуть страницу с пользователями")
        void findAllUsers_ShouldReturnPageOfUsers() {

            User userTo = new User();
            userTo.setName(NAME_MASHA);
            userTo.setEmail("masha@masha.com");
            userTo.setAge(USER_AGE);

            Pageable pageable = PageRequest.of(PAGE, PAGE_SIZE);
            List<User> users = List.of(user, userTo);
            Page<User> pageUsers = new PageImpl<>(users, pageable, users.size());

            when(userRepository.findAll(pageable)).thenReturn(pageUsers);
            Page<User> result = userService.findAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(TWO);
            assertThat(result.getContent().get(ZERO).getName()).isEqualTo(NAME_IVAN);
            assertThat(result.getContent().get(ONE).getName()).isEqualTo(NAME_MASHA);
            verify(userRepository, times(ONE)).findAll(pageable);
        }

        @Test
        @DisplayName("При обновлении пользователя с корректными данными, метод должен вернуть пользователя")
        void updateUser_WithValidData_ShouldSaveAndReturnUser(){

            User updatedDetails = new User();
            updatedDetails.setId(USER_ID);
            updatedDetails.setName(NEW_NAME_IVAN);
            updatedDetails.setEmail(EMAIL_IVAN);
            updatedDetails.setAge(ANOTHER_USER_AGE);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(updatedDetails);

            User result = userService.updateUser(USER_ID, updatedDetails);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(NEW_NAME_IVAN);
            assertThat(result.getAge()).isEqualTo(ANOTHER_USER_AGE);
            verify(userRepository, times(ONE)).save(any(User.class));
        }

        @Test
        @DisplayName("При удалении пользователя с существующим id, метод должен удалить пользователя")
        void deleteUser_WithExistingId_ShouldDeleteUser(){

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            doNothing().when(userRepository).delete(user);

            userService.deleteUser(USER_ID);

            verify(userRepository, times(ONE)).findById(USER_ID);
            verify(userRepository, times(ONE)).delete(user);
        }

        @Test
        @DisplayName("При добавлении корректного заказа, метод должен вернуть заказ")
        void addOrderToUser_WithValidData_ShouldReturnOrder(){

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(orderRepository.save(any(Order.class))).thenReturn(newOrder);

            Order result = userService.addOrderToUser(USER_ID, newOrder);

            assertThat(result).isNotNull();
            assertThat(result.getProductName()).isEqualTo(NEW_ORDER_PRODUCT_NAME);
            assertThat(result.getPrice()).isEqualTo(new BigDecimal(NEW_ORDER_PRODUCT_PRICE));
            assertThat(result.getUser().getId()).isEqualTo(USER_ID);
            verify(orderRepository, times(ONE)).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("негативные сценарии")
    class NegativeTests {

        @Test
        @DisplayName("При создании пользователя с уже существующим email, должно выброситься исключение")
        void createUser_WithExistingEmail_ShouldThrowDuplicateEmailException() {

            when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(user))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining(DUPLICATE_EMAIL_MESSAGE);
        }

        @Test
        @DisplayName("При поиске пользователя с несуществующим id, должно выброситься исключение")
        void findUserById_WhenUserNotExists_ShouldThrowResourceNotFoundException() {
            when(userRepository.findByIdWithOrders(NON_USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findUserById(NON_USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(USER_NOT_FOUND_MESSAGE);
        }

        @Test
        @DisplayName("При обновлении пользователя с несуществующим id, должно выброситься исключение")
        void updateUser_WhenUserNotExists_ShouldThrowResourceNotFoundException() {

            when(userRepository.findById(NON_USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(NON_USER_ID, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(USER_NOT_FOUND_MESSAGE);
        }

        @Test
        @DisplayName("При обновлении пользователя на email, который уже принадлежит другому пользователю, должно выброситься исключение")
        void updateUser_WithEmailAlreadyExists_ShouldThrowDuplicateEmailException() {

            User verifiableUser = new User();
            verifiableUser.setId(NEW_USER_ID);
            verifiableUser.setEmail("ivan@ivan.com");

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.findByEmail(verifiableUser.getEmail())).thenReturn(Optional.of(verifiableUser));

            assertThatThrownBy(() -> userService.updateUser(USER_ID, verifiableUser))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining(DUPLICATE_EMAIL_MESSAGE);
        }

        @Test
        @DisplayName("При удалении пользователя с несуществующим id, должно выброситься исключение")
        void deleteUser_WhenUserNotExists_ShouldThrowResourceNotFoundException() {
            when(userRepository.findById(NON_USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(NON_USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(USER_NOT_FOUND_MESSAGE);
        }

        @Test
        @DisplayName("При добавлении заказа для несуществующего пользователя, должно выброситься исключение")
        void addOrderToUser_WhenUserNotExists_ShouldThrowResourceNotFoundException() {
            when(userRepository.findById(NON_USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.addOrderToUser(NON_USER_ID, newOrder))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(USER_NOT_FOUND_MESSAGE);
        }

        @Test
        @DisplayName("При поиске заказа с несуществующим id, должно выброситься исключение")
        void findOrderById_WhenOrderNotExists_ShouldThrowResourceNotFoundException() {
            when(orderRepository.findById(NON_USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findOrderById(NON_USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(USER_NOT_FOUND_MESSAGE);
        }
    }
}
