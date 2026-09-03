package net.rajaonson.room_management_system.auth.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import net.rajaonson.room_management_system.auth.service.AuthService;
import net.rajaonson.room_management_system.auth.service.dto.LoginDto;
import net.rajaonson.room_management_system.auth.service.dto.TokenResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @SecurityRequirements
    @PostMapping("/login")
    public TokenResponseDto login(@Valid @RequestBody LoginDto loginDto) {
        return authService.authenticate(loginDto);
    }
}
