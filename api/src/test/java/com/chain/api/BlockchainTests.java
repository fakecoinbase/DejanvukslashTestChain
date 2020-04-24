package com.chain.api;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Block.BlockUtil;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Net.CNode;
import com.chain.api.core.Net.CreateBlockThread;
import com.chain.api.core.Net.MiningTask;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionUtil;
import com.chain.api.core.Transaction.UTXO;
import com.chain.api.core.Transaction.UnconfirmedTransactions;
import com.chain.api.core.Wallet.WalletUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BlockchainTests {
    // first node
    private List<Block> blockchainFirstNode;
    private List<UTXO> utxosFirstNode;
    private UnconfirmedTransactions unconfirmedTransactionsFirstNode;
    private List<CNode> vNodesFirstNode;
    private List<MiningTask> miningTaskListFirstNode;

    // second node
    private List<Block> blockchainSecondNode;
    private List<UTXO> utxosSecondNode;
    private UnconfirmedTransactions unconfirmedTransactionsSecondNode;
    private List<CNode> vNodesSecondNode;
    private List<MiningTask> miningTaskListSecondNode;

    // Users
    private PublicKey publicKeyFirstUser; // Also owner of the first node
    private PrivateKey privateKeyFirstUser;

    private PublicKey publicKeySecondUser; // Also owner of the second node
    private PrivateKey privateKeySecondUser;

    private PublicKey publicKeyThirdUser;
    private PrivateKey privateKeyThirdUser;



    @BeforeEach
    public void setup() {
        System.out.println("=============== Start of Blockchain Tests Setup ===============\n");

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        this.blockchainFirstNode = new ArrayList<>();
        this.utxosFirstNode = new ArrayList<>();
        this.unconfirmedTransactionsFirstNode = new UnconfirmedTransactions();
        this.vNodesFirstNode = new ArrayList<>();
        this.miningTaskListFirstNode = new ArrayList<>();

        this.blockchainSecondNode = new ArrayList<>();
        this.utxosSecondNode = new ArrayList<>();
        this.unconfirmedTransactionsSecondNode = new UnconfirmedTransactions();
        this.vNodesSecondNode = new ArrayList<>();
        this.miningTaskListSecondNode = new ArrayList<>();

        KeyPair keyPair = WalletUtil.generateKeyPair();
        publicKeyFirstUser = keyPair.getPublic();
        privateKeyFirstUser = keyPair.getPrivate();

        keyPair = WalletUtil.generateKeyPair();
        publicKeySecondUser = keyPair.getPublic();
        privateKeySecondUser = keyPair.getPrivate();

        keyPair = WalletUtil.generateKeyPair();
        publicKeyThirdUser = keyPair.getPublic();
        privateKeyThirdUser = keyPair.getPrivate();

        System.out.println("public key sender:      " + CryptoUtil.getStringFromKey(publicKeyFirstUser));

        System.out.println("private key sender:      " + CryptoUtil.getStringFromKey(privateKeyFirstUser));

        System.out.println("public key receiver     " + CryptoUtil.getStringFromKey(publicKeySecondUser));

        System.out.println("public key third user   " + CryptoUtil.getStringFromKey(publicKeyThirdUser));

        System.out.println("=============== End of Blockchain Tests Setup   ===============\n");
    }

    @Test
    public void BlockGenerationTest() {
        MiningTask miningTaskGenesisBlock = BlockUtil.generateGenesisBlock(publicKeyFirstUser, blockchainFirstNode, null);

        miningTaskListFirstNode.add(miningTaskGenesisBlock);

        try {
            miningTaskListFirstNode.get(0).getThread().join();

            System.out.println(blockchainFirstNode.get(0));

            assertTrue(blockchainFirstNode.size() == 1);

            assertEquals(utxosFirstNode.size(), 0);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MiningTask miningTaskEmptyBlock = BlockUtil.generateEmptyBlock(blockchainFirstNode.get(blockchainFirstNode.size() - 1), publicKeyFirstUser, utxosFirstNode,unconfirmedTransactionsFirstNode.getTransactions(),blockchainFirstNode,vNodesFirstNode);
        miningTaskListFirstNode.add(miningTaskEmptyBlock);

        try {
            miningTaskListFirstNode.get(1).getThread().join();

            assertTrue(blockchainFirstNode.size() == 2);

            assertEquals(utxosFirstNode.size(), 1);

            //blockchainFirstNode.stream().forEach(System.out::println);

            //utxosFirstNode.stream().forEach(System.out::println);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // empty the mining thread list

        miningTaskListFirstNode.clear();

        // mine 8 empty blocks

        for(int i = 0; i < 8; i++) {
            MiningTask miningTask = BlockUtil.generateEmptyBlock(blockchainFirstNode.get(blockchainFirstNode.size() - 1), publicKeyFirstUser, utxosFirstNode,unconfirmedTransactionsFirstNode.getTransactions(),blockchainFirstNode,vNodesFirstNode);
            try {
                miningTask.getThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertTrue(blockchainFirstNode.size() == 10);
        assertEquals(utxosFirstNode.size(), 9);
        assertEquals(0, unconfirmedTransactionsFirstNode.getTransactions().size());

        // empty the mining thread list
        miningTaskListFirstNode.clear();

        assertEquals(450, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeyFirstUser, utxosFirstNode)));

        // test block generation with transactions on the first node
        for(int i = 0; i < 4; i++) {
            try {
                Transaction trans = TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeyFirstUser),CryptoUtil.getStringFromKey(publicKeySecondUser), 100, utxosFirstNode,unconfirmedTransactionsFirstNode.getTransactions(), blockchainFirstNode.size());
                unconfirmedTransactionsFirstNode.getTransactions().add(trans);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }

        MiningTask miningTaskFullBlock = BlockUtil.generateBlockWithTransaction(
                blockchainFirstNode.get(blockchainFirstNode.size() - 1),
                publicKeyFirstUser,
                utxosFirstNode,
                blockchainFirstNode.size(),
                unconfirmedTransactionsFirstNode.copyUnconfirmedTransactions(),
                unconfirmedTransactionsFirstNode.getTransactions(),
                blockchainFirstNode,
                vNodesFirstNode
        );

        miningTaskListFirstNode.add(miningTaskFullBlock);

        try {
            miningTaskListFirstNode.get(miningTaskListFirstNode.size() - 1).getThread().join();

            assertEquals(11, blockchainFirstNode.size()); // 1 genesis block, 9 empty blocks , 1 block with transactions
            assertEquals(5 + 1, utxosFirstNode.size()); // 4 new utxo's of Second user + the coinbase reward transaction of first user

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Test the handling of the blocks */

        blockchainSecondNode.add(blockchainFirstNode.get(0));

        // Send the mined blocks from first node to second node in order

        for(int i = 1; i < blockchainFirstNode.size(); i++) {
            Block block = blockchainFirstNode.get(i);
            BlockUtil.handleBlock(block,
                    blockchainSecondNode,
                    utxosSecondNode,
                    unconfirmedTransactionsSecondNode,
                    miningTaskListSecondNode,
                    publicKeySecondUser,
                    vNodesSecondNode);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(blockchainFirstNode.size(), blockchainSecondNode.size());
        assertEquals(utxosFirstNode.size(), utxosSecondNode.size());
    }

}
