package com.vikas.demo.service;

import com.vikas.demo.domain.User;
import com.vikas.demo.domain.Wallet;
import com.vikas.demo.exception.CustomException;
import com.vikas.demo.exception.ResourceAlreadyExists;
import com.vikas.demo.exception.ResourceNotFoundException;
import com.vikas.demo.repository.WalletRepository;
import com.vikas.demo.util.EncryptDecrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Slf4j
@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserService userService;
    private static final Random random = new Random();
    private static final String walletDirectory = "/home/vikas/Documents/wallets";

    public String createWalletForUserId(Long userId) {

        // check for user
        User user = userService.findUserById(userId);

        // check whether wallet already exists
        checkWalletForUser(user);

        // create wallet for user
        Wallet wallet = createWalletForUser(user);
        walletRepository.save(wallet);
        return "Wallet created successfully";

    }

    private void checkWalletForUser(User user) {
        boolean walletExists = walletRepository.existsByUserId(user.getId());
        if (walletExists) {
            throw new ResourceAlreadyExists("Wallet for user already exists");
        }
    }

    public Wallet getWalletForUserId(Long id) {
        return walletRepository.findWalletByUserId(id).orElseThrow(() -> new ResourceNotFoundException("Wallet for user does not exist"));
    }

    private Wallet createWalletForUser(User user) {

        try {

            String walletPassword = user.getEmail() + random.nextInt(100, 500); // Use Encrypted form
            String walletName = WalletUtils.generateNewWalletFile(walletPassword, new File(walletDirectory));


            Credentials credentials = WalletUtils.loadCredentials(walletPassword, walletDirectory + "/" + walletName);
            String walletAddress = credentials.getAddress();

            String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(); // Use Encrypted form
            BigDecimal balance = new BigDecimal(0);
            String currencyName = "Ether";
            String currencyAbr = "ETH";
            String walletJsonFilePath = walletDirectory + File.separator + walletName;
            log.info("wallet json file path: {}", walletJsonFilePath);

            String walletPasswordEncrypted = EncryptDecrypt.encrypt(walletPassword);
            String privateKeyEncrypted = EncryptDecrypt.encrypt(privateKey);


            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setWalletAddress(walletAddress);
            wallet.setPrivateKey(privateKeyEncrypted);
            wallet.setBalance(balance);
            wallet.setCurrencyName(currencyName);
            wallet.setCurrencyAbr(currencyAbr);
            wallet.setWalletJsonFilePath(walletJsonFilePath);
            wallet.setWalletPassword(walletPasswordEncrypted);

            System.out.println("wallet configured successfully");
            return wallet;

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Error occurred while creating wallet: " + e.getMessage());
        }

    }

}
