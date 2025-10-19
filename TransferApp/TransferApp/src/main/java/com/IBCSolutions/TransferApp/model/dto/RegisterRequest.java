package com.IBCSolutions.TransferApp.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegisterRequest {

    private String username;
    private String password;
    private BigDecimal initialDeposit;
}