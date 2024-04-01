package com.vikas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionDTO {

    private String toWalletAddress;
    private BigDecimal txnAmountInEther;

}
