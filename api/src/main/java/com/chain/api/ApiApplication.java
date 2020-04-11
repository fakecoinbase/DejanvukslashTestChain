package com.chain.api;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Net.CNode;
import com.chain.api.core.Net.CreateBlockThread;
import com.chain.api.core.Net.ListenThread;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.UTXO;
import com.chain.api.core.Transaction.UnconfirmedTransactions;
import com.chain.api.core.Wallet.WalletUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class ApiApplication {

    @Bean
    @Scope("singleton")
    public List<Block> blockchain() { return new ArrayList<Block>(); }

    @Bean
    @Scope("singleton")
    public List<UTXO> unspentTransactionOutputs() { return new ArrayList<UTXO>(); }

    @Bean
    @Scope("singleton")
    public UnconfirmedTransactions unconfirmedTransactions() { return new UnconfirmedTransactions();}

    @Bean
    @Scope("singleton")
    public List<CNode> vNodes() {return new ArrayList<CNode>(); }

    @Bean
    @Scope("singleton")
    public KeyPair nodeOwnerKeyPair() {return WalletUtil.generateKeyPair();}

    @Bean
    @Scope("singleton")
    public List<CreateBlockThread>threadList() {return new ArrayList<CreateBlockThread>();}


    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());



        Thread listenThread = new Thread(new ListenThread(4000));
        listenThread.start();

        SpringApplication.run(ApiApplication.class, args);

    }

}
