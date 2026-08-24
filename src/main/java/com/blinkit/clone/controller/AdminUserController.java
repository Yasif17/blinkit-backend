package com.blinkit.clone.controller;

import com.blinkit.clone.dtos.response.UserSummaryDto;
import com.blinkit.clone.enums.Role;
import com.blinkit.clone.services.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(@PathVariable Long id, @RequestParam Role role) {
        adminUserService.updateRole(id, role);
        return ResponseEntity.noContent().build();
    }
}