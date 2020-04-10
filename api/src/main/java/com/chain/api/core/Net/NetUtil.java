package com.chain.api.core.Net;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Transaction.Transaction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class NetUtil {
    public static void sendTransactionToPeer(Transaction transaction , CNode peer) {
        sendObjectToPeer(transaction, peer, MsgType.TRANS);
    }

    public static void sendTransactionToAllPeers(Transaction transaction, List<CNode> peers) {
        peers.stream().forEach(peer -> {
            sendTransactionToPeer(transaction,peer);
        });
    }

    public static void sendBlockToPeer(Block block , CNode peer) {
        sendObjectToPeer(block, peer, MsgType.BLOCK);
    }

    public static void sendBlockToAllPeers(Block block, List<CNode> peers) {
        peers.stream().forEach(peer -> {
            sendBlockToPeer(block,peer);
        });
    }

    public static void sendBlockchainToPeer(List<Block> blocks, CNode peer) {
        sendObjectToPeer(blocks, peer, MsgType.BLOCKCHAIN);
    }

    public static void sendBlockchainToAllPeers(List<Block> blocks, List<CNode> peers) {
        peers.stream().forEach(peer -> {
            sendBlockchainToPeer(blocks,peer);
        });
    }

    public static void sendGetAddrMessageToPeers(List<CNode> peers) {
        peers.stream().forEach(peer -> sendGetAddrMessageToPeer(peer));
    }

    /**
     * Send GETADDR message to a peer
     * @param peer
     */
    public static void sendGetAddrMessageToPeer(CNode peer) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(peer.getSocket().getOutputStream());
            objectOutputStream.writeUTF(MsgType.GETADDR.name());
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendAddrMessageToPeers(List<CNode> peers) {
        peers.stream().forEach(peer -> sendAddrMessageToPeer(peers,peer));
    }

    /**
     * Send ADDR message to a peer along with our peer's list
     * @param peers
     * @param toPeer
     */
    public static void sendAddrMessageToPeer(List<CNode> peers, CNode toPeer) {
        try {
            List<String> peersList = peers.stream().filter(peer -> !peer.equals(toPeer))
                    .map(peer -> {
                Socket socket = peer.getSocket();
                InetAddress addr = socket.getInetAddress();
                int         port = socket.getPort();
                return addr.getHostAddress() + " : " + Integer.toString(port);
            }).collect(Collectors.toList());

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(toPeer.getSocket().getOutputStream());

            objectOutputStream.writeUTF(MsgType.ADDR.name());
            objectOutputStream.writeObject(peersList);

            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void sendObjectToPeer(Object data, CNode peer, MsgType msgType) {
        switch (msgType) {
            case BLOCK:
                Block block = (data != null) ? (Block) data : null;
                if(block == null){
                    System.out.println("Cannot send an empty block!");
                    break;
                }
                try {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(peer.getSocket().getOutputStream());
                    objectOutputStream.writeUTF(msgType.toString());
                    objectOutputStream.writeObject(block);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case BLOCKCHAIN:
                Block[] block_array = (data != null) ? (Block[]) data : null;
                if(block_array == null) {
                    System.out.println("Cannot send an empty blockchain!");
                    break;
                }
                List<Block> blockchain = new ArrayList<>(Arrays.asList(block_array));

                try {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(peer.getSocket().getOutputStream());
                    objectOutputStream.writeUTF(msgType.toString());
                    objectOutputStream.writeObject(blockchain);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case TRANS:
                Transaction transaction = (Transaction) data;
                try {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(peer.getSocket().getOutputStream());
                    objectOutputStream.writeUTF(msgType.toString());
                    objectOutputStream.writeObject(transaction);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


}
