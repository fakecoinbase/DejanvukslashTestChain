package com.chain.api.core.Net;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionInput;
import com.chain.api.core.Transaction.TransactionUtil;

import java.security.PublicKey;
import java.util.Date;

public class MineBlockThread implements Runnable {
    private volatile boolean exit = false;

    private Block block;
    private PublicKey nodeOwnder;
    private int blockHeight;

    public MineBlockThread(Block block, PublicKey nodeOwner, int blockHeight) {
        this.block = block;
        this.nodeOwnder = nodeOwner;
        this.blockHeight = blockHeight;
    }

    @Override
    public void run() {
        block.setTimestamp(new Date().getTime());
        String target = new String(new char[block.getDifficultyTarget()]).replace('\0', '0'); // Create a string with difficulty * "0"
        while (!exit && !block.getHash().substring(0, block.getDifficultyTarget()).equals(target)) {
            BlockUtil.generateHash(block);
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
    }

}
