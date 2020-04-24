package com.chain.api.core.Block;

import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Net.CNode;
import com.chain.api.core.Net.CreateBlockThread;
import com.chain.api.core.Net.MiningTask;
import com.chain.api.core.Net.NetUtil;
import com.chain.api.core.Transaction.*;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BlockUtil {

    public static boolean isBlockValid(Block block, List<Block> blockchain, int difficulty, int blockHeight) {

        Objects.requireNonNull(block, "Block is null!");
        Objects.requireNonNull(block, "Blockchain list is null!");

        // Reject if duplicate of block we have in any of the three categories
        for(int i = 0 ; i < blockchain.size(); i++) {
            Block currBlock = blockchain.get(i);
            if(block.getHash().equals(currBlock.getHash())) {
                System.out.println("Duplicate block!");
                return false;
            }
        }

        // Transaction list must be non-empty
        if(block.getTransactions().isEmpty()) {
            System.out.println("Transaction list must be non-empty!");
            return false;
        }

        // Block hash must satisfy claimed nBits proof of work
        // and check that nBits value matches the difficulty rules of the block
        if(!isBlockMined(block, difficulty) || !isBlockMined(block, block.getDifficultyTarget())) {
            System.out.println("Block hash must satisfy claimed nBits proof of work!");
            return false;
        }

        // Block timestamp must not be more than two hours in the future
        Long timestamp = block.getTimestamp();
        Long currTime = new Date().getTime();
        if(TimeUnit.MILLISECONDS.toHours(timestamp - currTime) > 2) {
            System.out.println("Block timestamp must not be more than two hours in the future");
            return false;
        }

        // First transaction must be coinbase (i.e. only 1 input, with hash=0, n=-1), the rest must not be
        Transaction coinbase = block.getTransactions().get(0);
        if(!TransactionUtil.verifyCoinbaseTransaction(coinbase,blockHeight)) {
            System.out.println("Failed verifying coinbase!");
            return false;
        }

        // Verify Merkle hash
        if(!BlockUtil.generateMerkleRoot(block.getTransactions()).equals(block.getMerkleRoot())) {
            System.out.println("Merkle hash is invalid!");
            return false;
        }

        // A timestamp is accepted as valid if it is greater than the median timestamp of previous 11 blocks
        // and less than the network-adjusted time + 2 hours.
        timestamp = 0l;
        int count = 0;
        for(int i = blockchain.size() - 1; i >= 0 && count < 11; i--, count++) {
           timestamp += blockchain.get(i).getTimestamp();
        }

        if(block.getTimestamp() < (timestamp / count)) {
            System.out.println("Block's timestamp must be greater than the median timestamp of previous 11 blocks!");
            return false;
        }

        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, 2); // adds two hours

        if(block.getTimestamp() > cal.getTime().getTime()) { // first call to getTime returns a Date object 2 hours in the future
            System.out.println("Block's timestamp must be less than the network-adjusted time + 2 hours!");
            return false;
        }

        // other validations

        return true;
    }

    /*
    public static boolean validateBlockchain(List<Block> blockchain) {

        if(blockchain.isEmpty()) {
            System.out.print("Blockchain can't be empty!");
            return false;
        }
        // verify the genesis block


        return true;
    }
    */


    public static boolean isChainValid(List<Block> blockchain) {

        Objects.requireNonNull(blockchain, "List of blocks is null!");

        Block currBlock;
        Block prevBlock;
        for(int i = 1; i < blockchain.size(); i++) {
            currBlock = blockchain.get(i);
            prevBlock = blockchain.get(i - 1);

            // Verify indexes
            if(currBlock.getIndex() - 1 != prevBlock.getIndex()) {
                System.out.println("Indexes don't match!" + currBlock.getIndex() + " : " + prevBlock.getIndex());
                return false;
            }
            // Verify previous block's hash
            if(currBlock.getPreviousHash() != prevBlock.getHash()) {
                System.out.println("Hashes don't match!" + currBlock.getIndex() + " : " + prevBlock.getIndex());
                return false;
            }
            // Verify current block's hash
            String currHash = currBlock.getHash();
            generateHash(currBlock);
            if(currBlock.getHash() != currHash) {
                System.out.println("Hash doesn't match!" + currBlock.getIndex());
                return false;
            }
        }

        return true;
    }

    /**
     * @param origBlockchain
     * @param newBlockchain
     * @return true if first chain has more cumulative difficulty
     */
    public static boolean compareChains(List<Block> origBlockchain, List<Block> newBlockchain) {

        Objects.requireNonNull(origBlockchain, "Original list of blocks is null!");
        Objects.requireNonNull(newBlockchain, "New list of blocks is null!");

        int origSize = 0;
        int newSize = 0;
        for(int i = 0; i < origBlockchain.size();i++) {
            origSize += Math.pow(2, origBlockchain.get(i).getDifficultyTarget());
        }
        for(int i = 0; i < newBlockchain.size();i++) {
            origSize += Math.pow(2, newBlockchain.get(i).getDifficultyTarget());
        }
        return (origSize > newSize);
    }

    public static boolean isBlockMined(Block block, int difficulty) {

        Objects.requireNonNull(block, "Block is null!");

        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        if(!block.getHash().substring( 0, difficulty).equals(hashTarget)) {
            System.out.println("Block hash must satisfy claimed nBits proof of work!");
            return false;
        }
        return true;
    }

    public static String generateMerkleRoot(List<Transaction> transactions) {

        if(transactions == null || transactions.size() == 0) {
            //System.out.println("Invalid transactions list");
            return "";
        }
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getTXID());
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(CryptoUtil.encryptSha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }

    public static void generateHash(Block block) {

        Objects.requireNonNull(block, "Block is null!");

        block.setHash(CryptoUtil.encryptSha256(block.getPreviousHash()
                + block.getIndex()
                + block.getTimestamp()
                + block.getMerkleRoot()
                + block.getNonce()));
    }

    public static  MiningTask generateEmptyBlock(Block prevBlock,PublicKey nodeOwner, List<UTXO> utxos, List<Transaction> unconfirmedTransactions, List<Block> blockchain, List<CNode> vNodes) {

        Objects.requireNonNull(nodeOwner, "Public Key is null!");
        Objects.requireNonNull(utxos, "UTXO list is null!");
        Objects.requireNonNull(unconfirmedTransactions, "Unconfirmed transaction is null!");
        Objects.requireNonNull(blockchain, "The list of block's is null!");
        Objects.requireNonNull(vNodes, "The list of Peers is null!");

        MiningTask miningTask = generateBlockWithTransaction(
                prevBlock,
                nodeOwner,
                utxos,
                blockchain.size(),
                null,
                unconfirmedTransactions,
                blockchain,
                vNodes);

        return miningTask;
    }

    public static MiningTask generateBlockWithTransaction(Block prevBlock,PublicKey nodeOwner, List<UTXO> utxos, int blockHeight, List<Transaction> transactions, List<Transaction> unconfirmedTransactions, List<Block> blockchain, List<CNode> vNodes) {

        Objects.requireNonNull(nodeOwner, "Public Key is null!");
        Objects.requireNonNull(blockchain, "The list of block's is null!");

        CreateBlockThread createBlockThread = new CreateBlockThread(prevBlock, nodeOwner, utxos, blockHeight, transactions, unconfirmedTransactions, blockchain, vNodes);
        Thread mineBlockThread = new Thread(createBlockThread);
        mineBlockThread.start();

        return new MiningTask(mineBlockThread, createBlockThread);

    }

    // Genesis block will be hardcoded
    public static MiningTask generateGenesisBlock(PublicKey nodeOwner, List<Block> blockchain, List<CNode> vNodes) {

        Objects.requireNonNull(nodeOwner, "Public Key is null!");
        Objects.requireNonNull(blockchain, "The list of block's is null!");

        MiningTask miningTask = generateBlockWithTransaction(
                null,
                nodeOwner,
                null,
                0,
                null,
                null,
                blockchain,
                vNodes);

        return miningTask;
    }


    public static void handleBlock(Block block, List<Block> blockchain, List<UTXO> unspentTransactionOutputs, UnconfirmedTransactions unconfirmedTransactions, List<MiningTask> miningTaskList, PublicKey publicKey, List<CNode> vNodes) {

        Objects.requireNonNull(block, "received null block!");
        Objects.requireNonNull(blockchain, "The list of block's is null!");
        Objects.requireNonNull(unspentTransactionOutputs, "UTXO list is null!");
        Objects.requireNonNull(unconfirmedTransactions, "Unconfirmed transaction is null!");
        Objects.requireNonNull(miningTaskList, "The list of mining tasks is null!");
        Objects.requireNonNull(publicKey, "Public Key is null!");
        Objects.requireNonNull(vNodes, "The list of Peers is null!");

        // 1. validate the received block
        int blockHeight = blockchain.size();

        if(!BlockUtil.isBlockValid(block,blockchain,block.getDifficultyTarget(),blockHeight)) {
            System.out.println("Received block is invalid!");
            return;
        }

        // Each miner can choose which transactions are included in or exempted from a block
        // Exempt only the transactions which are invalid
        List<Transaction> validTransactions = new ArrayList<>();

        // This step is not really not necessary, a SPV can be used
        for(int i = 1; i < block.getTransactions().size(); i++) {
            Transaction transaction = block.getTransactions().get(i);

            if(!TransactionUtil.verifyTransaction(transaction, blockchain, blockHeight)) {
                System.out.println("Failed tx " + i + " check!");
            }
            else {
                validTransactions.add(transaction);
            }
        }

        // 3. add the block to the blockchain

        // if prevHash doesnt match our latest block then we have to query the peer for all his blockchain

        // If multiple blocks are mined at the same time
        // Check if prev block (matching prev hash) is in main branch or side branches.
        // If not, add this to orphan blocks, then query peer we got this from for 1st missing orphan block in prev chain; done with block
        // TO DO

        if(validTransactions.size() >= 1 && validTransactions.size() != block.getTransactions().size()) {
            // add the valid transactions to our current block
            validTransactions.stream().forEach(transaction ->  TransactionUtil.handleTransaction(
                    transaction,
                    blockchain,
                    unspentTransactionOutputs,
                    unconfirmedTransactions,
                    miningTaskList,
                    publicKey,
                    vNodes));
        }
        else {
            // add the validated block to the tree
            blockchain.add(block);
            TransactionUtil.updateUtxos(block.getTransactions(),unspentTransactionOutputs);

            // remove the unconfirmed transactions
            TransactionUtil.updateUnconfirmedTransactions(unspentTransactionOutputs,unconfirmedTransactions.getTransactions());

            // 3. send it to all the known peers
            Thread thread = new Thread(() -> NetUtil.sendBlockToAllPeers(block, vNodes));
            thread.start();
        }
    }

    /*
    public void handleBlockchain(List<Block> receivedBlochain) {
        Objects.requireNonNull(receivedBlochain, "received null blockchain!");
    }
    */

}
