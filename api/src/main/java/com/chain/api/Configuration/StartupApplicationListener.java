package com.chain.api.Configuration;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Net.MiningTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.List;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    private List<Block> blockchain;
    private KeyPair nodeOwnerKeyPair;
    private List<MiningTask> miningTaskList;

    @Autowired
    public StartupApplicationListener(List<Block> blockchain, KeyPair nodeOwnerKeyPair, List<MiningTask> miningTaskList) {
        this.blockchain = blockchain;
        this.nodeOwnerKeyPair = nodeOwnerKeyPair;
        this.miningTaskList = miningTaskList;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        MiningTask miningTaskGenesisBlock = BlockUtil.generateGenesisBlock(nodeOwnerKeyPair.getPublic(), blockchain, null);

        try {
            miningTaskGenesisBlock.getThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
