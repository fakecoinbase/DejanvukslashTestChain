package com.chain.api.Configuration;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Block.MineEmptyBlockThread;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Net.ListenThread;
import com.chain.api.core.Net.MaintenanceThread;
import com.chain.api.core.Net.MiningTask;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    private List<Block> blockchain;

    @Value("${app.PUBLIC_KEY}")
    private String publicKey;

    private ApplicationContext ctx;

    private TaskExecutor taskExecutor;

    @Autowired
    public StartupApplicationListener(List<Block> blockchain,  ApplicationContext ctx, TaskExecutor taskExecutor) {
        this.blockchain = blockchain;
        this.ctx = ctx;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        /*
        MiningTask miningTaskGenesisBlock = BlockUtil.generateGenesisBlock(nodeOwnerKeyPair.getPublic(), blockchain, null);

        try {
            miningTaskGenesisBlock.getThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         */

        Block genesisBlock = new Block(null, null);

        Transaction coinbaseTransaction = TransactionUtil.createCoinbaseTransaction(publicKey,50, 0);
        genesisBlock.addTransaction(coinbaseTransaction);

        // generate the merkle root
        genesisBlock.setMerkleRoot(BlockUtil.generateMerkleRoot(genesisBlock.getTransactions()));

        genesisBlock.setNonce(14733);

        genesisBlock.setHash("0008ad78f0275a8998e931ea696f19fc41a468bb37dc1c91a3e95159bf0f4cde");

        blockchain.add(genesisBlock);

        System.out.println(genesisBlock);

        //Thread maintenanceThread = new Thread(new MaintenanceThread());
        //maintenanceThread.start();
        taskExecutor.execute(ctx.getBean(MaintenanceThread.class));

        //Thread listenThread = new Thread(new ListenThread(4000));
        //listenThread.start();
        taskExecutor.execute(ctx.getBean(ListenThread.class));

        //Thread mineEmptyThread = new Thread(new MineEmptyBlockThread());
        //mineEmptyThread.start();
        taskExecutor.execute(ctx.getBean(MineEmptyBlockThread.class));
    }
}
