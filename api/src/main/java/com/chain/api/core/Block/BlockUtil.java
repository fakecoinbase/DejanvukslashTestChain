package com.chain.api.core.Block;

import com.chain.api.core.Transaction.Transaction;

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
