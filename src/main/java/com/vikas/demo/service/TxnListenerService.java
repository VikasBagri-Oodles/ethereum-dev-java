package com.vikas.demo.service;

import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class TxnListenerService {

    private final TransactionService transactionService;

    @PostConstruct
    public void startTxnListening() {

        System.out.println("txn listener service has been started...");
        Web3j web3j = Web3j.build(new HttpService("https://rpc.sepolia.org"));

            Disposable disposable = web3j.transactionFlowable().subscribe(txn -> {
                try {
                    if (transactionService.checkForTxnHash(txn.getHash())) {
                        log.info("txn detail: to -> {}, from -> {}, hash -> {}", txn.getTo(), txn.getFrom(), txn.getHash());
                        Optional<TransactionReceipt> transactionReceipt = web3j.ethGetTransactionReceipt(txn.getHash()).send().getTransactionReceipt();
                        if (transactionReceipt.isPresent()) {
                            if (transactionReceipt.get().getStatus() != null && transactionReceipt.get().getStatus().equalsIgnoreCase("0x1")) {
                                log.info("Transaction successful");
                             } else {
                                log.info("Transaction failed!!!");
                            }
                        } else {
                            log.info("Transaction receipt not found");
                        }
                    }
                } catch (Exception e) {
                    log.error("error occurred during txn listen: {}", e.getMessage());
                }
            }, error -> {
                System.out.println(error.getMessage());
                System.out.println("An error occurred!");
            });

    }

}
