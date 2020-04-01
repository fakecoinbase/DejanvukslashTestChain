package com.chain.api.core.Net;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/*
A class to manage peers
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class CNode {
    private Socket socket;
    private boolean fWhiteListed;
    private ObjectOutputStream objectOutput;
    private ObjectInputStream objectInput;

    public CNode(Socket socket) {
        this.socket = socket;
        this.fWhiteListed = false;
        try {
            objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectInput = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
