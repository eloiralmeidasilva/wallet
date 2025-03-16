package com.eloir.wallet.controller;

import com.eloir.wallet.config.security.JwtTokenProvider;
import com.eloir.wallet.model.User;
import com.eloir.wallet.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId("user123");
    }

    @Test
    void authenticate_ShouldReturnToken_WhenUserExists() {
        when(userService.getUserId("user123", "password")).thenReturn(user);
        when(jwtTokenProvider.generateToken("user123")).thenReturn("token123");

        ResponseEntity<Map<String, String>> response = authController.authenticate("user123", "password");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("token"));
        assertEquals("token123", response.getBody().get("token"));
    }

    @Test
    void authenticate_ShouldReturnUnauthorized_WhenUserDoesNotExist() {
        when(userService.getUserId("user123", "password")).thenReturn(null);

        ResponseEntity<Map<String, String>> response = authController.authenticate("user123", "password");

        assertEquals(401, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("message"));
        assertEquals("Unauthorized", response.getBody().get("message"));
    }
}
