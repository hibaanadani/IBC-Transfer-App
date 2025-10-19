package com.IBCSolutions.TransferApp.controller;

import com.IBCSolutions.TransferApp.model.dto.TransferRequest;
import com.IBCSolutions.TransferApp.model.dto.TransactionRequest;
import com.IBCSolutions.TransferApp.service.TransferService;
import com.IBCSolutions.TransferApp.util.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class TransferController {

    private final TransferService transferService;
    private final JwtTokenProvider tokenProvider;
    private static final String ERROR_PREFIX = "ERROR: ";

    public TransferController(TransferService transferService, JwtTokenProvider tokenProvider) {
        this.transferService = transferService;
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

    @PostMapping("/transfers")
    public ResponseEntity<String> createTransfer(@RequestBody TransferRequest request, HttpServletRequest httpRequest) {
        try {
            Long senderId = getUserIdFromRequest(httpRequest);
            String result = transferService.executeTransfer(senderId, request);

            if (result.startsWith(ERROR_PREFIX)) {
                // Determine status: 400 for business logic (e.g., insufficient funds) or 500 for server error
                HttpStatus status = result.contains("server error") ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(status).body(result.substring(ERROR_PREFIX.length()));
            }

            return ResponseEntity.ok(result);

        } catch (SecurityException e) {
            // Handle error from JWT extraction (401 Unauthorized)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/accounts/deposit")
    public ResponseEntity<String> depositFunds(@RequestBody TransactionRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            String result = transferService.deposit(userId, request.getAmount());

            if (result.startsWith(ERROR_PREFIX)) {
                HttpStatus status = result.contains("server error") ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(status).body(result.substring(ERROR_PREFIX.length()));
            }

            return ResponseEntity.ok(result);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/accounts/withdraw")
    public ResponseEntity<String> withdrawFunds(@RequestBody TransactionRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            String result = transferService.withdraw(userId, request.getAmount());

            if (result.startsWith(ERROR_PREFIX)) {
                HttpStatus status = result.contains("server error") ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(status).body(result.substring(ERROR_PREFIX.length()));
            }

            return ResponseEntity.ok(result);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}