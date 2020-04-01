package com.chain.api.core.Net;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendObjectThread implements Runnable {
    private Object data;
    private MsgType msgType;

    private List<CNode> vNodes;

    private List<Block> blockchain;

    @Autowired
    public void setvNodes(List<CNode> vNodes) {
        this.vNodes = vNodes;
    }

    @Autowired
    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    SendObjectThread(Object data, MsgType msgType) {
        this.data = data;
        this.msgType = msgType;
    }

    @Override
    public void run() {
        switch (msgType) {
            case GETADDR:
                break;
            case ADDR:
                break;
            case BLOCK:
                Block block = (data != null) ? (Block) data : null;
                if(block == null){
                    System.out.println("Cannot send an empty block!");
                    break;
                }
                vNodes.stream().forEach(peer -> {
                    try {
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(peer.getSocket().getOutputStream());
                        objectOutputStream.writeUTF(MsgType.BLOCK.name());
                        objectOutputStream.writeObject(block);
                        objectOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case BLOCKCHAIN:
                /*
                Block[] block_array = (data != null) ? (Block[]) data : null;
                if(block_array == null) {
                    System.out.println("Cannot send an empty blockchain!");
                    break;
                }
                List<Block> blockchain = new ArrayList<>(Arrays.asList(block_array));
                */
                vNodes.stream().forEach(peer -> {
                    try {
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(peer.getSocket().getOutputStream());
                        objectOutputStream.writeUTF(MsgType.BLOCK.name());
                        objectOutputStream.writeObject(blockchain);
                        objectOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case TRANS:
                Transaction transaction = (Transaction) data;
                vNodes.stream().forEach(peer -> {
                    try {
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(peer.getSocket().getOutputStream());
                        objectOutputStream.writeUTF(MsgType.TRANS.name());
                        objectOutputStream.writeObject(transaction);
                        objectOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
        }
    }
}
