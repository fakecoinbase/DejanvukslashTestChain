package com.chain.api.core.Transaction;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Crypto.CryptoUtil;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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
            // find the UTXO it refferences
            UTXO tempUtxo = userUTXOs.stream().filter(utxo -> utxo.getPreviousTx().equals(txi.getPreviousTx()) && utxo.getIndex() == txi.getIndex()).findAny().orElse(null);
            if(tempUtxo == null) {
                System.out.println("Input doesn't match output!");
                return false;
            }
            try {
                if(!CryptoUtil.getStringFromKey(tempUtxo.getOwner()).equals(CryptoUtil.getStringFromKey(CryptoUtil.DerivePubKeyFromPrivKey((BCECPrivateKey) sender)))) {
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
            if(!CryptoUtil.verifyECDSASig(owner, TXID, CryptoUtil.hexStringToByteArray(txi.getSignature()))) {
                System.out.println("The transaction input with index: " + i + " was modified!");
                return false;
            }
        }
        return true;
    }

    /**
     * @param blockHeight index in the blockchain the block is found
     * Generate's the transaction id TXID which is the SHA256 encrypted value of the inputs,outputs,sender,receiver,value and a random nonce to avoid identical hashes
     */
    public static String generateTransactionId(List<TransactionInput> inputs, List<TransactionOutput> outputs, String sender, String receiver, float value, Integer blockHeight) {
        String txIn = inputs.stream().map( TXI -> TXI.getPreviousTx() + TXI.getIndex()).reduce("", (subtotal, element) -> subtotal + element);
        String txOut = outputs.stream().map( TXO -> TXO.getTo() + Float.toString(TXO.getValue())).reduce("", (subtotal, element) -> subtotal + element);
        String TXID = CryptoUtil.encryptSha256(
                   sender +
                        receiver +
                        Float.toString(value) +
                        txIn +
                        txOut +
                        Integer.toString(blockHeight));
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
    public static void updateUtxos(List<Transaction> transactions, List<UTXO> currentUTXOs) {
        // Retrieve all the TXO's from the received block as UTXO
        List<UTXO> blocksTXOs = new ArrayList<>(); // the UTXO's from the received block
        transactions.stream().forEach(transaction -> {
            int i = 0;
            List<TransactionOutput> outputs = transaction.getOutputs();
            while(i < outputs.size()) {
                TransactionOutput to = outputs.get(i);
                blocksTXOs.add(new UTXO(transaction.getTXID(), i++, to.getTo(), to.getValue()));
            }
        });

        // check which currentUTXOs and blocksUTXOs are consumed by the transactions from the received block

        // 1.get all the new spent TXO's referenced by the new TXI's
        List<UTXO> consTXOs =  new ArrayList<>();
        transactions.stream().forEach(transaction -> {
            transaction.getInputs().stream().forEach(input -> {
                consTXOs.add(new UTXO(input.getPreviousTx(),input.getIndex(), null, 0));});});

        // 2.remove the consumed UTXO's from the currentUTXO's
        for(int i = 0; i < currentUTXOs.size(); i++) {
            UTXO utxo = currentUTXOs.get(i);
            for(int j = 0; j < consTXOs.size(); j++) {
                UTXO spentUTXO = consTXOs.get(j);
                if(utxo.getPreviousTx() == spentUTXO.getPreviousTx() && utxo.getIndex() == spentUTXO.getIndex()) {
                    currentUTXOs.remove(i);
                    i--;
                    consTXOs.remove(j); // an input can only reference a TXO once
                    break;
                }
            }
        }

        // 3. remove the consumed TXO's from the blocksTXOs

        for(int i = 0; i < blocksTXOs.size(); i++) {
            UTXO utxo = blocksTXOs.get(i);
            for(int j = 0; j < consTXOs.size(); j++) {
                UTXO spentUTXO = consTXOs.get(j);
                if(utxo.getPreviousTx() == spentUTXO.getPreviousTx() && utxo.getIndex() == spentUTXO.getIndex()) {
                    blocksTXOs.remove(i);
                    i--;
                    consTXOs.remove(j);
                    break;
                }
            }
        }

        // 4. add the new UTXO's blocksTXO's to the currentUTXOs
        currentUTXOs.addAll(blocksTXOs);
    }

    /**
     *
     * update's the internal list and database of UTXO's
     * used whenever a new transaction is received from a peer
     * @param transaction
     * @param currentUTXOs the UTXO's of the blockchain prior to the new received block
     * @return the new list of unspent transaction outputs
     */
    public static void updateUtxos(Transaction transaction, List<UTXO> currentUTXOs) {
        // Retrieve all the TXO's from the received transaction as UTXO
        List<UTXO> blocksTXOs = new ArrayList<>();
        List<TransactionOutput> outputs = transaction.getOutputs();
        for(int i = 0;i < outputs.size(); ) {
            TransactionOutput to = outputs.get(i);
            blocksTXOs.add(new UTXO(transaction.getTXID(), i++, to.getTo(), to.getValue()));
        }

        // check which currentUTXOs and blocksUTXOs are consumed by the received transaction

        // 1.get all the new spent TXO's referenced by the new TXI's
        List<UTXO> consTXOs =  new ArrayList<>();
        transaction.getInputs().stream().forEach(input -> { consTXOs.add(new UTXO(input.getPreviousTx(),input.getIndex(), null, 0));});

        // 2.remove the consumed UTXO's from the currentUTXO's
        for(int i = 0; i < currentUTXOs.size(); i++) {
            UTXO utxo = currentUTXOs.get(i);
            for(int j = 0; j < consTXOs.size(); j++) {
                UTXO spentUTXO = consTXOs.get(j);
                if(utxo.getPreviousTx() == spentUTXO.getPreviousTx() && utxo.getIndex() == spentUTXO.getIndex()) {
                    currentUTXOs.remove(i);
                    i--;
                    consTXOs.remove(j); // an input can only reference a TXO once
                    break;
                }
            }
        }

        // 3. remove the consumed TXO's from the blocksTXOs

        for(int i = 0; i < blocksTXOs.size(); i++) {
            UTXO utxo = blocksTXOs.get(i);
            for(int j = 0; j < consTXOs.size(); j++) {
                UTXO spentUTXO = consTXOs.get(j);
                if(utxo.getPreviousTx() == spentUTXO.getPreviousTx() && utxo.getIndex() == spentUTXO.getIndex()) {
                    blocksTXOs.remove(i);
                    i--;
                    consTXOs.remove(j);
                    break;
                }
            }
        }

        // 4. add the new UTXO's blocksTXO's to the currentUTXOs
        currentUTXOs.addAll(blocksTXOs);
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

    public static boolean verifyTransaction(Transaction transaction,List<Block> blockchain, Integer blockHeight) {
        // Check if the transaction was modified
        if(!transaction.getTXID().equals(
                generateTransactionId(transaction.getInputs(),
                        transaction.getOutputs(),
                        CryptoUtil.getStringFromKey(transaction.getSender()),
                        CryptoUtil.getStringFromKey(transaction.getReceiver()),
                        transaction.getValue(),
                        blockHeight))) {
            System.out.println("Transaction has an invalid id!");
            return false;
        }

        // Validate the transaction inputs
        if(!verifyTransactionInputs(transaction.getSender(),transaction.getInputs(), transaction.getTXID())) {
            System.out.println("");
            return false;
        }

        // the value of TXI's must be equivalent to the value of TXO's
        float txInTotalValue = transaction.getInputs().stream().map(input -> findTransactionOutput(input,blockchain).getValue()).reduce(0f, (a,b) -> a+b);

        float txOutTotalValue = transaction.getOutputs().stream().map(output -> output.getValue()).reduce(0f, (a,b) -> a+b);

        if(txInTotalValue != txOutTotalValue) {
            System.out.println("Outputs and Inputs total values dont match!");
            return false;
        }

        // other validations


        return true;
    }

    public static boolean verifyCoinbaseTransaction(Transaction coinbasetransaction,Integer blockHeight) {
        // Check if the coinbase transaction was modified
        if(!coinbasetransaction.getTXID().equals(
                generateTransactionId(
                        coinbasetransaction.getInputs(),
                        coinbasetransaction.getOutputs(),
                        (coinbasetransaction.getSender() == null) ? "" : CryptoUtil.getStringFromKey(coinbasetransaction.getSender()),
                        CryptoUtil.getStringFromKey(coinbasetransaction.getReceiver()),
                        coinbasetransaction.getValue(),
                        blockHeight))) {
            System.out.println("Coinbase transaction has an invalid TXID!");
            return false;
        }

        // Reward transaction has no inputs
        if(!coinbasetransaction.getInputs().isEmpty()) {
            System.out.println("Coinbase transaction should have no inputs!");
            return false;
        }

        // Reward transaction has one output
        if(coinbasetransaction.getOutputs().size() != 1) {
            System.out.println("Coinbase transaction has more than one output!");
            return false;
        }

        // other coinbase validations

        return true;
    }

    /**
     *
     * @param owner
     * @param utxos
     * @return user's UTXO's
     */
    public static List<UTXO> getUserUtxos(PublicKey owner,List<UTXO> utxos) {
        return utxos.stream().filter(utxo -> CryptoUtil.getStringFromKey(utxo.getOwner()).equals(CryptoUtil.getStringFromKey(owner))).collect(Collectors.toList());
    }

    /**
     * Search through the UTXO's for the transactions with the receiver publicKey
     * @param utxos user's utxos
     * @return
     */
    public static float getUsersBalance(List<UTXO> utxos) {
        float value = utxos.stream().map(utxo -> utxo.getValue()).reduce(0f, (a,b) -> a+b);
        return value;
    }

    /**
     * Create a Coinbase transaction with no inputs
     * @param to owner of the miner
     * @param value constant
     * @param utxos
     * @return Transaction or null if the creation failed
     */
    public static Transaction createCoinbaseTransaction(String to, float value, List<UTXO> utxos, Integer blockHeight) {
        Transaction transaction = null;
        try {
            PublicKey toKey = CryptoUtil.getPublicKeyFromString(to);

            // No inputs, so an empty list
            List<TransactionInput> inputs = new ArrayList<>();

            inputs.add(new TransactionInput( // coinbase
                    "0000000000000000000000000000000000000000000000000000000000000000",
                    blockHeight, // The input for the coinbase transaction must contain the block height as the first script data
                    ""));

            // add Transaction Output
            List<TransactionOutput> outputs = new ArrayList<>();
            outputs.add(new TransactionOutput(CryptoUtil.getPublicKeyFromString(to), value));

            transaction = new Transaction(
                    1,
                    (short) 1,
                    generateTransactionId(inputs,outputs,"",to,value, blockHeight),
                    null,
                    toKey,
                    value,
                    inputs,
                    outputs
            );

            // add the new TXO's in UTXO's after we get txid

            for(int i = 0; i < outputs.size(); i++) {
                TransactionOutput tempTo = outputs.get(i);
                utxos.add(new UTXO(transaction.getTXID(),i,tempTo.getTo(),tempTo.getValue()));
            }

            return transaction;

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return transaction;
    }

    /**
     *
     * @param from private key of sender
     * @param to public key of receiver
     * @param value
     * @param utxos
     * @param blockHeight the index of the block from were the transaction is found
     * @return Transaction or null if the creation failed
     */
    public static Transaction createTransaction(String from, String to, float value, List<UTXO> utxos, Integer blockHeight) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        Transaction transaction = null;
        PublicKey fromKey = CryptoUtil.DerivePubKeyFromPrivKey((BCECPrivateKey) CryptoUtil.getPrivateKeyFromString(from));
        PublicKey toKey = CryptoUtil.getPublicKeyFromString(to);

        // get user's specific UTXO
        List<UTXO> usersUtxos = getUserUtxos(fromKey, utxos);

        // find UTXO's that add up to >= value
        List<UTXO> utxostoBeRemoved = new ArrayList<>();
        float utxosToBeRemovedValue = 0;
        for(int i = 0; i < usersUtxos.size(); i++) {
            UTXO tempUtxo = usersUtxos.get(i);
            utxostoBeRemoved.add(tempUtxo);
            utxosToBeRemovedValue += tempUtxo.getValue();
            if(utxosToBeRemovedValue >= value) break;
        }

        // verify if sender has enough funds to send
        if(utxosToBeRemovedValue < value) throw new RuntimeException("Sender does not have enough funds");

        // create the TXI's
        List<TransactionInput> inputs = utxostoBeRemoved.stream().map(utxo -> new TransactionInput(utxo.getPreviousTx(),utxo.getIndex(),"")).collect(Collectors.toList());

        // create the new TXO's
        List<TransactionOutput> outputs = new ArrayList<>();
        outputs.add(new TransactionOutput(toKey,value));

        final float fee = 0f;
        float leftoverValue = utxosToBeRemovedValue - value - fee; // send back to owner leftover coins minus the fee

        //output.add(new TransactionOutput(fromKey, fee)); // the fee

        if(leftoverValue > 0)
            outputs.add(new TransactionOutput(fromKey, leftoverValue));

        // create the transaction and generate transaction ID

        transaction = new Transaction(1,
                (short) 1,
                generateTransactionId(inputs,outputs,CryptoUtil.getStringFromKey(fromKey),CryptoUtil.getStringFromKey(toKey),value, blockHeight),
                fromKey,
                toKey,
                value,
                inputs,
                outputs);

        // add the new TXO's in UTXO's after we got TXID

        for(int i = 0; i < outputs.size(); i++) {
            TransactionOutput tempTo = outputs.get(i);
            utxos.add(new UTXO(transaction.getTXID(),i,tempTo.getTo(),tempTo.getValue()));
        }

        // sign TXI's after adding the new UTXO
        lockTransactionInputs((BCECPrivateKey) CryptoUtil.getPrivateKeyFromString(from),inputs,transaction.getTXID(),utxos);

        // remove the spent UTXO's used as inputs AFTER locking else locking check will fail since the referenced UTXO's by inputs will not be found!
        // OR search the inputs refferences in the blockchain
        for(int i = 0; i < utxos.size();i++) {
            UTXO currentUTXO = utxos.get(i);
            for(int k = 0; k < utxostoBeRemoved.size(); k++) {
                UTXO consumedUTXO = utxostoBeRemoved.get(k);
                if(currentUTXO.getPreviousTx() == consumedUTXO.getPreviousTx() && currentUTXO.getIndex() == consumedUTXO.getIndex()) {
                    utxos.remove(i);
                    i--;
                    break;
                }
            }
        }

        // return the transaction
        return transaction;
    }

}
