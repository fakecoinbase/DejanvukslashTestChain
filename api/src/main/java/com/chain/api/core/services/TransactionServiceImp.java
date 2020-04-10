package com.chain.api.core.services;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Net.CNode;
import com.chain.api.core.Net.NetUtil;
import com.chain.api.core.Transaction.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@RestController
public class TransactionServiceImp implements TransactionService {

    @Autowired
    List<Block> blockchain;

    @Autowired
    List<UTXO> unspentTransactionOutputs;

    @Autowired
    UnconfirmedTransactions unconfirmedTransactions;

    @Autowired
    List<CNode> vNodes;

    @Override
    public Transaction createTransaction(TransactionPayload payload) {
        try {
            Transaction transaction = TransactionUtil.createTransaction(payload.getFrom(), payload.getTo(), payload.getValue(), unspentTransactionOutputs, blockchain.size());

            // add the transactions to unconfirmed
            unconfirmedTransactions.addUnconfirmedTransactions(transaction);

            // send the transaction to all known peers
            NetUtil.sendTransactionToAllPeers(transaction,vNodes);
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
