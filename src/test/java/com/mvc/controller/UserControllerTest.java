package com.mvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvc.entity.Order;
import com.mvc.entity.OrderStatus;
import com.mvc.entity.User;
import com.mvc.exception.GlobalExceptionHandler;
import com.mvc.exception.ResourceNotFoundException;
import com.mvc.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Контроллер пользователей (модульные тесты)")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User user;
    private User anotherUser;
    Order order = new Order();

    private static final long USER_ID = 1;
    private static final long NEW_USER_ID = 2;
    private static final long NON_USER_ID = 999;
    private static final long ORDER_ID = 1;
    private static final int ONE = 1;
    private static final int USER_AGE = 25;
    private static final int ANOTHER_USER_AGE = 26;
    private static final int PAGE = 0;
    private static final int PAGE_SIZE = 10;

    private static final String NAME_IVAN = "Иван";
    private static final String NAME_MASHA = "Маша";
    private static final String EMAIL_IVAN = "ivan@example.com";
    private static final String EMAIL_MASHA = "maria@example.com";
    private static final String ORDER_PRODUCT_NAME = "Ноутбук";
    private static final Integer ORDER_PRODUCT_PRICE = 50000;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        order.setId(ORDER_ID);
        order.setProductName(ORDER_PRODUCT_NAME);
        order.setQuantity(ONE);
        order.setPrice(new BigDecimal(ORDER_PRODUCT_PRICE));
        order.setStatus(OrderStatus.PENDING);

        user = new User();
        user.setId(USER_ID);
        user.setName(NAME_IVAN);
        user.setEmail(EMAIL_IVAN);
        user.setAge(USER_AGE);
        user.setOrders(List.of(order));

        anotherUser = new User();
        anotherUser.setId(NEW_USER_ID);
        anotherUser.setName(NAME_MASHA);
        anotherUser.setEmail(EMAIL_MASHA);
        anotherUser.setAge(ANOTHER_USER_AGE);
        anotherUser.setOrders(List.of());
    }

    @Test
    @DisplayName("GET /api/users - должен вернуть пользователей БЕЗ заказов (UserSummary)")
    void getAllUsers_ShouldReturnUsersWithoutOrders() throws Exception {

        List<User> users = List.of(user, anotherUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(PAGE, PAGE_SIZE), users.size());

        when(userService.findAllUsers(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(USER_ID))
                .andExpect(jsonPath("$.content[0].name").value(NAME_IVAN))
                .andExpect(jsonPath("$.content[0].age").doesNotExist())
                .andExpect(jsonPath("$.content[0].orders").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/users/{id} - должен вернуть пользователя c заказами (UserDetails)")
    void getUserById_ShouldReturnUserWithOrders() throws Exception {

        when(userService.findUserById(USER_ID)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(NAME_IVAN))
                .andExpect(jsonPath("$.age").doesNotExist())
                .andExpect(jsonPath("$.orders").exists())
                .andExpect(jsonPath("$.orders[0].productName").value(ORDER_PRODUCT_NAME));
    }

    @Test
    @DisplayName("GET /api/users/{id} - при несуществующем id должен вернуть 404")
    void getUserById_WhenUserNotExists_ShouldReturnNotFound() throws Exception {

        when(userService.findUserById(NON_USER_ID)).thenThrow(new ResourceNotFoundException("Пользователь", NON_USER_ID));

        mockMvc.perform(get("/api/users/{id}", NON_USER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/users - должен создать пользователя и вернуть БЕЗ заказов")
    void createUser_ShouldReturnCreatedUserWithoutOrders() throws Exception {

        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.age").doesNotExist())
                .andExpect(jsonPath("$.orders").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - должен обновить пользователя и вернуть БЕЗ заказов")
    void updateUser_ShouldReturnUpdatedUserWithoutOrders() throws Exception {

        when(userService.updateUser(eq(USER_ID), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(NAME_IVAN))
                .andExpect(jsonPath("$.age").doesNotExist())
                .andExpect(jsonPath("$.orders").doesNotExist());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - должен удалить пользователя")
    void deleteUser_ShouldReturnNoContent() throws Exception {

        doNothing().when(userService).deleteUser(USER_ID);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/users/{userId}/orders - должен добавить заказ")
    void addOrderToUser_ShouldReturnCreatedOrder() throws Exception {

        when(userService.addOrderToUser(eq(USER_ID), any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/api/users/{userId}/orders", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.productName").value(ORDER_PRODUCT_NAME));
    }
}
