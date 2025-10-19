package com.IBCSolutions.TransferApp.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String recipientAccountNumber;
    private BigDecimal amount;
}