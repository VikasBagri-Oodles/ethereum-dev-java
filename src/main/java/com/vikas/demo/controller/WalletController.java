package com.vikas.demo.controller;

import com.vikas.demo.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class WalletController {

    private final WalletService walletService;
    @PostMapping("/user/{userId}/wallet")
    public ResponseEntity<String> createWallet(@PathVariable(name = "userId") Long userId) {

        String response = walletService.createWalletForUserId(userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

}
