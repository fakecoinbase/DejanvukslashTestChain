package com.chain.api.core.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnconfirmedTransactions {
    private List<Transaction> transactions;

    public UnconfirmedTransactions() {
        this.transactions = Collections.synchronizedList(new ArrayList<Transaction>());
    }

    public synchronized void updateUnconfirmedTransactions(List<Transaction> newUnconfirmedTransactions) {
        this.transactions = newUnconfirmedTransactions;
    }

    public synchronized List<Transaction> getTransactions() { return transactions; }
}
