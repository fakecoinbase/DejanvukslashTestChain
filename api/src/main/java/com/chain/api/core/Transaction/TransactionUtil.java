package com.chain.api.core.Transaction;

import com.chain.api.core.Block.Block;
import com.chain.util.crypto.CryptoUtil;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of utils for Transactions
 */


public class TransactionUtil {
    /**
     * only the user that owns the UTXOs can use them as inputs
     * signs the inputs to the owner of the referenced output
     * @param sender the creator of the transaction
     * @param inputs transaction inputs created by the user
     * @param TXID the data used to sign the transaction
     * @param userUTXOs either get them from local or read the owner's current UTXO's from the db
     */
    public static boolean lockTransactionInputs(PrivateKey sender, List<TransactionInput> inputs, String TXID, List<UTXO> userUTXOs) {
        for(int i = 0; i < inputs.size(); i++) {
            TransactionInput txi = inputs.get(i);
            UTXO tempUtxo = userUTXOs.stream().filter(utxo -> utxo.getPreviousTx().equals(txi.getPreviousTx()) && utxo.getIndex() == txi.getIndex()).findAny().orElse(null);
            if(tempUtxo == null) {
                System.out.println("Input doesn't match output!");
                return false;
            }
            try {
                if(CryptoUtil.getStringFromKey(tempUtxo.getOwner()) != CryptoUtil.getStringFromKey(CryptoUtil.DerivePubKeyFromPrivKey((BCECPrivateKey) sender))) {
                    System.out.println("The owner is different!");
                    return false;
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
            String signature = CryptoUtil.byteArrayToHexString(CryptoUtil.applyECDSASig(sender, TXID));
            txi.setSignature(signature);
        }

        return true;
    }

    /**
     * Verify that the transaction inputs could have only been created by the owner of the utxo they refference
     * @param inputs transaction's inputs
     * @param TXID the data that was used to create the input's signature / to sign the input
     * @param owner the creator of the transaction
     * @return true if the transaction was verified succesfully
     */
    public static boolean verifyTransactionInputs(PublicKey owner,List<TransactionInput> inputs, String TXID) {
        for(int i = 0; i < inputs.size(); i++) {
            TransactionInput txi = inputs.get(i);
            if(CryptoUtil.verifyECDSASig(owner, TXID, CryptoUtil.hexStringToByteArray(txi.getSignature()))) {
                System.out.println("The transaction input with index: " + txi.getIndex() + " was modified!");
                return false;
            }
        }
        return true;
    }

    /**
     * Generate's the transaction id TXID which is the SHA256 encrypted value of the inputs,outputs,sender,receiver,value and a random nonce to avoid identical hashes
     */
    public static String generateTransactionId(List<TransactionInput> inputs, List<TransactionOutput> outputs, PublicKey sender, PublicKey receiver, float value) {
        String txIn = inputs.stream().map( TXI -> TXI.getPreviousTx() + TXI.getIndex()).reduce("", (subtotal, element) -> subtotal + element);
        String txOut = outputs.stream().map( TXO -> TXO.getTo() + Float.toString(TXO.getValue())).reduce("", (subtotal, element) -> subtotal + element);
        int nonce =  new Random().nextInt(1000);
        String TXID = CryptoUtil.encryptSha256(
                CryptoUtil.getStringFromKey(sender) +
                        CryptoUtil.getStringFromKey(receiver) +
                        Float.toString(value) +
                        txIn +
                        txOut +
                        Integer.toString(nonce));
        return TXID;
    }

    /**
     *
     * update's the internal list and database of unspent transactions
     * used whenver a new block is received from a peer
     * @param transactions of the newly received block
     * @param currentUTXOs the UTXO's of the blockchain prior to the new received block
     * @return the new list of unspent transaction outputs
     */
    public static List<UTXO> updateUtxos(List<Transaction> transactions, List<UTXO> currentUTXOs) {
        // Retrieve all the UTXO's from the received block
        List<UTXO> blocksUTXOs = new ArrayList<>(); // the UTXO's from the received block
        transactions.stream().forEach(transaction -> {
            int i = 0;
            List<TransactionOutput> outputs = transaction.getOutputs();
            while(i < outputs.size()) {
                TransactionOutput to = outputs.get(i);
                blocksUTXOs.add(new UTXO(transaction.getTXID(), i++, to.getTo(), to.getValue()));
            }
        });

        // check which currentUTXOs and blocksUTXOs are consumed by the transactions from the received block

        // 1.get all the new block's TXI's
        List<UTXO> consTXOs =  new ArrayList<>();
        transactions.stream().forEach(transaction -> {
            transaction.getInputs().stream().forEach(input -> {
                consTXOs.add(new UTXO(input.getPreviousTx(),input.getIndex(), null, 0));});});

        // 2.remove the consumed UTXO's from the currentUTXO's
        List<UTXO> returnUTXOs = currentUTXOs.stream().filter(utxo -> {
            for(int i = 0; i < consTXOs.size(); i++) {
                UTXO spentUTXO = consTXOs.get(i);
                if(utxo.getPreviousTx() == spentUTXO.getPreviousTx() && utxo.getIndex() == spentUTXO.getIndex())
                    return false;
            }
            return true;
        }).collect(Collectors.toList());

        // 3. remove the consumed UTXO's from the blocksUTXOs
        /*
        List<UTXO> returnBlocksUTXOs = blocksUTXOs.stream().filter(utxo -> {
            for(int i = 0; i < consTXOs.size(); i++) {
                UTXO spentUTXO = consTXOs.get(i);
                if(utxo.getPreviousTx() == spentUTXO.getPreviousTx() && utxo.getIndex() == spentUTXO.getIndex())
                    return false;
            }
            return true;
        }).collect(Collectors.toList());

         */

        // 4.merge 2 and 3 lists and return them
        //returnUTXOs.addAll(returnBlocksUTXOs);
        //return returnUTXOs;

        return Stream.of(returnUTXOs,blocksUTXOs)
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
    }

    /**
     * find the specific TXO refferenced by the TI in the blockchain
     * @param TI
     * @param blockchain
     * @return TXO or null
     */
    public static TransactionOutput findTransactionOutput(TransactionInput TI, List<Block> blockchain) {
        // Find the transaction the TXI refers to
        Transaction transaction = findTransaction(TI.getPreviousTx(), blockchain);
        if(transaction == null) {
            System.out.println("Transaction: " + TI + " not found!");
            return null;
        }
        List<TransactionOutput> txos = transaction.getOutputs();
        if(TI.getIndex() >= txos.size()) {
            System.out.println("Invalid output index!");
            return null;
        }
        TransactionOutput txo = txos.get(TI.getIndex());
        return txo;
    }

    /**
     * Find the transaction with the specific transaction id in the blockchain
     * @param TXID
     * @param blockchain
     * @return Transaction or null
     */
    public static Transaction findTransaction(String TXID, List<Block> blockchain) {
        for(int i = 0; i < blockchain.size(); i++) {
            Block block = blockchain.get(i);
            Transaction trans = block.getTransactions().stream().filter(transaction -> transaction.getTXID().equals(TXID)).findAny().orElse(null);
            if(trans != null) return trans;
        }
        return null;
    }

    public static boolean verifyTransaction(Transaction transaction) {
        // Check if the transaction was modified
        if(!transaction.getTXID().equals(
                generateTransactionId(transaction.getInputs(), transaction.getOutputs(), transaction.getSender(), transaction.getReceiver(), transaction.getValue()))) {
            System.out.println("Received block has an invalid TXID!");
            return false;
        }

        // Validate the transaction inputs
        if(!verifyTransactionInputs(transaction.getSender(),transaction.getInputs(), transaction.getTXID())) {
            System.out.println("");
            return false;
        }

        // the value of TXI's must be equivalent to the value of TXO's
        float txInTotalValue = transaction.getInputs().stream().map(input -> {

        }).reduce(0, (a,b) -> a+b);

        return true;
    }
}
