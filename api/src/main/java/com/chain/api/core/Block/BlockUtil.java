package com.chain.api.core.Block;

import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UTXO;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlockUtil {

    public static boolean isBlockValid() {
        return true;
    }

    /**
     * Whenever we receive a new block from a peer we must check the unconfirmed transactions
     */
    public static void updateUnconfirmedTransactions() {
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

    public static boolean isBlockMined(Block block) {
        int difficulty = 5; // read dificulty from a central config server
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        if(!block.getHash().substring( 0, difficulty).equals(hashTarget)) {
            System.out.println("Block wasn't mined!");
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
                //+ block.getMerkleRoot()
                + block.getNonce()));
    }

    public static void mineBlock(Block block, PublicKey nodeOwner, List<UTXO> utxos, int blockHeight) {
        block.setTimestamp(new Date().getTime());
        String target = new String(new char[block.getDifficultyTarget()]).replace('\0', '0'); // Create a string with difficulty * "0"
        do {
            generateHash(block);
            block.setNonce(block.getNonce() + 1);
        }
        while (!block.getHash().substring(0, block.getDifficultyTarget()).equals(target));
        // add reward/coinbase  transaction to the miner's wallet
        Transaction coinbaseTransaction = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(nodeOwner),5, utxos, blockHeight);

        block.addTransaction(coinbaseTransaction);
    }

    public static Block generateEmptyBlock(Block prevBlock,PublicKey nodeOwner, List<UTXO> utxos, int blockHeight) {
        Block blockToBeMined = new Block(prevBlock, null);
        BlockUtil.mineBlock(blockToBeMined, nodeOwner, utxos, blockHeight);
        return blockToBeMined;
    }

    public static Block generateBlockWithTransaction(Block prevBlock,PublicKey nodeOwner, List<UTXO> utxos, int blockHeight, List<Transaction> transactions) {
        Block blockToBeMined = new Block(prevBlock, null);
        BlockUtil.mineBlock(blockToBeMined, nodeOwner, utxos, blockHeight);

        // add the unconfirmed transactions
        transactions.stream().forEach(transaction -> blockToBeMined.addTransaction(transaction));

        // generate the merkle root
        blockToBeMined.setMerkleRoot(BlockUtil.generateMerkleRoot(blockToBeMined.getTransactions()));

        return blockToBeMined;
    }
}
