package com.chain.api;

import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UTXO;
import com.chain.api.core.Wallet.WalletUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TransactionTests {

    List<UTXO> utxos;
    private PublicKey publicKeySender;
    private PrivateKey privateKeySender;

    private PublicKey publicKeyReceiver;
    private PrivateKey privateKeyReceiver;

    @BeforeEach
    public void setup() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        utxos = new ArrayList<>();

        // generate random wallets for sender and receiver
        KeyPair keyPair = WalletUtil.generateKeyPair();
        publicKeySender = keyPair.getPublic();
        privateKeySender = keyPair.getPrivate();

        keyPair = WalletUtil.generateKeyPair();
        publicKeyReceiver = keyPair.getPublic();
        privateKeyReceiver = keyPair.getPrivate();

        /*
        Transaction transaction1 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, utxos);
        Transaction transaction2 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),4, utxos);
        Transaction transaction3 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),2, utxos);
         */
    }

    @Test
    public void verifyCoinbaseTransactions() {
        Transaction transaction1 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, utxos, 0);
        Transaction transaction2 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),4, utxos, 0);
        Transaction transaction3 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),2, utxos, 0);

        assertTrue(TransactionUtil.verifyCoinbaseTransaction(transaction1, 0));
    }
}
