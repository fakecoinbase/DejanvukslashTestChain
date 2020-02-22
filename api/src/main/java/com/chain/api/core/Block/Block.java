package com.chain.api.core.Block;

import com.chain.api.core.Transaction.Transaction;
import com.chain.util.crypto.CryptoUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Block {
    private Integer index;
    private String hash;
    private String previousHash;
    private Long timestamp;
    private List<Transaction> transactions;
    private String merkleRoot;
    private Integer difficultyTarget;

    public Block() {}

    public Block(Block prevBlock) {
        generateBlock(prevBlock);
    }

    private void generateBlock(Block prevBlock) {
        Block retBlock = new Block();
        retBlock.setTimestamp(new Date().getTime());
        if(prevBlock != null) {
            retBlock.setPreviousHash(prevBlock.getHash());
            retBlock.setIndex(prevBlock.getIndex());
        }
        retBlock.generateHash();
    }

    public void generateHash() {
        hash = CryptoUtil.encryptSha256(previousHash + index + timestamp + merkleRoot);
    }

    public void mineBlock() {
        this.setTimestamp(new Date().getTime());
        this.setDifficultyTarget(5);// read difficulty from a central config server
        String target = new String(new char[this.getDifficultyTarget()]).replace('\0', '0'); // Create a string with difficulty * "0"
    }

    public static String generateMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
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
            currBlock.generateHash();
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
     * @return true if first chain is bigger
     */
    public static boolean compareChains(List<Block> origBlockchain, List<Block> newBlockchain) {
        return origBlockchain.size() > newBlockchain.size();
    }

    private boolean checkGenesisBlock(Block firstBlock) {
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
}

