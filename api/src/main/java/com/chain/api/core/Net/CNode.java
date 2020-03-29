package com.chain.api.core.Net;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;

import java.io.IOException;
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

    public CNode(Socket socket) {
        this.socket = socket;
        this.fWhiteListed = false;
    }
}
