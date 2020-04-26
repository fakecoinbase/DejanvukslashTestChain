package com.chain.api.core.Block;

import com.chain.api.core.Transaction.Transaction;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Block {
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
        else { // genesis
            this.index = 0;
        }
        this.transactions = transactions;
        this.nonce = 0;
    }

    public void addTransaction(Transaction transaction) {
        if(transactions == null) transactions = new ArrayList<>();
        transactions.add(transaction);
    }

}

