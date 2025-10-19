package com.IBCSolutions.TransferApp.controller;

import com.IBCSolutions.TransferApp.model.dto.LoginRequest;
import com.IBCSolutions.TransferApp.model.dto.LoginResponse;
import com.IBCSolutions.TransferApp.model.dto.RegisterRequest;
import com.IBCSolutions.TransferApp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private static final String ERROR_PREFIX = "ERROR: ";

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest registerRequest) {
        String result = authService.registerNewUser(registerRequest);

        if (result.startsWith(ERROR_PREFIX)) {
            HttpStatus status = result.contains("server error") ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(result.substring(ERROR_PREFIX.length()));
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}