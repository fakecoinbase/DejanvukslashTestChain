package com.chain.api.core.Transaction;

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
public class UnconfirmedTransactions {
    private List<Transaction> transactions;

    public UnconfirmedTransactions() {
        this.transactions = new ArrayList<Transaction>();
    }

    public synchronized void addUnconfirmedTransactions(Transaction newUnconfirmedTransaction) {
        this.transactions.add(newUnconfirmedTransaction);
    }

    public synchronized List<Transaction> getTransactions() { return transactions; }


    public List<Transaction> copyUnconfirmedTransactions() {
        /*
        * Normally we would use a deep copy of the transactions but this would require
        * Key to be serializable
        *  */
        List<Transaction> copy = new ArrayList<>();
        this.transactions.stream().forEach(tx -> copy.add(tx));
        return copy;
    }
}
