package com.chain.api.core.Net;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Transaction.UTXO;
import com.chain.api.core.Transaction.UnconfirmedTransactions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope("prototype")
public class ListenThread implements Runnable {
    private List<CNode> vNodes;

    private List<Block> blockchain;

    @Value("${app.PUBLIC_KEY}")
    private String publicKey;

    private UnconfirmedTransactions unconfirmedTransactions;

    private List<UTXO> unspentTransactionOutputs;

    private List<MiningTask> miningTaskList;

    private AtomicInteger difficultyTarget;

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
    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    @Autowired
    public void setUnconfirmedTransactions(UnconfirmedTransactions unconfirmedTransactions) { this.unconfirmedTransactions = unconfirmedTransactions; }

    @Autowired
    public void setvNodes(List<CNode> vNodes) {
        this.vNodes = vNodes;
    }

    private ServerSocket serverSocket;

    private int port = 4000;

    // Implement the login for listening incoming requests
    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            // log "Error could not listen on port " + port";
            System.exit(1);
        }

        while(true) {
            try {
                Socket socket = serverSocket.accept();

                // Create a new peer
                CNode peer = new CNode(socket);

                // Handle peer on a different thread
                Thread handlePeerThread = new Thread(new HandlePeerThread(peer, vNodes, blockchain, unconfirmedTransactions, unspentTransactionOutputs, miningTaskList, difficultyTarget));
                handlePeerThread.start();

                //  Add it to our list of known peers
                vNodes.add(peer);

                // ask the new peer for his list of peers
                NetUtil.sendGetAddrMessageToPeer(peer);

            } catch (IOException e) {
                // log "Error accepting peer!";
                e.printStackTrace();
            }
        }
    }
}
