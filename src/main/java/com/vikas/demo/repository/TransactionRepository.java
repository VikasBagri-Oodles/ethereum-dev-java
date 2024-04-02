package com.vikas.demo.repository;

import com.vikas.demo.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Boolean existsByTxnHash(String txnHash);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.txnHash = :txnHash")
    Optional<Transaction> getTxnByTxnHash(@Param("txnHash") String txnHash);

}
