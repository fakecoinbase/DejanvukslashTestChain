package com.chain.api;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.*;
import com.chain.api.core.Wallet.WalletUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTests {

    private List<UTXO> utxos;

    private List<UTXO> utxosOtherNode;

    private PublicKey publicKeySender;
    private PrivateKey privateKeySender;

    private PublicKey publicKeyReceiver;
    private PrivateKey privateKeyReceiver;

    private PublicKey publicKeyThirdUser;
    private PrivateKey privateKeyThirdUser;

    private UnconfirmedTransactions unconfirmedTransactions;

    @BeforeEach
    public void setup() {
        System.out.println("=============== Start of Test Setup ===============\n");

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        utxos = new ArrayList<>();
        utxosOtherNode = new ArrayList<>();

        unconfirmedTransactions = new UnconfirmedTransactions();

        // generate random wallets for sender and receiver and for a third user
        KeyPair keyPair = WalletUtil.generateKeyPair();
        publicKeySender = keyPair.getPublic();
        privateKeySender = keyPair.getPrivate();

        keyPair = WalletUtil.generateKeyPair();
        publicKeyReceiver = keyPair.getPublic();
        privateKeyReceiver = keyPair.getPrivate();

        keyPair = WalletUtil.generateKeyPair();
        publicKeyThirdUser = keyPair.getPublic();
        privateKeyThirdUser = keyPair.getPrivate();

        System.out.println("public key sender:      " + CryptoUtil.getStringFromKey(publicKeySender));

        System.out.println("public key receiver     " + CryptoUtil.getStringFromKey(publicKeyReceiver));

        System.out.println("public key third user   " + CryptoUtil.getStringFromKey(publicKeyThirdUser));

        System.out.println("=============== End of Test Setup ===============\n");
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
     * Verifies the creation of transactions
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
     * verifies the sending of multiple transactions
     */
    @Test
    public void sendTransactionsTest() {
        List<Transaction> transactions = new ArrayList<>();
        // Create 3 transactions of 12 coins total to sender
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, utxos, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),4, utxos, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),3, utxos, 0));

        // Create 2 transactions of 17 coins total to receiver
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeyReceiver),9, utxos, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeyReceiver),8, utxos, 0));

        try {
            // Try sending 2 coins from sender to receiver
            transactions.add(TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 2, utxos, 0));

            // the first transaction of 5 coins was split in 2 coins that were sent to receiver and 3 coins that were sent back to sender
            // so sender has 3 utxos and receiver received 1 extra utxo,making it 3
            // the utxo value order for sender is 4 3 3 and the utxo order for receiver is 9 8 2
            assertEquals(3, TransactionUtil.getUserUtxos(publicKeySender,utxos).size());
            assertEquals(3, TransactionUtil.getUserUtxos(publicKeyReceiver,utxos).size());
            assertEquals(12 - 2, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeySender,utxos)));
            assertEquals(17 + 2, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeyReceiver,utxos)));

            // Try sending all 19 coins of receiver to third user
            transactions.add(TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeyReceiver),CryptoUtil.getStringFromKey(publicKeyThirdUser), 19, utxos, 0));

            assertEquals(3, TransactionUtil.getUserUtxos(publicKeySender,utxos).size());
            assertEquals(0, TransactionUtil.getUserUtxos(publicKeyReceiver,utxos).size());
            assertEquals(1, TransactionUtil.getUserUtxos(publicKeyThirdUser,utxos).size());
            assertEquals(10, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeySender,utxos)));
            assertEquals(19 - 19, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeyReceiver,utxos)));
            assertEquals(0 + 19, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeyThirdUser,utxos)));


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
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

    /**
     * Tests the addition of incoming block transactions
     */
    @Test
    public void updateTxosTest() {
        // Make some  coinbase transactions
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, utxos, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),4, utxos, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),3, utxos, 0));
        try {
            TransactionUtil.updateUtxos(transactions,utxosOtherNode); // send the 3 created transactions to second node

            // You can only send blocks or single transactions so we use lists to mimic a block
            // 1. Sender creates 2 transactions on a new node
            List<Transaction> newTransactions = new ArrayList<>();
            newTransactions.add(TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 2, utxosOtherNode, 1));
            newTransactions.add(TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 3, utxosOtherNode, 1));
            TransactionUtil.updateUtxos(newTransactions,utxos); // send the 2 created transactions to first node

            // the 2 nodes should have the same utxos in the same exact order
            assertEquals(utxos.size(),utxosOtherNode.size());
            for(int i = 0; i < utxos.size(); i++) {
                if(utxos.get(i).getValue() != utxosOtherNode.get(i).getValue()) fail();
            }

            // 2. sender creates 2 more transactions in a new block on the second node but sends them in mixed order
            // the value would still be the same despite having different utxo's
            List<Transaction> otherTransactions = new ArrayList<>();
            otherTransactions.add(TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 3, utxosOtherNode, 2));
            otherTransactions.add(TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 4, utxosOtherNode, 2));

            // Send a block with the same transactions as the previous block just with the transactions in switched order
            List<Transaction> switchedTransactions = new ArrayList<>();
            switchedTransactions.add(otherTransactions.get(otherTransactions.size() - 1));
            switchedTransactions.add(otherTransactions.get(otherTransactions.size() - 2));
            TransactionUtil.updateUtxos(switchedTransactions,utxos); // send the 2 created transactions to first node

            // The value in the UTXO's and the owner must be the receiver
            Integer[] firstUtxos = {2,3,3,4};
            Integer[] secondUtxos = {2,3,4,3};

            for(int i = 0; i < utxosOtherNode.size(); i++) {
                UTXO utxo = utxosOtherNode.get(i);
                if(utxo.getValue() != firstUtxos[i] || !CryptoUtil.getStringFromKey(utxo.getOwner()).equals(CryptoUtil.getStringFromKey(publicKeyReceiver)))
                fail();
            }

            for(int i = 0; i < utxos.size(); i++) {
                UTXO utxo = utxos.get(i);
                if(utxo.getValue() != secondUtxos[i] || !CryptoUtil.getStringFromKey(utxo.getOwner()).equals(CryptoUtil.getStringFromKey(publicKeyReceiver))) fail();
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    /**
     * test the method that gets the user UTXO's
     */
    @Test
    public void getUserUtxosTest() {
        List<Transaction> transactions = new ArrayList<>();
        // Create 3 transactions of 12 coins total to sender
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, utxos, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),4, utxos, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),3, utxos, 0));

        // Create 2 transactions of 3 coins total to receiver
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeyReceiver),2, utxos, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeyReceiver),1, utxos, 0));

        List<UTXO> userUtxos = TransactionUtil.getUserUtxos(publicKeySender,utxos);
        Float value = TransactionUtil.getUsersBalance(userUtxos);

        assertEquals(3,userUtxos.size()); // sender has 3 UTXO's
        assertEquals(12,value); // of 12 coins total

        userUtxos = TransactionUtil.getUserUtxos(publicKeyReceiver,utxos);
        value = TransactionUtil.getUsersBalance(userUtxos);

        assertEquals(2,userUtxos.size()); // receiver has 2 UTXO's
        assertEquals(3,value); // of 3 coins total

    }
}
