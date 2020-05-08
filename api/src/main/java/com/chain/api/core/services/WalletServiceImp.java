package com.chain.api.core.services;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.*;
import com.chain.api.core.Wallet.Wallet;
import com.chain.api.core.Wallet.WalletService;
import com.chain.api.core.Wallet.WalletUtil;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class WalletServiceImp implements WalletService {
    Logger logger = LoggerFactory.getLogger(WalletServiceImp.class);

    private List<UTXO> unspentTransactionOutputs;

    private List<Block> blockchain;

    private TransactionMapper transactionMapper;

    private UnconfirmedTransactions unconfirmedTransactions;

    @Autowired
    public void setUnspentTransactionOutputs(List<UTXO> unspentTransactionOutputs) {
        this.unspentTransactionOutputs = unspentTransactionOutputs;
    }

    @Autowired
    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    @Autowired
    public void setTransactionMapper(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    @Autowired
    public void setUnconfirmedTransactions(UnconfirmedTransactions unconfirmedTransactions) { this.unconfirmedTransactions = unconfirmedTransactions; }

    @Override
    public ResponseEntity<?> getUsersBalance(String walletPublicKey) {
        System.out.println(walletPublicKey);

        if(walletPublicKey.isBlank()) {
            logger.error("Wallet's public key is blank!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            PublicKey userPublicKey = CryptoUtil.getPublicKeyFromString(walletPublicKey);

            String userBalance = Float.toString(TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(userPublicKey, unspentTransactionOutputs)));

            System.out.println(userBalance);

            return new ResponseEntity<>(userBalance, HttpStatus.OK);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> generateWallet() {
        KeyPair keyPair = WalletUtil.generateKeyPair();

        Wallet wallet = new Wallet(CryptoUtil.getStringFromKey(keyPair.getPrivate()),CryptoUtil.getStringFromKey(keyPair.getPublic()));

        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    /**
     * Because of the lack of a database, we have to search for user's made and received transactions manually
     * @param walletPublicKey
     * @return
     */
    @Override
    public ResponseEntity<?> getUsersTransaction(String walletPublicKey) {

        List<TransactionResponse> sentTransactions = new ArrayList<>();
        List<TransactionResponse> receivedTransactions = new ArrayList<>();

        System.out.println(walletPublicKey);

        // skip the genesis block
        for(int i = 1; i < blockchain.size(); i++) {

            Block block = blockchain.get(i);

            List<Transaction> transactions = block.getTransactions();

            for(int j = 0; j < transactions.size(); j++) {

                Transaction tx = transactions.get(j);

                if(j != 0) {
                    if(CryptoUtil.getStringFromKey(tx.getSender()).equals(walletPublicKey)) {
                        TransactionResponse txResponse = transactionMapper.transactionToTransactionResponse(tx);
                        txResponse.setVerified(TransactionUtil.isVerified(tx, unconfirmedTransactions.getTransactions())); // true
                        txResponse.setOwnerBlock(block.getHash());
                        sentTransactions.add(txResponse);
                    }
                }

                if(CryptoUtil.getStringFromKey(tx.getReceiver()).equals(walletPublicKey)) {
                    TransactionResponse txResponse = transactionMapper.transactionToTransactionResponse(tx);
                    txResponse.setVerified(TransactionUtil.isVerified(tx, unconfirmedTransactions.getTransactions())); // true
                    txResponse.setOwnerBlock(block.getHash());
                    receivedTransactions.add(txResponse);
                }
            }

        }

        UserTransactionsResponse userTransactionsResponse = new UserTransactionsResponse(sentTransactions, receivedTransactions);

        return new ResponseEntity<>(userTransactionsResponse, HttpStatus.OK);

    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    private class UserTransactionsResponse {
        List<TransactionResponse> sentTransactions;
        List<TransactionResponse> receivedTransactions;
    }

}
