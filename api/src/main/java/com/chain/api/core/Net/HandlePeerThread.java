package com.chain.api.core.Net;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UTXO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HandlePeerThread implements Runnable{
    private CNode CNode;

    private List<CNode> vNodes;

    private List<Block> blockchain;

    private List<Transaction> unconfirmedTransactions;

    private KeyPair nodeOwnerKeyPair;

    private List<UTXO> unspentTransactionOutputs;

    @Autowired
    public void setNodeOwnerKeyPair(KeyPair nodeOwnerKeyPair) { nodeOwnerKeyPair = nodeOwnerKeyPair; }

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
    public void setUnconfirmedTransactions(List<Transaction> unconfirmedTransactions) { this.unconfirmedTransactions = unconfirmedTransactions; }

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
                        break;
                    case BLOCK: // received a block
                        // add the block to the blockchain
                        break;
                    case TRANS: // received a transaction
                        try {
                            // read the transaction and perform verifications
                            Transaction transaction = (Transaction) CNode.getObjectInput().readObject();
                            if(!TransactionUtil.verifyTransaction(transaction,blockchain, blockchain.size())) {
                                System.out.println("The transaction is invalid!");
                                break;
                            }
                            // add the transaction to our block or create a new block if it's full
                            synchronized (this) {
                                unconfirmedTransactions.add(transaction);
                                if(unconfirmedTransactions.size() == 3499) {
                                    // generate a new block with the unconfirmed transactions
                                    Thread thread = new Thread(){
                                        public void run(){
                                            Block block = BlockUtil.generateBlockWithTransaction(
                                                    blockchain.get(blockchain.size() - 1),
                                                    nodeOwnerKeyPair.getPublic(),
                                                    unspentTransactionOutputs,
                                                    blockchain.size(),
                                                    unconfirmedTransactions
                                            );
                                            // clear the unconfirmed transactions list
                                            unconfirmedTransactions = Collections.synchronizedList(new ArrayList<Transaction>());
                                        }
                                    };
                                    thread.start();
                                }
                            }

                            // send the transaction to all of our known peers save the one who sent it
                            Thread thread = new Thread(){
                                public void run(){
                                    NetUtil.sendTransactionToAllPeers(transaction, vNodes);
                                }
                            };
                            thread.start();

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            break;
                        }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
