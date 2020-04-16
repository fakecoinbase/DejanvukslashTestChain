package com.chain.api.core.services;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Net.CNode;
import com.chain.api.core.Net.CreateBlockThread;
import com.chain.api.core.Net.NetUtil;
import com.chain.api.core.Transaction.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Objects;

@RestController
public class TransactionServiceImp implements TransactionService {

    private List<CNode> vNodes;

    private List<Block> blockchain;

    private UnconfirmedTransactions unconfirmedTransactions;

    private KeyPair nodeOwnerKeyPair;

    private List<UTXO> unspentTransactionOutputs;

    private List<CreateBlockThread> threadList;

    @Autowired
    public void setThreadList(List<CreateBlockThread> threadList) {this.threadList = threadList;}

    @Autowired
    public void setNodeOwnerKeyPair(KeyPair nodeOwnerKeyPair) { this.nodeOwnerKeyPair = nodeOwnerKeyPair; }

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

    @Override
    public Transaction createTransaction(TransactionPayload payload) {
        try {
            Transaction transaction = TransactionUtil.createTransaction(payload.getFrom(), payload.getTo(), payload.getValue(), unspentTransactionOutputs, unconfirmedTransactions.getTransactions(), blockchain.size());

            Objects.requireNonNull(transaction, "transaction can't be null!");

            if(TransactionUtil.verifyTransaction(transaction, blockchain, blockchain.size())) {
                System.out.println("The transaction is invalid!");
                return null;
            }

            TransactionUtil.handleTransaction(
                    transaction,
                    blockchain,
                    unspentTransactionOutputs,
                    unconfirmedTransactions,
                    threadList,
                    nodeOwnerKeyPair.getPublic(),
                    vNodes);

            return transaction;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
