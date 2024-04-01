package com.vikas.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fromWalletAddress;
    @Column(nullable = false)
    private String toWalletAddress;
    @Column(nullable = false)
    private BigDecimal txnAmount;
    @Column(nullable = false)
    private String txnStatus;
    @Column(nullable = false)
    private BigDecimal txnFee;
    @Column(nullable = false)
    private String txnHash;

}
