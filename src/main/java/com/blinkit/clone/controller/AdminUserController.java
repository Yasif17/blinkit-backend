package com.blinkit.clone.controller;

import com.blinkit.clone.entities.User;
import com.blinkit.clone.enums.Role;
import com.blinkit.clone.exceptions.UserNotFoundException;
import com.blinkit.clone.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(@PathVariable Long id, @RequestParam Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setRole(role);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}