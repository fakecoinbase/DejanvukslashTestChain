package com.chain.api.core.Block;

import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.Transaction;
import com.chain.api.core.Transaction.TransactionUtil;
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
        else { // genesis
            this.index = 0;
        }
        this.transactions = transactions;
        this.difficultyTarget = 3; // read difficulty from a central config server
        this.nonce = 0;
    }

    public void addTransaction(Transaction transaction) {
        if(transactions == null) transactions = new ArrayList<>();
        transactions.add(transaction);
    }

}

