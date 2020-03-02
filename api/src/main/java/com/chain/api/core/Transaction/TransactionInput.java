package com.chain.api.core.Transaction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class TransactionInput {
    private String previousTx;  //hash of a previous transaction / TXID
    private Integer index; // specific output in the referenced transaction
    private String signature; // this is to prevent anybody else from spending funds in our wallet.
}
