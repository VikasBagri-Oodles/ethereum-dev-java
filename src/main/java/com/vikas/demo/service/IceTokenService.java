package com.vikas.demo.service;

import com.vikas.demo.domain.Transaction;
import com.vikas.demo.domain.User;
import com.vikas.demo.domain.Wallet;
import com.vikas.demo.dto.SendIceDTO;
import com.vikas.demo.erc20Tokens.Ice_token;
import com.vikas.demo.exception.CustomException;
import com.vikas.demo.repository.TransactionRepository;
import com.vikas.demo.util.EncryptDecrypt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;

@RequiredArgsConstructor
@Service
public class IceTokenService {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;

    public String sendIceToken(Long userId, SendIceDTO sendIceDTO) {

        // check for user
        User sender = userService.findUserById(userId);

        // check for wallet
        Wallet spenderWallet = walletService.getWalletForUserId(sender.getId());

        try {

            // load IceToken (i.e. ERC20 Token)
            String iceTokenAddress = "0x0ee68792599ca999BE628d2D4FE2F270a2902B08";
            Web3j web3j = Web3j.build(new HttpService("https://sepolia.drpc.org/"));
            String walletPassword = EncryptDecrypt.decrypt(spenderWallet.getWalletPassword());
            String walletPath = spenderWallet.getWalletJsonFilePath();
            TransactionManager transactionManager = new RawTransactionManager(
                    web3j,
                    WalletUtils.loadCredentials(walletPassword, walletPath),
                    Long.parseLong(web3j.netVersion().send().getNetVersion())
            );
            Ice_token iceToken = Ice_token.load(
                    iceTokenAddress,
                    web3j,
                    transactionManager,
                    new DefaultGasProvider()
            );

            BigInteger allowanceAmount = new BigInteger(sendIceDTO.getIceToSend() + "000000000000000000");
            String spenderWalletAddress = spenderWallet.getWalletAddress();
            TransactionReceipt tr = iceToken.approve(spenderWalletAddress, allowanceAmount).send();
            TransactionReceipt transactionReceipt = iceToken.transferFrom(spenderWalletAddress, sendIceDTO.getToWalletAddress(), allowanceAmount).send();
            System.out.println("Txn hash: " + transactionReceipt.getTransactionHash());
            System.out.println("Txn status: " + transactionReceipt.getStatus());

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Error occurred while sending ICE: " + e.getMessage());
        }

        return "ICE sent successfully";

    }

}
