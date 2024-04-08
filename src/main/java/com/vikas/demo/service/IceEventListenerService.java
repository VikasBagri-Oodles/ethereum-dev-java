package com.vikas.demo.service;

import com.vikas.demo.domain.Transaction;
import com.vikas.demo.erc20Tokens.Ice_token;
import io.reactivex.Flowable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class IceEventListenerService {

    public static final Ice_token iceToken;

    private final TransactionService transactionService;

    static {
        // load IceToken (i.e. ERC20 Token)
        String iceTokenAddress = "0x0ee68792599ca999BE628d2D4FE2F270a2902B08";
        Web3j web3j = Web3j.build(new HttpService("https://sepolia.drpc.org/"));
        iceToken = Ice_token.load(
                iceTokenAddress,
                web3j,
                Credentials.create("66b8494a3b22639ff3b074acd904d09022ace59923f87f6b427a6d87d1cf2f4b"),
                new DefaultGasProvider()
        );
    }

    @PostConstruct
    private void listenToTxnForIceToken() {

        try {

            Flowable<Ice_token.TransferEventResponse> flowable = iceToken.transferEventFlowable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST);
            flowable.subscribe(event -> {
                String from = event.from.toString();
                String to = event.to.toString();
                String amountICE = event.value.toString().substring(0, event.value.toString().length() - 18);
                System.out.println("Transfer from: %s to: %s an amount of ICE: %s".formatted(from, to, amountICE));

                // Create Transaction object and store it in the database
                Transaction transaction = new Transaction();
                transaction.setFromWalletAddress(from);
                transaction.setToWalletAddress(to);
                transaction.setTxnAmount(new BigDecimal(amountICE));
                transaction.setTxnStatus("Completed");
                transaction.setTxnFee(new BigDecimal("100000"));
                transaction.setTxnHash(event.log.getTransactionHash());
                transactionService.saveTxn(transaction);

            }, error -> {
                System.out.println("onError method called...");
                System.out.println(error.getMessage());
            });

        } catch (Exception e) {
            System.out.println("Error occurred");
            e.printStackTrace();
            System.out.println(e.getMessage());
        }


    }

}
