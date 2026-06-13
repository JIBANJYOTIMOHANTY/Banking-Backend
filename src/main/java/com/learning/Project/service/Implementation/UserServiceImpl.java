package com.learning.Project.service.Implementation;

import com.learning.Project.dto.RegisterRequest;
import com.learning.Project.model.User;
import com.learning.Project.repository.UserRepository;
import com.learning.Project.service.UserService;
import com.learning.Project.validation.UserValidator;
import com.learning.Project.exceptions.CustomerAccountExceptions;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        // 1. Validate password strength
        Optional<String> passwordValidationError = UserValidator.validatePassword(request.getPassword());
        if (passwordValidationError.isPresent()) {
            throw new CustomerAccountExceptions(passwordValidationError.get());
        }

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new CustomerAccountExceptions("Username is already taken");
        }

        // 2. Create and save User
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new CustomerAccountExceptions("First name is mandatory");
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new CustomerAccountExceptions("Last name is mandatory");
        }
        if (request.getProfileImage() == null || request.getProfileImage().isBlank()) {
            throw new CustomerAccountExceptions("Profile image is mandatory");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setProfileImage(request.getProfileImage());

        String role = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().toUpperCase()
                : "USER";
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        user.setRole(role);

        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
}
