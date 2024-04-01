package com.vikas.demo.repository;

import com.vikas.demo.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Query("SELECT " +
            "CASE WHEN COUNT( w ) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Wallet w " +
            "WHERE w.user.id = :userId")
    Boolean existsByUserId(@Param("userId") Long userId);

    @Query("SELECT w FROM Wallet w " +
            "WHERE w.user.id = :userId")
    Optional<Wallet> findWalletByUserId(@Param("userId") Long userId);

}
