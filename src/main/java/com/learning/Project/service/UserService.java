package com.learning.Project.service;

import com.learning.Project.dto.RegisterRequest;
import com.learning.Project.model.User;

public interface UserService {
    User registerUser(RegisterRequest request);
    User findByUsername(String username);
}
