package com.vikas.demo.service;

import com.vikas.demo.domain.Transaction;
import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        Web3j web3j = Web3j.build(new HttpService("https://rpc2.sepolia.org"));

            Disposable disposable = web3j.transactionFlowable().subscribe(txn -> {
                try {
                    System.out.println("txn: " + txn.getHash());
                    if (transactionService.checkForTxnHash(txn.getHash())) {
                        log.info("txn detail: to -> {}, from -> {}, hash -> {}", txn.getTo(), txn.getFrom(), txn.getHash());
                        Optional<TransactionReceipt> transactionReceipt = web3j.ethGetTransactionReceipt(txn.getHash()).send().getTransactionReceipt();
                        if (transactionReceipt.isPresent()) {
                            if (transactionReceipt.get().getStatus() != null && transactionReceipt.get().getStatus().equalsIgnoreCase("0x1")) {
                                log.info("Transaction successful");
                                // update status for txn (i.e. txn hash) in db
                                Transaction transaction = transactionService.getTxnByTxnHash(txn.getHash());
                                transaction.setTxnStatus("Completed");
                                transactionService.saveTxn(transaction);
                                log.info("Txn status been stored updated in db successfully");
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
