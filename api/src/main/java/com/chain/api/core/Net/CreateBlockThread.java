package com.chain.api.core.Net;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionInput;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UTXO;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

public class CreateBlockThread implements Runnable {
    private volatile boolean exit = false;

    private Block prevBlock;
    private PublicKey nodeOwnder;
    private List<UTXO> utxos;
    private int blockHeight;
    private List<Transaction> transactions;
    private List<Transaction> unconfirmedTransactions;
    private List<Block> blockchain;
    private List<CNode> vNodes;

    public CreateBlockThread(Block prevBlock, PublicKey nodeOwner,List<UTXO> utxos, int blockHeight,List<Transaction> transactions, List<Transaction> unconfirmedTransactions, List<Block> blockchain, List<CNode> vNodes) {
        this.prevBlock = prevBlock;
        this.nodeOwnder = nodeOwner;
        this.utxos = utxos;
        this.blockHeight = blockHeight;
        this.transactions = transactions;
        this.unconfirmedTransactions = unconfirmedTransactions;
        this.blockchain = blockchain;
        this.vNodes = vNodes;
    }

    public void stopMining(){
        exit = true;
    }

    @Override
    public void run() {
        Block block = generateBlock(prevBlock, nodeOwnder, utxos, blockHeight, transactions);

        mineBlock(block, blockHeight);

        // Add the block to the blockchain
        blockchain.add(block);

        // add the new TXO as UTXO and remove the used UTXO as TXIs
        TransactionUtil.updateUtxos(transactions,utxos);

        // remove the unconfirmed transactions
        TransactionUtil.updateUnconfirmedTransactions(utxos,unconfirmedTransactions);

        // send the block to all our peers
        Thread thread = new Thread(() -> NetUtil.sendBlockToAllPeers(block, vNodes));
        thread.start();

        // Add the block to the database

    }

    private Block generateBlock(Block prevBlock, PublicKey nodeOwner, List<UTXO> utxos, int blockHeight, List<Transaction> transactions) {
        Block blockToBeMined = new Block(prevBlock, null);

        // add reward/coinbase  transaction to the miner's wallet
        Transaction coinbaseTransaction = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(nodeOwner),5, utxos, blockHeight);
        blockToBeMined.addTransaction(coinbaseTransaction);
        blockToBeMined.getTransactions().addAll(transactions);

        // generate the merkle root
        blockToBeMined.setMerkleRoot(BlockUtil.generateMerkleRoot(transactions));

        return blockToBeMined;
    }

    private void mineBlock(Block block,int blockHeight) {
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
