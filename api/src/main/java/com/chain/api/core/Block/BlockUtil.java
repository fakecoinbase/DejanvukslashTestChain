package com.chain.api.core.Block;

import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionInput;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UTXO;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BlockUtil {

    public static boolean isBlockValid(Block block, List<Block> blockchain, int difficulty, int blockHeight) {
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

    public static boolean validateBlockchain(List<Block> blockchain) {
        if(blockchain.isEmpty()) {
            System.out.print("Blockchain can't be empty!");
            return false;
        }
        // verify the genesis block


        return true;
    }

    public static boolean isChainValid(List<Block> blockchain) {
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

    private static boolean checkGenesisBlock(Block firstBlock) {
		/*
		Block genesis = new Block(null); // read the default values from a prop file or system variable
		if(genesis != firstBlock) {
			return false;
		}
		return true;
		*/
        return true;
    }

    public static boolean isBlockMined(Block block, int difficulty) {
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        if(!block.getHash().substring( 0, difficulty).equals(hashTarget)) {
            System.out.println("Block hash must satisfy claimed nBits proof of work!");
            return false;
        }
        return true;
    }

    public static String generateMerkleRoot(List<Transaction> transactions) {
        if(transactions.size() == 0) {
            System.out.println("Invalid transactions list");
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
        block.setHash(CryptoUtil.encryptSha256(block.getPreviousHash()
                + block.getIndex()
                + block.getTimestamp()
                + block.getMerkleRoot()
                + block.getNonce()));
    }

    public static void mineBlock(Block block, PublicKey nodeOwner, int blockHeight) {
        block.setTimestamp(new Date().getTime());
        String target = new String(new char[block.getDifficultyTarget()]).replace('\0', '0'); // Create a string with difficulty * "0"
        do {
            generateHash(block);
            if(block.getNonce() >= Integer.MAX_VALUE) {
                // Change the Coinbase transaction's nonce
                Transaction coinbaseTransaction = block.getTransactions().get(0);
                TransactionInput coinbase = coinbaseTransaction.getInputs().get(0);
                coinbase.increaseNonce();
                // Recalculate the TXID of the coinbase transaction
                String coinbaseTxId = TransactionUtil.generateTransactionId(
                        coinbaseTransaction.getInputs(),
                        coinbaseTransaction.getOutputs(),
                        "",
                        CryptoUtil.getStringFromKey(coinbaseTransaction.getReceiver()),
                        coinbaseTransaction.getValue(),
                        blockHeight);
                coinbaseTransaction.setTXID(coinbaseTxId);

                // generate the merkle root again
                block.setMerkleRoot(BlockUtil.generateMerkleRoot(block.getTransactions()));

                // reset the block's nonce
                block.setNonce(0);
            }
            block.setNonce(block.getNonce() + 1);
        }
        while (!block.getHash().substring(0, block.getDifficultyTarget()).equals(target));
    }

    public static Block generateEmptyBlock(Block prevBlock,PublicKey nodeOwner, List<UTXO> utxos, int blockHeight) {
        Block blockToBeMined = new Block(prevBlock, null);
        // add reward/coinbase  transaction to the miner's wallet
        Transaction coinbaseTransaction = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(nodeOwner),5, utxos, blockHeight);
        blockToBeMined.addTransaction(coinbaseTransaction);

        return blockToBeMined;
    }

    public static Block generateBlockWithTransaction(Block prevBlock,PublicKey nodeOwner, List<UTXO> utxos, int blockHeight, List<Transaction> transactions) {
        Block blockToBeMined = new Block(prevBlock, null);

        // add reward/coinbase  transaction to the miner's wallet
        Transaction coinbaseTransaction = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(nodeOwner),5, utxos, blockHeight);
        blockToBeMined.addTransaction(coinbaseTransaction);
        blockToBeMined.getTransactions().addAll(transactions);

        // generate the merkle root
        blockToBeMined.setMerkleRoot(generateMerkleRoot(transactions));

        BlockUtil.mineBlock(blockToBeMined, nodeOwner,blockHeight);

        // Add the block to the database

        return blockToBeMined;
    }

    public static Block generateGenesisBlock(PublicKey nodeOwner, int value) {
        Block block = new Block(null, null);
        Transaction coinbaseTransaction = TransactionUtil.createCoinbaseTransaction(
                CryptoUtil.getStringFromKey(nodeOwner),
                value,
                null, // genesis block coinbase transaction can't be used
                0);
        block.addTransaction(coinbaseTransaction);

        BlockUtil.mineBlock(block, nodeOwner, 0);

        return block;
    }
}
