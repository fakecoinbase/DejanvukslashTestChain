package com.chain.api.core.Transaction;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Net.CNode;
import com.chain.api.core.Net.MiningTask;
import com.chain.api.core.Net.NetUtil;
import com.chain.api.core.Transaction.exceptions.CreateTransactionException;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
     * @param utxos current UTXO's list
     */
    public static boolean lockTransactionInputs(PrivateKey sender, List<TransactionInput> inputs, String TXID, List<UTXO> utxos) {

        Objects.requireNonNull(sender, "Private Key is null!");
        Objects.requireNonNull(inputs, "TXI list is null!");
        Objects.requireNonNull(utxos, "UTXO list is null!");


        for(int i = 0; i < inputs.size(); i++) {
            TransactionInput txi = inputs.get(i);
            // find the UTXO it references
            UTXO tempUtxo = utxos.stream().filter(utxo -> utxo.getPreviousTx().equals(txi.getPreviousTx()) && utxo.getIndex() == txi.getIndex()).findAny().orElse(null);
            if(tempUtxo == null) {
                throw new CreateTransactionException("Input doesn't match output!");
            }
            try {
                if(!CryptoUtil.getStringFromKey(tempUtxo.getOwner()).equals(CryptoUtil.getStringFromKey(CryptoUtil.DerivePubKeyFromPrivKey((BCECPrivateKey) sender)))) {
                    throw new CreateTransactionException("The owner is different!");
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

        Objects.requireNonNull(owner, "Public Key is null!");
        Objects.requireNonNull(inputs, "TXI list is null!");

        for(int i = 0; i < inputs.size(); i++) {
            TransactionInput txi = inputs.get(i);
            if(!CryptoUtil.verifyECDSASig(owner, TXID, CryptoUtil.hexStringToByteArray(txi.getSignature()))) {
                System.out.println("The transaction input with index: " + i + " was modified!");
            }
        }
        return true;
    }

    /**
     * @param blockHeight index in the blockchain the block is found
     * Generate's the transaction id TXID which is the SHA256 encrypted value of the inputs,outputs,sender,receiver,value and a random nonce to avoid identical hashes
     */
    public static String generateTransactionId(List<TransactionInput> inputs, List<TransactionOutput> outputs, String sender, String receiver, float value, Integer blockHeight) {

        Objects.requireNonNull(inputs, "TXI list is null!");
        Objects.requireNonNull(outputs, "TXO list is null!");

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

        Objects.requireNonNull(transactions, "Transactions list is null!");

        Objects.requireNonNull(currentUTXOs, "UTXO list is null!");

        /*
        transactions.stream().forEach(transaction -> {
            updateUtxos(transaction,currentUTXOs,unconfirmedTransactions);
        });
        */

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
        /* step removed because unconfirmed transactions aren't supported atm
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

         */

        // 4. add the new UTXO's blocksTXO's to the currentUTXOs
        currentUTXOs.addAll(blocksTXOs);
    }


    /*
    public static void updateUtxos(Transaction transaction, List<UTXO> currentUTXOs, UnconfirmedTransactions unconfirmedTransactions) {
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
        /*
        // this step is no longer required as the new txo's are not referenced by the transactions txi's,this step was only required when we had an array of transactions and we processed them all at once
        // the name blocksTXO's should be changed to transactionsTXOs
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

        // 5. add the Transaction to unconfirmed transactions

        unconfirmedTransactions.addUnconfirmedTransactions(transaction);
    }
    */


    /**
     * find the specific TXO refferenced by the TI in the blockchain
     * @param TI
     * @param blockchain
     * @return TXO or null
     */
    public static TransactionOutput findTransactionOutput(TransactionInput TI, List<Block> blockchain) {

        Objects.requireNonNull(TI, "Transaction input is null!");
        Objects.requireNonNull(blockchain, "The list of block's is null!");

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
     * Find the transaction with the specific transaction id in the memory pool
     * @param TXID
     * @param unconfirmedTransactions
     * @return
     */
    public static Transaction findUnconfirmedTransaction(String TXID, List<Transaction> unconfirmedTransactions) {

        Objects.requireNonNull(unconfirmedTransactions, "Unconfirmed transactions list is null!");

        Transaction trans = unconfirmedTransactions.stream().filter(transaction -> transaction.getTXID().equals(TXID)).findAny().orElse(null);

        return trans;

    }

    /**
     * Find the transaction with the specific transaction id in the blockchain
     * @param TXID
     * @param blockchain
     * @return Transaction or null
     */
    public static Transaction findTransaction(String TXID, List<Block> blockchain) {

        Objects.requireNonNull(blockchain, "The list of block's is null!");

        for(int i = 0; i < blockchain.size(); i++) {
            Block block = blockchain.get(i);
            Transaction trans = block.getTransactions().stream().filter(transaction -> transaction.getTXID().equals(TXID)).findAny().orElse(null);
            if(trans != null) return trans;
        }
        return null;
    }

    /*
    Only to be used here,normally the BlockResponse would be generated after block was mined
    and its transactions updated with the corresponding block' hash
     */
    public static String findBlockWithTx(String txid, List<Block> blockchain) {

        Objects.requireNonNull(blockchain, "The list of block's is null!");

        for(int i = 0; i < blockchain.size(); i++) {
            Block block = blockchain.get(i);
            Transaction trans = block.getTransactions().stream().filter(transaction -> transaction.getTXID().equals(txid)).findAny().orElse(null);
            if(trans != null) return block.getHash();
        }
        return "";
    }

    /**
     * If the transaction isn't found in the unconfirmed transaction memory pool than it is considered varified
     * @param tx
     * @param unconfirmedTransactions
     * @return
     */
    public static boolean isVerified(Transaction tx,List<Transaction> unconfirmedTransactions) {

        Objects.requireNonNull(unconfirmedTransactions, "Unconfirmed transactions list is null!");

        String TXID = tx.getTXID();

        Transaction unconfirmedTransaction = unconfirmedTransactions.stream().filter(utx -> utx.getTXID().equals(TXID)).findAny().orElse(null);

        return unconfirmedTransaction == null;
    }

    public static boolean verifyTransaction(Transaction transaction,List<Block> blockchain, Integer blockHeight) {

        Objects.requireNonNull(transaction, "Transaction is null!");
        Objects.requireNonNull(blockchain, "The list of block's is null!");

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
            System.out.println("Invalid transaction inputs!");
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

        // Note: verifying that none of the transaction's inputs have been previously spent is not necessary

        return true;
    }

    public static boolean verifyCoinbaseTransaction(Transaction coinbase,Integer blockHeight) {

        Objects.requireNonNull(coinbase, "Coinbase transaction is null!");

        // Check if the coinbase transaction was modified
        if(!coinbase.getTXID().equals(
                generateTransactionId(
                        coinbase.getInputs(),
                        coinbase.getOutputs(),
                        (coinbase.getSender() == null) ? "" : CryptoUtil.getStringFromKey(coinbase.getSender()),
                        CryptoUtil.getStringFromKey(coinbase.getReceiver()),
                        coinbase.getValue(),
                        blockHeight))) {
            System.out.println("Coinbase transaction has an invalid TXID!");
            return false;
        }

        // Reward transaction has no inputs
        if(coinbase.getInputs().size() != 1) {
            System.out.println("Coinbase should have 1 input only!");
            return false;
        }

        // Reward transaction has one output
        if(coinbase.getOutputs().size() != 1) {
            System.out.println("Coinbase transaction has more than one output!");
            return false;
        }

        // other coinbase validations

        if(!coinbase.getInputs().get(0).getPreviousTx().equals("0000000000000000000000000000000000000000000000000000000000000000")) {
            return false;
        }


        return true;
    }

    /**
     *
     * @param owner
     * @param utxos
     * @return user's UTXO's
     */
    public static List<UTXO> getUserUtxos(PublicKey owner,List<UTXO> utxos) {

        Objects.requireNonNull(owner, "Public Key is null!");
        Objects.requireNonNull(utxos, "UTXO list is null!");

        return utxos.stream().filter(utxo -> CryptoUtil.getStringFromKey(utxo.getOwner()).equals(CryptoUtil.getStringFromKey(owner))).collect(Collectors.toList());
    }

    /**
     * Search through the UTXO's for the transactions with the receiver publicKey
     * @param utxos user's utxos
     * @return
     */
    public static float getUsersBalance(List<UTXO> utxos) {
        Objects.requireNonNull(utxos, "UTXO list is null!");

        float value = utxos.stream().map(utxo -> utxo.getValue()).reduce(0f, (a,b) -> a+b);
        return value;
    }

    /**
     * Create a Coinbase transaction with no inputs
     * @param to owner of the miner
     * @param value constant
     * @return Transaction or null if the creation failed
     */
    public static Transaction createCoinbaseTransaction(String to, float value, Integer blockHeight) {
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
            /* step removed as utxo's are updated once tx is mined
            if(utxos != null) {
                for(int i = 0; i < outputs.size(); i++) {
                    TransactionOutput tempTo = outputs.get(i);
                    utxos.add(new UTXO(transaction.getTXID(),i,tempTo.getTo(),tempTo.getValue()));
                }
            }
            */

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
    public static Transaction createTransaction(String from, String to, float value, List<UTXO> utxos,List<Transaction> unconfirmedTransactions, Integer blockHeight)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {

        Objects.requireNonNull(utxos, "UTXO list is null!");
        Objects.requireNonNull(unconfirmedTransactions, "Unconfirmed transactions list is null!");

        Transaction transaction = null;
        PublicKey fromKey = CryptoUtil.DerivePubKeyFromPrivKey((BCECPrivateKey) CryptoUtil.getPrivateKeyFromString(from));
        PublicKey toKey = CryptoUtil.getPublicKeyFromString(to);

        // get user's specific UTXO
        List<UTXO> usersUtxos = getUserUtxos(fromKey, utxos);

        // get only the UTXO's that are not used by user's unconfirmed transactions
        // in this way a user can make multiple transactions without having to wait for someone to mine the previous one

        List<TransactionInput> unnconfirmedTxsTxins = new ArrayList<>(); // list of all the inputs of our unconfirmed transaction
        unconfirmedTransactions.stream().forEach(unconfirmedTx -> unnconfirmedTxsTxins.addAll(unconfirmedTx.getInputs()));

        for(int i = 0; i < usersUtxos.size(); i++) {
            UTXO utxo = usersUtxos.get(i);
            for(int j = 0; j < unnconfirmedTxsTxins.size(); j++) {
                TransactionInput txi = unnconfirmedTxsTxins.get(j);
                if(utxo.getPreviousTx().equals(txi.getPreviousTx()) && utxo.getIndex().equals(txi.getIndex())) {
                    usersUtxos.remove(i);
                    i--;
                    // the txi can also be removed for faster speed as it can only refer to a single utxo
                    break;
                }
            }
        }

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
        if(utxosToBeRemovedValue < value) throw new CreateTransactionException("Sender does not have enough funds!");

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
                generateTransactionId(inputs,outputs,CryptoUtil.getStringFromKey(fromKey),CryptoUtil.getStringFromKey(toKey),utxosToBeRemovedValue, blockHeight),
                fromKey,
                toKey,
                utxosToBeRemovedValue, // TOTAL VALUE
                inputs,
                outputs);

        // sign TXI's
        lockTransactionInputs((BCECPrivateKey) CryptoUtil.getPrivateKeyFromString(from),inputs,transaction.getTXID(),utxos);

        // return the transaction
        return transaction;
    }


    /**
     * Simply for every unconfirmed transaction check if the inputs refer to existing utxo's, if not remove the unconfirmed transaction.
     * @param utxos
     * @param unconfirmedTransactions
     */
    public static void updateUnconfirmedTransactions(List<UTXO> utxos, List<Transaction> unconfirmedTransactions) {

        Objects.requireNonNull(utxos, "UTXO list is null!");
        Objects.requireNonNull(unconfirmedTransactions, "Unconfirmed transactions list is null!");

        for(int i = 0; i < unconfirmedTransactions.size(); i++) {
            Transaction currentTx = unconfirmedTransactions.get(i);
            List<TransactionInput> unconfirmedTxInputs = currentTx.getInputs();
            for(int j = 0; j < unconfirmedTxInputs.size(); j++) {
                TransactionInput txi = unconfirmedTxInputs.get(j);
                boolean found = false;
                for(int k = 0; k < utxos.size(); k++) {
                    UTXO utxo = utxos.get(k);
                    if(txi.getPreviousTx().equals(utxo.getPreviousTx()) && txi.getIndex().equals(utxo.getIndex())) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    unconfirmedTransactions.remove(i);
                    i--;
                    break;
                }
            }
        }
    }


    /**
     *
     * @param transaction
     * @param blockchain
     * @param unspentTransactionOutputs
     * @param unconfirmedTransactions
     * @param publicKey
     * @param vNodes
     */
    public static void handleTransaction(
            Transaction transaction,
            List<Block> blockchain,
            List<UTXO> unspentTransactionOutputs,
            UnconfirmedTransactions unconfirmedTransactions,
            List<MiningTask> miningTaskList,
            PublicKey publicKey,
            List<CNode> vNodes,
            AtomicInteger difficultyTarget) {

        Objects.requireNonNull(transaction, "Transaction is null!");
        Objects.requireNonNull(blockchain, "The list of block's is null!");
        Objects.requireNonNull(unspentTransactionOutputs, "UTXO list is null!");
        Objects.requireNonNull(unconfirmedTransactions, "Unconfirmed transaction is null!");
        Objects.requireNonNull(miningTaskList, "The list of mining tasks is null!");
        Objects.requireNonNull(publicKey, "Public Key is null!");
        Objects.requireNonNull(vNodes, "The list of Peers is null!");

        final Integer MAX_BLOCK_SIZE = 5;

        if(!verifyTransaction(transaction, blockchain, blockchain.size())) {
            throw new CreateTransactionException("The transaction is invalid!");
        }

        unconfirmedTransactions.getTransactions().add(transaction);

        // if the block is full mine and send it to all peers
        if(unconfirmedTransactions.getTransactions().size() >= MAX_BLOCK_SIZE - 1) {
            // generate a new block with the unconfirmed transactions
            MiningTask miningTask= BlockUtil.generateBlockWithTransaction(
                    blockchain.get(blockchain.size() - 1),
                    publicKey,
                    unspentTransactionOutputs,
                    blockchain.size(),
                    unconfirmedTransactions.copyUnconfirmedTransactions(),
                    unconfirmedTransactions.getTransactions(),
                    blockchain,
                    vNodes,
                    difficultyTarget
            );
            miningTaskList.add(miningTask);
        }

        // send the transaction to all of our known peers
        Thread thread = new Thread(() -> NetUtil.sendTransactionToAllPeers(transaction, vNodes));
        thread.start();
    }


}
