package com.IBCSolutions.TransferApp.controller;

import com.IBCSolutions.TransferApp.model.entity.Account;
import com.IBCSolutions.TransferApp.model.entity.User;
import com.IBCSolutions.TransferApp.service.UserService;
import com.IBCSolutions.TransferApp.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/account") 
public class AccountController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    public AccountController(UserService userService, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    private Long getUserIdFromRequest(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Missing or invalid Authorization header.");
        }
        String jwt = authHeader.substring(7);
        return tokenProvider.getUserIdFromJWT(jwt);
    }

    @GetMapping
    public ResponseEntity<?> getAccount(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found."));

            Account account = user.getAccount();
            
            if (account == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No account associated with this user."));
            }
            
            return ResponseEntity.ok(account);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to retrieve account details."));
        }
    }
}