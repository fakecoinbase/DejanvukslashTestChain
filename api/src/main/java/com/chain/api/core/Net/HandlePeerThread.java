package com.chain.api.core.Net;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UTXO;
import com.chain.api.core.Transaction.UnconfirmedTransactions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HandlePeerThread implements Runnable{
    private CNode CNode;

    private List<CNode> vNodes;

    private List<Block> blockchain;

    private UnconfirmedTransactions unconfirmedTransactions;

    private KeyPair nodeOwnerKeyPair;

    private List<UTXO> unspentTransactionOutputs;

    private List<CreateBlockThread> threadList;

    @Autowired
    public void setThreadList(List<CreateBlockThread> threadList) {this.threadList = threadList;}

    @Autowired
    public void setNodeOwnerKeyPair(KeyPair nodeOwnerKeyPair) { this.nodeOwnerKeyPair = nodeOwnerKeyPair; }

    @Autowired
    public void setUnspentTransactionOutputs(List<UTXO> unspentTransactionOutputs) {
        this.unspentTransactionOutputs = unspentTransactionOutputs;
    }

    @Autowired
    public void setvNodes(List<CNode> vNodes) {
        this.vNodes = vNodes;
    }

    @Autowired
    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    @Autowired
    public void setUnconfirmedTransactions(UnconfirmedTransactions unconfirmedTransactions) { this.unconfirmedTransactions = unconfirmedTransactions; }

    HandlePeerThread(CNode CNode) {
        this.CNode = CNode;
    }

    @Override
    public void run() {
        while(!CNode.getSocket().isClosed()) {
            try {
                String msgString = CNode.getObjectInput().readUTF();
                if(msgString.isEmpty()) continue;
                MsgType msg = MsgType.valueOf(msgString);
                switch (msg) {
                    case GETADDR: // send our list of peers back
                        break;
                    case ADDR: // received a list of peers
                        break;
                    case BLOCKCHAIN: //received a list of blocks
                        try {
                            List<Block> receivedBlockchain = (ArrayList<Block>)CNode.getObjectInput().readObject();
                            // validate the blockchain

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        break;
                    case BLOCK: // received a block
                        // stop the mining threads if they exist
                        stopMiningThreads(threadList);

                        // handle the block
                        Block block = null;
                        try {
                            block = (Block) CNode.getObjectInput().readObject();
                            handleBlock(block);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case TRANS: // received a transaction
                        Transaction transaction = null;
                        try {
                            // read the transaction and perform verifications
                            transaction = (Transaction) CNode.getObjectInput().readObject();

                            Objects.requireNonNull(transaction, "received null transaction!");

                            if(TransactionUtil.verifyTransaction(transaction, blockchain, blockchain.size())) {
                                System.out.println("The transaction is invalid!");
                                break;
                            }

                            TransactionUtil.handleTransaction(
                                    false,
                                    transaction,
                                    blockchain,
                                    unspentTransactionOutputs,
                                    unconfirmedTransactions,
                                    threadList,
                                    nodeOwnerKeyPair.getPublic(),
                                    vNodes);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            break;
                        } catch (NullPointerException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleBlock(Block block) {

        Objects.requireNonNull(block, "received null block!");

        // 1. validate the received block
        int blockHeight = blockchain.size() - 1;

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

            if(TransactionUtil.verifyTransaction(transaction, blockchain, blockHeight)) {
                System.out.println("Failed tx " + i + " check!");
            }
            else {
                validTransactions.add(transaction);
            }
        }

        // 2. add the block to the blockchain

        // if prevHash doesnt match our latest block then we have to query the peer for all his blockchain

        // If multiple blocks are mined at the same time
        // Check if prev block (matching prev hash) is in main branch or side branches.
        // If not, add this to orphan blocks, then query peer we got this from for 1st missing orphan block in prev chain; done with block
        // TO DO

        if(validTransactions.size() >= 1 && validTransactions.size() != block.getTransactions().size()) {
            // add the valid transactions to our current block
            validTransactions.stream().forEach(transaction ->  TransactionUtil.handleTransaction(
                    false,
                    transaction,
                    blockchain,
                    unspentTransactionOutputs,
                    unconfirmedTransactions,
                    threadList,
                    nodeOwnerKeyPair.getPublic(),
                    vNodes));
        }
        else {
            // add the validated block to the tree
            blockchain.add(block);
            TransactionUtil.updateUtxos(block.getTransactions(),unspentTransactionOutputs,unconfirmedTransactions);
        }

        // 3. send it to all the known peers
        Thread thread = new Thread(){
            public void run(){
                NetUtil.sendBlockToAllPeers(block, vNodes);
            }
        };
        thread.start();
    }

    public void handleBlockchain(List<Block> receivedBlochain) {
        Objects.requireNonNull(receivedBlochain, "received null blockchain!");
    }

    public void stopMiningThreads(List<CreateBlockThread> threadList) {
        threadList.stream().forEach(thread -> thread.stopMining());
    }

}
