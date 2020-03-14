package com.chain.api.core.Block;

import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.Transaction;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Document(collection = "Blocks")
public class Block {
    @Id
    private String id;

    private Integer index;
    private String hash;
    private String previousHash;
    private Long timestamp;
    private List<Transaction> transactions;
    private String merkleRoot;
    private Integer difficultyTarget;
    private Integer nonce; // blockHeight

    public Block(Block prevBlock, List<Transaction> transactions) {
        if(prevBlock != null) {
            this.previousHash = prevBlock.getHash();
            this.index = prevBlock.getIndex() + 1;
        }
        if(this.transactions != null) {
            this.transactions = transactions;
        }
        this.difficultyTarget = 5; // read difficulty from a central config server
        this.nonce = 0;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void generateHash() {
        hash = CryptoUtil.encryptSha256(previousHash + index + timestamp + merkleRoot + nonce);
    }

    public void mineBlock() {
        this.setTimestamp(new Date().getTime());
        this.generateMerkleRoot();
        String target = new String(new char[this.getDifficultyTarget()]).replace('\0', '0'); // Create a string with difficulty * "0"
        do {
            generateHash();
            nonce++;
        }
        while (!hash.substring(0, difficultyTarget).equals(target));
        // add reward/coinbase  transaction to the miner's wallet
    }

    private void generateMerkleRoot() {
        if(this.transactions.size() == 0) return;
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getTXID());
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(CryptoUtil.encryptSha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        this.merkleRoot = merkleRoot;
    }
}

