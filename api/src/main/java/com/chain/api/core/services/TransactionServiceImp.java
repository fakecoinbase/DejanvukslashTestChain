package com.chain.api.core.services;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.MineEmptyBlockThread;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Net.CNode;
import com.chain.api.core.Net.CreateBlockThread;
import com.chain.api.core.Net.MiningTask;
import com.chain.api.core.Net.NetUtil;
import com.chain.api.core.Transaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class TransactionServiceImp implements TransactionService {

    Logger logger = LoggerFactory.getLogger(TransactionServiceImp.class);

    private List<CNode> vNodes;

    private List<Block> blockchain;

    private UnconfirmedTransactions unconfirmedTransactions;

    @Value("${app.PUBLIC_KEY}")
    private String publicKey;

    private List<UTXO> unspentTransactionOutputs;

    private List<MiningTask> miningTaskList;

    @Autowired
    public void setMiningTaskList(List<MiningTask> miningTaskList) {
        this.miningTaskList = miningTaskList;
    }

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
    public ResponseEntity<?> createTransaction(TransactionPayload payload, BindingResult bindingResult) {
        ResponseEntity<?> errors = validateResult(bindingResult);

        if(errors != null) {
            logger.error("Transaction payload is invalid!");
            return errors;
        }

        try {
            Transaction transaction = TransactionUtil.createTransaction(payload.getFrom(), payload.getTo(), payload.getValue(), unspentTransactionOutputs, unconfirmedTransactions.getTransactions(), blockchain.size());

            TransactionUtil.handleTransaction(
                    transaction,
                    blockchain,
                    unspentTransactionOutputs,
                    unconfirmedTransactions,
                    miningTaskList,
                    CryptoUtil.getPublicKeyFromString(publicKey),
                    vNodes);

            return new ResponseEntity<>(transaction, HttpStatus.CREATED);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseEntity<?> validateResult(BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            Map<String,String> errorMap = new HashMap<>();
            for(FieldError error: bindingResult.getFieldErrors()) {
                errorMap.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<Map<String,String>>(errorMap, HttpStatus.BAD_REQUEST);
        }
        else return null;
    }
}
