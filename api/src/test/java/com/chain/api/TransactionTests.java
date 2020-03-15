package com.chain.api;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionInput;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UTXO;
import com.chain.api.core.Wallet.WalletUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransactionTests {

    private List<UTXO> utxos;

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

        // print them!
        System.out.println("public key sender: " + CryptoUtil.getStringFromKey(publicKeySender));

        System.out.println("public key receiver " +CryptoUtil.getStringFromKey(publicKeyReceiver));
    }

    /**
     *
     */
    @Test
    public void verifyCoinbaseTransactions() {
        Transaction transaction1 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, utxos, 0);
        assertTrue(TransactionUtil.verifyCoinbaseTransaction(transaction1, 0));
    }

    /**
     *
     */
    @Test
    public void getUserUtxosTest() {
        Transaction transaction1 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, utxos, 0);
        List<UTXO> userUtxos = TransactionUtil.getUserUtxos(publicKeySender,utxos);
        assertTrue(userUtxos.size() == 1);
    }

    /**
     * Verifies the sending of transactions
     */
    @Test
    public void verifyTransaction() {
        // Sender mines a block to get some coins
        Transaction transaction1 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, utxos, 0);
        /*
        System.out.println("Before sending a transaction");
        utxos.stream().forEach(utxo -> {
            System.out.println("owner:" + CryptoUtil.getStringFromKey(utxo.getOwner()) + " value:" + utxo.getValue());
        });
        */
        assertTrue(utxos.size() == 1);
        assertEquals(CryptoUtil.getStringFromKey(utxos.get(0).getOwner()),CryptoUtil.getStringFromKey(publicKeySender));

        // Send 5 coins to receiver
        try {
            Transaction transaction2 = TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender), CryptoUtil.getStringFromKey(publicKeyReceiver),5,utxos,0);
            assertTrue(utxos.size() == 1);
            assertEquals(CryptoUtil.getStringFromKey(utxos.get(0).getOwner()),CryptoUtil.getStringFromKey(publicKeyReceiver));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        // Send 5 coins back to sender
        try {
            Transaction transaction3 = TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeyReceiver), CryptoUtil.getStringFromKey(publicKeySender),5,utxos,0);
            assertTrue(utxos.size() == 1);
            assertEquals(CryptoUtil.getStringFromKey(utxos.get(0).getOwner()),CryptoUtil.getStringFromKey(publicKeySender));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        // Send 6 coins to receiver when sender has only 5
        RuntimeException exception = assertThrows(RuntimeException.class, () -> TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender), CryptoUtil.getStringFromKey(publicKeyReceiver),6,utxos,0));
        assertEquals("Sender does not have enough funds", exception.getMessage());


    }

    /**
     * Test the locking and verification methods of Transaction inputs
     */
    @Test
    public void lockAndVerifyInputs() {
        String txid = "test txid";
        // give sender 15 coins
        utxos.add(new UTXO("trans0",0,publicKeySender,5));
        utxos.add(new UTXO("trans1",0,publicKeySender,7));
        utxos.add(new UTXO("trans2",0,publicKeySender,2));

        // create some TXI's to reference them
        List<TransactionInput> inputs = new ArrayList<>();
        inputs.add(new TransactionInput("trans0",0,""));
        inputs.add(new TransactionInput("trans1",0,""));
        inputs.add(new TransactionInput("trans2",0,""));

        // sign the TXI's with some gibberish txid to sender
        boolean status = TransactionUtil.lockTransactionInputs(privateKeySender,inputs,txid,utxos);

        assertTrue(status);

        // modify  a TXI to refference a non-existent transaction
        inputs.add(new TransactionInput("trans3",0,""));

        status = TransactionUtil.lockTransactionInputs(privateKeySender,inputs,txid,utxos);

        assertFalse(status);

        /*
         test the method that validates the inputs
         */

        //trans3 has a null signature so it will throw a runtime error when we try to decode the signature

        RuntimeException exception = assertThrows(RuntimeException.class, () -> TransactionUtil.verifyTransactionInputs(publicKeySender,inputs,txid));
        assertEquals("java.security.SignatureException: error decoding signature bytes.", exception.getMessage());

        inputs.remove(3); // remove the bad TXI and try again

        status = TransactionUtil.verifyTransactionInputs(publicKeySender,inputs,txid);

        assertTrue(status);
    }
}
