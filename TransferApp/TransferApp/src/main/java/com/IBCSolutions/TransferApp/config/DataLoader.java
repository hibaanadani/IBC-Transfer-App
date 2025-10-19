package com.IBCSolutions.TransferApp.config;

import com.IBCSolutions.TransferApp.model.entity.Account;
import com.IBCSolutions.TransferApp.model.entity.User;
import com.IBCSolutions.TransferApp.repository.AccountRepository;
import com.IBCSolutions.TransferApp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {

            User user1 = new User("user1", passwordEncoder.encode("password"));
            user1 = userRepository.save(user1);

            Account account1 = new Account();
            account1.setUser(user1);
            account1.setAccountNumber("100001");
            account1.setBalance(new BigDecimal("500.00"));
            accountRepository.save(account1);

            user1.setAccount(account1);


            User user2 = new User("user2", passwordEncoder.encode("password"));
            user2 = userRepository.save(user2);

            Account account2 = new Account();
            account2.setUser(user2);
            account2.setAccountNumber("100002");
            account2.setBalance(new BigDecimal("1000.00"));
            accountRepository.save(account2);

            user2.setAccount(account2);

            System.out.println("Initial user data loaded successfully (user1 and user2).");
        }
    }
}