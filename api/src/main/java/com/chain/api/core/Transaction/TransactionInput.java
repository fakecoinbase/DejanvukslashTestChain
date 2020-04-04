package com.chain.api.core.Transaction;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInput {
    private String previousTx;  //hash of a previous transaction / TXID
    private Integer index; // specific output in the referenced transaction
    private String signature; // this is to prevent anybody else from spending funds in our wallet.

    // in coinbase transaction index will be used as nonce
    public void increaseNonce() {
            this.index++;
    }
}
