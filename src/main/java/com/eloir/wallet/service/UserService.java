package com.eloir.wallet.service;

import com.eloir.wallet.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public User getUserId(String username, String password) {
        if ("user".equals(username) && "password".equals(password)) {
            User user = new User();
            user.setUserId("1");
            user.setUserName("user");
            user.setEmail("user.wallet@gmail.com");
            user.setRole("USER");
            return user;
        }
        else if ("user2".equals(username) && "password2".equals(password)) {
            User user = new User();
            user.setUserId("2");
            user.setUserName("user2");
            user.setEmail("user2.wallet@gmail.com");
            user.setRole("USER");
            return user;
        }
        return null;
    }
}
