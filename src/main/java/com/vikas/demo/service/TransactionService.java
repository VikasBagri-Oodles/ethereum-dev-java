package com.vikas.demo.service;

import com.vikas.demo.domain.Transaction;
import com.vikas.demo.domain.Wallet;
import com.vikas.demo.dto.CreateTransactionDTO;
import com.vikas.demo.exception.CustomException;
import com.vikas.demo.exception.ResourceNotFoundException;
import com.vikas.demo.repository.TransactionRepository;
import com.vikas.demo.util.EncryptDecrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainIdLong;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final WalletService walletService;

    private final UserService userService;

    public Transaction getTxnByTxnHash(String txnHash) {
        return transactionRepository.getTxnByTxnHash(txnHash).orElseThrow(() -> new ResourceNotFoundException("Txn does not exists in the db"));
    }

    public void saveTxn(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public boolean checkForTxnHash(String txnHash) {
        return transactionRepository.existsByTxnHash(txnHash);
    }

    public String broadcastTxn(Long userId, CreateTransactionDTO createTransactionDTO) {

        // check whether user exists or not
        userService.findUserById(userId);

        // get wallet for user
        Wallet wallet = walletService.getWalletForUserId(userId);

        try {

            Web3j web3j = Web3j.build(new HttpService("\t\n" +
                    "https://rpc2.sepolia.org"));

            // <------------  1. LOAD AN ACCOUNT AND GET NONCE  ------------->

            String walletPassword = EncryptDecrypt.decrypt(wallet.getWalletPassword());
            log.info("walletPassword: " + walletPassword);
            String walletPath = wallet.getWalletJsonFilePath();
            log.info("walletPath: " + walletPath);

            // decrypt wallet and open it in the credential object
            Credentials credentials = WalletUtils.loadCredentials(walletPassword, walletPath);
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            log.info("nonce: " + nonce);

            // <---------  2.  CONFIGURE RECIPIENT WALLET ADDRESS AND AMOUNT OF WEI TO SEND --------->

            String recipientWalletAddress = createTransactionDTO.getToWalletAddress();
            BigInteger amountToSend = Convert.toWei(createTransactionDTO.getTxnAmountInEther(), Convert.Unit.ETHER).toBigInteger();
            log.info("recipientWalletAddress: {}", recipientWalletAddress);
            log.info("amountToSend in wei: {}", amountToSend);

            // <--------- 3.  CONFIGURE GAS PARAMETERS  ------------>

            // one simple send txn has a gas limit of 21000
            BigInteger gasLimit = BigInteger.valueOf(21000L);
            EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
            log.info("ehtGasPrice: {}", ethGasPrice.getGasPrice());
//            BigInteger gasPrice = Convert.toWei("26", Convert.Unit.GWEI).toBigInteger();
            BigInteger gasPrice = ethGasPrice.getGasPrice();
            log.info("custom gasPrice: {}", gasPrice);
//            BigInteger gasPrice = Convert.toWei("0.000000001", Convert.Unit.ETHER).toBigInteger();

            // <--------- 4.  PREPARE THE RAW TRANSACTION  ----------->

            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    recipientWalletAddress,
                    amountToSend
            );

            // <----------  5.  SIGN THE TXN  ------------>
            long chainId = 11155111L;
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId,  credentials);

            // <----------  6.  SEND IT VIA JSON-RPC

            String hexSignedMessage = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexSignedMessage).send();
            String transactionHash = ethSendTransaction.getTransactionHash();
            log.info("txn hash: {}", transactionHash);

            // Construct a TRANSACTION object and store it in the database
            Transaction transaction = new Transaction();
            transaction.setFromWalletAddress(credentials.getAddress());
            transaction.setToWalletAddress(createTransactionDTO.getToWalletAddress());
            transaction.setTxnAmount(new BigDecimal(amountToSend));
            transaction.setTxnStatus("Pending"); // txn is broadcast only, not finalized yet
            transaction.setTxnFee(new BigDecimal(gasPrice.multiply(new BigInteger(String.valueOf(gasLimit)))));
            transaction.setTxnHash(transactionHash);
            transactionRepository.saveAndFlush(transaction);

            return "Txn has been broadcast successfully: " + transactionHash;

        } catch (Exception e) {
            log.error("Error occurred while broadcasting txn: {}", e.getMessage());
            throw new CustomException("Error occurred while broadcasting txn: " + e.getMessage());
        }

    }

    public String broadcastTxnUsingOnlyPrivateKey(Long userId, CreateTransactionDTO createTransactionDTO) {

        // check whether user exists or not
        userService.findUserById(userId);

        // get wallet for user
        Wallet wallet = walletService.getWalletForUserId(userId);

        try {

            Web3j web3j = Web3j.build(new HttpService("https://eth-sepolia.public.blastapi.io"));

            // <------------  1. LOAD AN ACCOUNT AND GET NONCE  ------------->

            String privateKey = EncryptDecrypt.decrypt(wallet.getPrivateKey());
            Credentials credentials = Credentials.create(privateKey);
            String walletAddress = credentials.getAddress();
            log.info("wallet address: {}", walletAddress);

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(walletAddress, DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            log.info("nonce: " + nonce);

            // <---------  2.  CONFIGURE RECIPIENT WALLET ADDRESS AND AMOUNT OF WEI TO SEND --------->

            String recipientWalletAddress = createTransactionDTO.getToWalletAddress();
            BigInteger amountToSend = Convert.toWei(createTransactionDTO.getTxnAmountInEther(), Convert.Unit.ETHER).toBigInteger();
            log.info("recipientWalletAddress: {}", recipientWalletAddress);
            log.info("amountToSend in wei: {}", amountToSend);

            // <--------- 3.  CONFIGURE GAS PARAMETERS  ------------>

            // one simple send txn has a gas limit of 21000
            BigInteger gasLimit = BigInteger.valueOf(21000L);
            BigInteger gasPrice = Convert.toWei("26", Convert.Unit.GWEI).toBigInteger();
//            BigInteger gasPrice = Convert.toWei("0.000000001", Convert.Unit.ETHER).toBigInteger();

            // <--------- 4.  PREPARE THE RAW TRANSACTION  ----------->

            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    recipientWalletAddress,
                    amountToSend
            );

            // <----------  5.  SIGN THE TXN  ------------>
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);

            // <----------  6.  SEND IT VIA JSON-RPC

            String hexSignedMessage = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexSignedMessage).send();
            String transactionHash = ethSendTransaction.getTransactionHash();
            log.info("txn hash: {}", transactionHash);

            // Construct a TRANSACTION object and store it in the database
            Transaction transaction = new Transaction();
            transaction.setFromWalletAddress(credentials.getAddress());
            transaction.setToWalletAddress(createTransactionDTO.getToWalletAddress());
            transaction.setTxnAmount(new BigDecimal(amountToSend));
            transaction.setTxnStatus("Pending"); // txn is broadcast only, not finalized yet
            transaction.setTxnFee(new BigDecimal(gasPrice.multiply(new BigInteger(String.valueOf(gasLimit)))));
            transaction.setTxnHash(transactionHash);
            transactionRepository.save(transaction);

            return "Txn has been broadcast successfully: " + transactionHash;

        } catch (Exception e) {
            log.error("Error occurred while broadcasting txn: {}", e.getMessage());
            e.printStackTrace();
            throw new CustomException("Error occurred while broadcasting txn: " + e.getMessage());
        }

    }

}
