package com.chain.api.core.Net;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Transaction.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.Socket;
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
                        NetUtil.sendAddrMessageToPeer(vNodes, CNode);
                        break;
                    case ADDR: // received a list of peers
                        try {
                            List<String> peersList = (ArrayList<String>)CNode.getObjectInput().readObject();
                            addPeers(peersList);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
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
                            BlockUtil.handleBlock(block,
                                    blockchain,
                                    unspentTransactionOutputs,
                                    unconfirmedTransactions,
                                    threadList,
                                    nodeOwnerKeyPair.getPublic(),
                                    vNodes);
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

    public void stopMiningThreads(List<CreateBlockThread> threadList) {
        threadList.stream().forEach(thread -> thread.stopMining());
        threadList.clear();
    }

    public void addPeers(List<String> peersList) {
        Objects.requireNonNull(peersList, "received null peers list!");

        peersList.stream().forEach(peer -> {
            String[] parts = peer.split(":");

            try {
                Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));

                CNode cNode = new CNode(socket);

                // Handle peer on a different thread
                Thread handlePeerThread = new Thread(new HandlePeerThread(cNode));
                handlePeerThread.start();

                //  Add it to our list of known peers
                vNodes.add(cNode);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
