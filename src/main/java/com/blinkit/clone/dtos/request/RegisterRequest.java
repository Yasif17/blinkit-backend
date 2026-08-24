package com.blinkit.clone.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {


    @NotBlank(message = "Name cannot be empty")
    private String name;


    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;


    @NotBlank(message = "password is required")
    @Size(min = 8,message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$",
            message = "Password must contain at least one uppercase letter and one digit"
    )
    private String password;



}
