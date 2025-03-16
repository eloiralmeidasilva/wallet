package com.eloir.wallet.controller;

import com.eloir.wallet.config.security.JwtTokenProvider;
import com.eloir.wallet.model.User;
import com.eloir.wallet.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthController(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Map<String, String>> authenticate(@RequestParam String username, @RequestParam String password) {
        User user = userService.getUserId(username, password);

        if (user != null) {
            String token = jwtTokenProvider.generateToken(user.getUserId());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
    }
}
