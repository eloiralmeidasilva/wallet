package com.eloir.wallet.service;

import com.eloir.wallet.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void getUserId_ShouldReturnUser_WhenUsernameAndPasswordAreCorrect() {
        String username = "user";
        String password = "password";

        User user = userService.getUserId(username, password);

        assertNotNull(user);
        assertEquals("1", user.getUserId());
        assertEquals("user", user.getUserName());
        assertEquals("user.wallet@gmail.com", user.getEmail());
        assertEquals("USER", user.getRole());
    }

    @Test
    void getUserId_ShouldReturnNull_WhenUsernameIsCorrectButPasswordIsIncorrect() {
        String username = "user";
        String password = "wrongpassword";

        User user = userService.getUserId(username, password);

        assertNull(user);
    }

    @Test
    void getUserId_ShouldReturnNull_WhenUsernameIsIncorrectButPasswordIsCorrect() {
        String username = "wronguser";
        String password = "password";

        User user = userService.getUserId(username, password);

        assertNull(user);
    }

    @Test
    void getUserId_ShouldReturnNull_WhenUsernameAndPasswordAreIncorrect() {
        String username = "wronguser";
        String password = "wrongpassword";

        User user = userService.getUserId(username, password);

        assertNull(user);
    }
}

