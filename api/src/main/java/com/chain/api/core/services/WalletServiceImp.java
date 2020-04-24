package com.chain.api.core.services;

import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UTXO;
import com.chain.api.core.Wallet.WalletService;
import com.chain.api.core.Wallet.WalletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@RestController
public class WalletServiceImp implements WalletService {
    Logger logger = LoggerFactory.getLogger(WalletServiceImp.class);

    private List<UTXO> unspentTransactionOutputs;

    @Autowired
    public void setUnspentTransactionOutputs(List<UTXO> unspentTransactionOutputs) {
        this.unspentTransactionOutputs = unspentTransactionOutputs;
    }

    @Override
    public ResponseEntity<?> getUsersBalance(String walletPublicKey) {
        if(walletPublicKey.isBlank()) {
            logger.error("Wallet's public key is blank!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            PublicKey userPublicKey = CryptoUtil.getPublicKeyFromString(walletPublicKey);

            String userBalance = Float.toString(TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(userPublicKey, unspentTransactionOutputs)));

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

        String userWallet = "Public Key: " + CryptoUtil.getStringFromKey(keyPair.getPublic()) + " /n" +
                "Private key: " + CryptoUtil.getStringFromKey(keyPair.getPrivate());

        return new ResponseEntity<>(userWallet, HttpStatus.OK);
    }
}
