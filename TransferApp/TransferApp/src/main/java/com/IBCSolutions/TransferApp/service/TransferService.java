package com.IBCSolutions.TransferApp.service;

import com.IBCSolutions.TransferApp.model.dto.TransferRequest;
import com.IBCSolutions.TransferApp.model.entity.Account;
import com.IBCSolutions.TransferApp.model.entity.User; 
import com.IBCSolutions.TransferApp.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    public TransferService(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    @Transactional
    public String executeTransfer(Long senderUserId, TransferRequest request) {
        try {
            BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return "ERROR: Transfer amount must be positive.";
            }

            Account senderAccount = userService.findById(senderUserId)
                    .map(User::getAccount)
                    .orElse(null);

            if (senderAccount == null) {
                return "ERROR: Sender account not found (Authentication issue).";
            }

            Account recipientAccount = accountRepository.findByAccountNumber(request.getRecipientAccountNumber())
                    .orElse(null);

            if (recipientAccount == null) {
                return "ERROR: Recipient account not found.";
            }

            if (senderAccount.getBalance().compareTo(amount) < 0) {
                return "ERROR: Insufficient funds. Current balance: $" + senderAccount.getBalance().setScale(2, RoundingMode.HALF_UP);
            }

            senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
            recipientAccount.setBalance(recipientAccount.getBalance().add(amount));

            accountRepository.save(senderAccount);
            accountRepository.save(recipientAccount);

            return String.format("Transfer of $%.2f to account %s successful. New Balance: $%.2f",
                    amount, recipientAccount.getAccountNumber(), senderAccount.getBalance());

        } catch (Exception e) {
            System.err.println("Unexpected Transfer error: " + e.getMessage());
            return "ERROR: Transfer failed due to an unexpected server error.";
        }
    }

    @Transactional
    public String deposit(Long userId, BigDecimal amount) {
        try {
            BigDecimal depositAmount = amount.setScale(2, RoundingMode.HALF_UP);

            if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return "ERROR: Deposit amount must be positive.";
            }

            Account account = userService.findById(userId)
                    .map(User::getAccount)
                    .orElse(null);

            if (account == null) {
                return "ERROR: Account not found for user.";
            }

            account.setBalance(account.getBalance().add(depositAmount));
            accountRepository.save(account);

            return String.format("Deposit of $%.2f successful. New Balance: $%.2f",
                    depositAmount, account.getBalance());
                    
        } catch (Exception e) {
            System.err.println("Unexpected Deposit error: " + e.getMessage());
            return "ERROR: Deposit failed due to an unexpected server error.";
        }
    }

    @Transactional
    public String withdraw(Long userId, BigDecimal amount) {
        try {
            BigDecimal withdrawalAmount = amount.setScale(2, RoundingMode.HALF_UP);

            if (withdrawalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return "ERROR: Withdrawal amount must be positive.";
            }

            Account account = userService.findById(userId)
                    .map(User::getAccount)
                    .orElse(null);

            if (account == null) {
                return "ERROR: Account not found for user.";
            }

            if (account.getBalance().compareTo(withdrawalAmount) < 0) {
                return "ERROR: Insufficient funds. Current balance: $" + account.getBalance().setScale(2, RoundingMode.HALF_UP);
            }

            account.setBalance(account.getBalance().subtract(withdrawalAmount));
            accountRepository.save(account);

            return String.format("Withdrawal of $%.2f successful. New Balance: $%.2f",
                    withdrawalAmount, account.getBalance());

        } catch (Exception e) {
            System.err.println("Unexpected Withdrawal error: " + e.getMessage());
            return "ERROR: Withdrawal failed due to an unexpected server error.";
        }
    }
}