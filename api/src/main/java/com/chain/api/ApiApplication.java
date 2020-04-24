package com.chain.api;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Net.*;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.UTXO;
import com.chain.api.core.Transaction.UnconfirmedTransactions;
import com.chain.api.core.Wallet.WalletUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class ApiApplication {

    @Bean
    @Scope("singleton")
    public List<Block> blockchain() { return new ArrayList<>(); }

    @Bean
    @Scope("singleton")
    public List<UTXO> unspentTransactionOutputs() { return new ArrayList<>(); }

    @Bean
    @Scope("singleton")
    public UnconfirmedTransactions unconfirmedTransactions() { return new UnconfirmedTransactions();}

    @Bean
    @Scope("singleton")
    public List<CNode> vNodes() {return new ArrayList<>(); }

    @Bean
    @Scope("singleton")
    public List<MiningTask> miningTaskList() {return new ArrayList<>();}

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Thread maintenanceThread = new Thread(new MaintenanceThread());
        maintenanceThread.start();

        Thread listenThread = new Thread(new ListenThread(4000));
        listenThread.start();

        SpringApplication.run(ApiApplication.class, args);

    }

}
