package com.chain.api.core.Net;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HandlePeerThread implements Runnable{
    private CNode CNode;

    private List<CNode> vNodes;

    private List<Block> blockchain;

    @Value("${app.PUBLIC_KEY}")
    private String publicKey;

    private UnconfirmedTransactions unconfirmedTransactions;

    private List<UTXO> unspentTransactionOutputs;

    private List<MiningTask> miningTaskList;

    private AtomicInteger difficultyTarget;;

    @Autowired
    public void setDifficultyTarget(AtomicInteger difficultyTarget) {
        this.difficultyTarget = difficultyTarget;
    }

    @Autowired
    public void setMiningTaskList(List<MiningTask> miningTaskList) {this.miningTaskList = miningTaskList;}

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
                        stopMiningThreads(miningTaskList);

                        // handle the block
                        Block block = null;
                        try {
                            block = (Block) CNode.getObjectInput().readObject();
                            BlockUtil.handleBlock(block,
                                    blockchain,
                                    unspentTransactionOutputs,
                                    unconfirmedTransactions,
                                    miningTaskList,
                                    CryptoUtil.getPublicKeyFromString(publicKey),
                                    vNodes,
                                    difficultyTarget);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            System.out.println(e.getMessage());
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchProviderException e) {
                            e.printStackTrace();
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                        break;
                    case TRANS: // received a transaction
                        Transaction transaction = null;
                        try {
                            // read the transaction and perform verifications
                            transaction = (Transaction) CNode.getObjectInput().readObject();

                            TransactionUtil.handleTransaction(
                                    transaction,
                                    blockchain,
                                    unspentTransactionOutputs,
                                    unconfirmedTransactions,
                                    miningTaskList,
                                    CryptoUtil.getPublicKeyFromString(publicKey),
                                    vNodes,
                                    difficultyTarget);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            break;
                        } catch (NullPointerException e) {
                            System.out.println(e.getMessage());
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchProviderException e) {
                            e.printStackTrace();
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopMiningThreads(List<MiningTask> miningTasks) {
        miningTasks.stream().forEach(miningTask -> miningTask.getCreateBlockThread().stopMining());
        miningTasks.clear();
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
