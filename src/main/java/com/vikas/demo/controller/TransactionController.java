package com.vikas.demo.controller;

import com.vikas.demo.dto.CreateTransactionDTO;
import com.vikas.demo.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/user/{userId}/broadcastTxn")
    public ResponseEntity<String> broadcastTxn(@PathVariable(name = "userId") Long userId,
                                               @RequestBody CreateTransactionDTO createTransactionDTO) {

        String message = transactionService.broadcastTxn(userId, createTransactionDTO);
        return new ResponseEntity<>(message, HttpStatus.CREATED);

    }

}
