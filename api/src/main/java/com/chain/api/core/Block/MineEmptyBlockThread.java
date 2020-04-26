package com.chain.api.core.Block;

import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Net.CNode;
import com.chain.api.core.Net.MiningTask;
import com.chain.api.core.Transaction.UTXO;
import com.chain.api.core.Transaction.UnconfirmedTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MineEmptyBlockThread implements Runnable  {

    Logger logger = LoggerFactory.getLogger(MineEmptyBlockThread.class);

    @Value("${app.PUBLIC_KEY}")
    private String publicKey;

    private List<Block> blockchain;

    private List<UTXO> unspentTransactionOutputs;

    private UnconfirmedTransactions unconfirmedTransactions;

    private List<CNode> vNodes;

    private AtomicInteger difficultyTarget;

    @Autowired
    public void setDifficultyTarget(AtomicInteger difficultyTarget) {
        this.difficultyTarget = difficultyTarget;
    }

    @Autowired
    public MineEmptyBlockThread(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    @Autowired
    public void setvNodes(List<CNode> vNodes) {
        this.vNodes = vNodes;
    }

    @Autowired
    public void setUnconfirmedTransactions(UnconfirmedTransactions unconfirmedTransactions) { this.unconfirmedTransactions = unconfirmedTransactions; }

    @Autowired
    public void setUnspentTransactionOutputs(List<UTXO> unspentTransactionOutputs) {
        this.unspentTransactionOutputs = unspentTransactionOutputs;
    }

    @Override
    public void run() {
        MiningTask miningTaskEmptyBlock = mineEmptyBlock();

        if(miningTaskEmptyBlock == null) {
            System.console().printf("Failed to mine! \n");
            return;
        }

        while (true) {
            if(!miningTaskEmptyBlock.getThread().isAlive()) {
                if(readUserInput()) {
                    miningTaskEmptyBlock = mineEmptyBlock();

                    if(miningTaskEmptyBlock == null) {
                        System.console().printf("Failed to mine! \n");
                        break;
                    }
                }
                else {
                    System.console().printf("Mining stoped! \n");
                    // stop the mining of the block
                    miningTaskEmptyBlock.getCreateBlockThread().stopMining();
                    break;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public MiningTask mineEmptyBlock() {
        try {
            return BlockUtil.generateEmptyBlock(
                    blockchain.get(blockchain.size() - 1),
                    CryptoUtil.getPublicKeyFromString(publicKey),
                    unspentTransactionOutputs,
                    unconfirmedTransactions.getTransactions(),
                    blockchain,
                    vNodes,
                    difficultyTarget);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean readUserInput() {
        System.console().printf("Type Stop to stop mining empty blocks! \n");

        String userInput = System.console().readLine();

        return userInput.equals("Stop") ? false : true;
    }
}
