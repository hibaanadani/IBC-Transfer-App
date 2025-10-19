package com.IBCSolutions.TransferApp.service;

import com.IBCSolutions.TransferApp.model.dto.LoginRequest;
import com.IBCSolutions.TransferApp.model.dto.LoginResponse;
import com.IBCSolutions.TransferApp.model.dto.RegisterRequest;
import com.IBCSolutions.TransferApp.model.entity.Account;
import com.IBCSolutions.TransferApp.model.entity.User;
import com.IBCSolutions.TransferApp.repository.AccountRepository;
import com.IBCSolutions.TransferApp.repository.UserRepository;
import com.IBCSolutions.TransferApp.util.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));

        String jwt = tokenProvider.generateToken(user.getId(), authentication);

        return new LoginResponse(jwt, "Bearer");
    }

    @Transactional
    public String registerNewUser(RegisterRequest registerRequest) {
        try {
            if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
                return "ERROR: Username is already taken!";
            }

            BigDecimal initialDeposit = registerRequest.getInitialDeposit();

            if (initialDeposit == null || initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
                return "ERROR: Initial deposit must be a non-negative value.";
            }

            User newUser = new User(
                    registerRequest.getUsername(),
                    passwordEncoder.encode(registerRequest.getPassword())
            );
            newUser = userRepository.save(newUser);

            Account newAccount = new Account();
            newAccount.setUser(newUser);
            newAccount.setAccountNumber(UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            newAccount.setBalance(initialDeposit);
            accountRepository.save(newAccount);

            newUser.setAccount(newAccount);

            return "User registered and account created successfully!";

        } catch (Exception e) {
            System.err.println("Unexpected Registration error: " + e.getMessage());
            return "ERROR: Registration failed due to an unexpected server error.";
        }
    }
}