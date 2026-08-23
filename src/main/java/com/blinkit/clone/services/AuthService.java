package com.blinkit.clone.services;

import com.blinkit.clone.dtos.request.LoginRequest;
import com.blinkit.clone.dtos.request.RegisterRequest;
import com.blinkit.clone.dtos.response.AuthResponse;
import com.blinkit.clone.entities.User;
import com.blinkit.clone.enums.Role;
import com.blinkit.clone.exceptions.InvalidCredentialsException;
import com.blinkit.clone.exceptions.UserAlreadyExistsException;
import com.blinkit.clone.repositories.UserRepository;
import com.blinkit.clone.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hash, never store raw
        user.setRole(Role.CUSTOMER);
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getId(), user.getName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Same error message as "user not found" above — don't leak which one was wrong
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getId(), user.getName(), user.getRole().name());
    }

    public void logout(String token) {
        long remainingMs = jwtUtil.getRemainingValidityMs(token);
        if (remainingMs > 0) {
            // Blacklist only until the token would've expired anyway — no need to store it forever
            redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofMillis(remainingMs));
        }
    }
}
