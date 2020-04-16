package com.chain.api.core.Net;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ListenThread implements Runnable {
    @Autowired
    private List<CNode> vNodes;

    private ServerSocket serverSocket;

    private int port;

    public ListenThread(int port) {
        this.port = port;
    }

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
                Thread handlePeerThread = new Thread(new HandlePeerThread(peer));
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
