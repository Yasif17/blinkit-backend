package com.blinkit.clone.services;

import com.blinkit.clone.dtos.response.UserSummaryDto;
import com.blinkit.clone.entities.User;
import com.blinkit.clone.enums.Role;
import com.blinkit.clone.exceptions.UserNotFoundException;
import com.blinkit.clone.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummaryDto(u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.getCreatedAt()))
                .toList();
    }

    public void updateRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }
}