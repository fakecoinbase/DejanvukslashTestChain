package com.currency.example.node;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.UTXO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class NodeApplication {
    @Bean
    @Scope("singleton")
    public List<Block> blockchain() {
        return new ArrayList<Block>();
    }

    @Bean
    @Scope("singleton")
    public List<UTXO> unspentTransactionOutputs() { return new ArrayList<UTXO>(); }

    @Bean
    @Scope("singleton")
    public List<Transaction> transactions() {
        return new ArrayList<Transaction>();
    }

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SpringApplication.run(NodeApplication.class, args);
    }

}
